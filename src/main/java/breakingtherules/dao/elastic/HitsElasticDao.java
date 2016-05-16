package breakingtherules.dao.elastic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import breakingtherules.dao.HitsDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Triple;
import breakingtherules.utilities.Utility;

public class HitsElasticDao implements HitsDao {

    private final Node m_elasticNode;

    private final Client m_elasticClient;

    /**
     * Cache for the number of hits, for a specific job, with certain Rules and
     * Filter. This prevents reading ALL of the job's hits to determine the
     * number of relevant hits. Useful for getHits with startIndex and endIndex
     */
    private final Map<Triple<Integer, List<Rule>, Filter>, Integer> m_totalHitsCache;

    public HitsElasticDao() {
	final NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder();
	final Builder settingsBuilder = Settings.settingsBuilder();
	settingsBuilder.put("http.enabled", false);
	nodeBuilder.settings(settingsBuilder);
	nodeBuilder.clusterName(ElasticDaoConfig.CLUSTER_NAME);
	nodeBuilder.client(true);

	m_elasticNode = nodeBuilder.node();
	m_elasticClient = m_elasticNode.client();
	m_totalHitsCache = new HashMap<>();
    }

    public void cleanup() {
	m_elasticNode.close();
    }

    public boolean doesJobExist(int jobId) {
	try {
	    final SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(ElasticDaoConfig.INDEX_NAME);
	    final QueryBuilder query = QueryBuilders.termQuery(ElasticDaoConfig.FIELD_JOB_ID, jobId);
	    srchRequest.setQuery(query);
	    srchRequest.setSize(0);
	    srchRequest.setTerminateAfter(1);
	    final SearchResponse response = srchRequest.get();
	    return response.getHits().totalHits() > 0;

	} catch (final IndexNotFoundException e) {
	    return false;
	}
    }

    public void deleteJob(int jobId) {
	System.out.println("deleting");

	final QueryBuilder query = QueryBuilders.termQuery(ElasticDaoConfig.FIELD_JOB_ID, jobId);
	final SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(ElasticDaoConfig.INDEX_NAME);
	srchRequest.setQuery(query);
	srchRequest.setSize(ElasticDaoConfig.DELETION_THRESHOLD);

	final SearchHits hitsRes = srchRequest.get().getHits();
	if (hitsRes.totalHits() > ElasticDaoConfig.DELETION_THRESHOLD) {
	    System.out
		    .println("Job is too big to delete programatically. Please delete manually through ElasticSearch.");
	}
	if (hitsRes.getTotalHits() == 0) {
	    System.out.println("nothing to delete");
	    return;
	}
	final BulkRequestBuilder bulkDelete = m_elasticClient.prepareBulk();
	for (final SearchHit hit : hitsRes.getHits()) {
	    System.out.println(hit.getId());

	    final DeleteRequestBuilder deleteRequest = m_elasticClient.prepareDelete();
	    deleteRequest.setIndex(ElasticDaoConfig.INDEX_NAME);
	    deleteRequest.setType(ElasticDaoConfig.TYPE_HIT);
	    deleteRequest.setId(hit.getId());

	    bulkDelete.add(deleteRequest);
	}
	final BulkResponse deleteResponse = bulkDelete.execute().actionGet();
	if (deleteResponse.hasFailures()) {
	    throw new RuntimeException(deleteResponse.buildFailureMessage());
	}
	refreshIndex();
    }

    public void addHit(final Hit hit, final int jobId) {
	addHits(Collections.singletonList(hit), jobId);
	refreshIndex();
    }

    public void addHits(final List<Hit> hits, final int jobId) {
	final BulkRequestBuilder bulkRequest = m_elasticClient.prepareBulk();
	for (final Hit hit : hits) {

	    try {
		final XContentBuilder hitJson = XContentFactory.jsonBuilder();
		hitJson.startObject();
		hitJson.field(ElasticDaoConfig.FIELD_ID, hit.getId());
		hitJson.field(ElasticDaoConfig.FIELD_JOB_ID, jobId);
		hitJson.startArray(ElasticDaoConfig.FIELD_ATTRIBUTES);
		for (final Attribute attr : hit) {
		    hitJson.startObject();
		    hitJson.field(ElasticDaoConfig.FIELD_ATTR_TYPEID, attr.getTypeId());
		    hitJson.field(ElasticDaoConfig.FIELD_ATTR_VALUE, attr.toString());
		    hitJson.endObject();
		}
		hitJson.endArray().endObject();

		final IndexRequestBuilder indexRequest = m_elasticClient.prepareIndex(ElasticDaoConfig.INDEX_NAME,
			ElasticDaoConfig.TYPE_HIT);
		indexRequest.setSource(hitJson);
		indexRequest.setId(elasticHitId(jobId, hit.getId()));
		bulkRequest.add(indexRequest);

		hitJson.close();

	    } catch (final IOException e) {
		e.printStackTrace();
	    }
	}
	final BulkResponse bulkResponse = bulkRequest.execute().actionGet();
	if (bulkResponse.hasFailures()) {
	    System.out.println("Bulk add request had failures.");
	    // process failures by iterating through each bulk response item
	    for (final BulkItemResponse item : bulkResponse) {
		System.out.println(item.getFailureMessage());
	    }
	}
	refreshIndex();
    }

    @Override
    public int getHitsNumber(final int jobId, final List<Rule> rules, final Filter filter) throws IOException {
	final Integer cachedSize = m_totalHitsCache.get(new Triple<>(jobId, rules, filter));
	if (cachedSize != null) {
	    return cachedSize;
	} else {
	    return getHits(jobId, rules, filter).getSize();
	}
    }

    @Override
    public ListDto<Hit> getHits(final int jobId, final List<Rule> rules, final Filter filter) throws IOException {
	final List<Hit> hits = getHits(jobId, rules, filter, true, 0, 0);
	m_totalHitsCache.put(new Triple<>(jobId, rules, filter), hits.size());
	return new ListDto<>(hits, 0, hits.size(), hits.size());
    }

    @Override
    public ListDto<Hit> getHits(final int jobId, final List<Rule> rules, final Filter filter, final int startIndex,
	    final int endIndex) throws IOException {
	final Integer total = m_totalHitsCache.get(new Triple<>(jobId, rules, filter));
	if (total == null) {
	    // getHits(int, List<Rule>, Filter) save to cache
	    final ListDto<Hit> allHits = getHits(jobId, rules, filter);
	    final List<Hit> hits = Utility.subList(allHits.getData(), startIndex, endIndex - startIndex);
	    return new ListDto<>(hits, startIndex, endIndex, allHits.getData().size());
	} else {
	    final List<Hit> hits = getHits(jobId, rules, filter, false, startIndex, endIndex);
	    return new ListDto<>(hits, startIndex, endIndex, total);
	}
    }

    private void refreshIndex() {
	// using "execute" instead of "get" in the following line, does not
	// ensure that the refresh will happen immediately
	m_elasticClient.admin().indices().prepareRefresh(ElasticDaoConfig.INDEX_NAME).get();
    }

    /**
     * Gets hits from the ElasticSearch database. If the boolean "all" is true,
     * this method ignores startIndex and endIndex. Otherwise, out of all the
     * hits that do not match the given rules and filter, this method returns
     * the hits in range [startIndex, endIndex)
     * 
     * @param jobId
     *            The job from which to take the hits
     * @param rules
     *            The rules of this job. Hits that match these rules will not be
     *            accounted.
     * @param filter
     *            The filter of this job. Hits that match this filter will not
     *            be accounted.
     * @param all
     *            Whether to return all the matching hits or relate to the
     *            startIndex and endIndex
     * @param startIndex
     *            0-index of the wanted beginning hit, from all the hits that
     *            don't match the rules+filter
     * @param endIndex
     *            0-index + 1 of the wanted finishing hit, from all the hits
     *            that don't match the rules+filter
     * @return A ListDto of the relevant hits, from the given range, if such
     *         exists
     */
    private List<Hit> getHits(final int jobId, final List<Rule> rules, final Filter filter, final boolean all,
	    int startIndex, final int endIndex) {
	final QueryBuilder query = QueryBuilders.termQuery(ElasticDaoConfig.FIELD_JOB_ID, jobId);
	final SortBuilder sort = SortBuilders.fieldSort(ElasticDaoConfig.FIELD_ID).order(SortOrder.ASC);

	// TODO - make this code readable
	final SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(ElasticDaoConfig.INDEX_NAME)
		.setSearchType(SearchType.QUERY_AND_FETCH).setScroll(new TimeValue(ElasticDaoConfig.TIME_PER_SCROLL))
		.setQuery(query).addSort(sort).setSize(ElasticDaoConfig.HITS_PER_SCROLL);
	SearchResponse scrollResp = srchRequest.execute().actionGet();

	final List<Hit> relevantHits = new ArrayList<>();
	int i = 0; // to only take the relevant indices.
	if (all) {
	    startIndex = 0;
	}

	// Scroll until no hits are returned or endIndex has been reached
	while (true) {
	    // Go over search results
	    for (final SearchHit srchHit : scrollResp.getHits().getHits()) {
		// Add the hit to the answer list, if it passes rules and
		// filters
		final Hit firewallHit = toFirewallHit(srchHit);
		if (isMatch(rules, filter, firewallHit)) {
		    // Found a hit that passes the rules and the filter
		    if (all || (i >= startIndex && i < endIndex)) {
			relevantHits.add(toFirewallHit(srchHit));
		    }
		    i++;
		    if (i == endIndex && !all) {
			break;
		    }
		}
	    }
	    if (i == endIndex && !all) {
		break;
	    }

	    // TODO - make this code readable
	    // Get next batch
	    scrollResp = m_elasticClient.prepareSearchScroll(scrollResp.getScrollId())
		    .setScroll(new TimeValue(ElasticDaoConfig.TIME_PER_SCROLL)).execute().actionGet();
	    // Break condition: No hits are returned
	    if (scrollResp.getHits().getHits().length == 0) {
		break;
	    }
	}

	return relevantHits;
    }

    /**
     * Check if a hit is match to a list of rules and a filter
     * 
     * i.e. The hit "passes" iff it matches the filter, but doesn't match any
     * rule except the first
     * 
     * @param rules
     *            list of rules to check on the hit
     * @param filter
     *            filter to check on the hit
     * @param hit
     *            the hit that being checked
     * @return true if hit match all rules and filter, else - false
     */
    private static boolean isMatch(final List<Rule> rules, final Filter filter, final Hit hit) {
	if (!filter.isMatch(hit)) {
	    return false;
	}
	for (final Rule rule : rules) {
	    if (rule.isMatch(hit)) {
		return false;
	    }
	}
	return true;
    }

    private static String elasticHitId(final int jobId, final int hitId) {
	return jobId + "x" + hitId;
    }

    private static Hit toFirewallHit(final SearchHit searchHit) {
	final Map<String, Object> fields = searchHit.getSource();

	final int hitId = (int) fields.get(ElasticDaoConfig.FIELD_ID);
	final List<Attribute> attrs = new ArrayList<>();

	final Object allAtributes = fields.get(ElasticDaoConfig.FIELD_ATTRIBUTES);
	if (!(allAtributes instanceof ArrayList)) {
	    System.out.println("Unexpected hit format");
	    return null;
	}
	for (final Object attribute : (ArrayList<?>) allAtributes) {
	    if (!(attribute instanceof Map)) {
		System.out.println("Unexpected hit format");
		return null;
	    }
	    final Map<?, ?> attributeHash = (Map<?, ?>) attribute;
	    final int attrTypeID = (int) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_TYPEID);
	    final String attrValue = (String) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_VALUE);
	    attrs.add(Attribute.createFromString(attrTypeID, attrValue));
	}
	return new Hit(hitId, attrs);
    }

}
