package breakingtherules.dao.elastic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
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

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.utilities.Triple;
import breakingtherules.utilities.Triple.UnmodifiableTriple;
import breakingtherules.utilities.Utility;

public class ElasticHitsDao implements HitsDao {

    // TODO extends from AbstractCachedHitsDao

    private final Node m_elasticNode;

    private final Client m_elasticClient;

    /**
     * Cache for the number of hits, for a specific job, with certain Rules and
     * Filter. This prevents reading ALL of the job's hits to determine the
     * number of relevant hits. Useful for getHits with startIndex and endIndex
     */
    private final Map<UnmodifiableTriple<Integer, List<Rule>, Filter>, Integer> m_totalHitsCache;

    public ElasticHitsDao() {
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

    /**
     * Should be called before destroying the DAO, in order to close the
     * connection to other ElasticSearch nodes
     */
    public void cleanup() {
	m_elasticNode.close();
    }

    /**
     * @param jobName
     *            The name of the job to check
     * @return Whether the job exists (has hits) in ElasticSearch
     */
    public boolean doesJobExist(String jobName) {
	try {
	    final SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(ElasticDaoConfig.INDEX_NAME);
	    final QueryBuilder query = QueryBuilders.termQuery(ElasticDaoConfig.FIELD_JOB_NAME, jobName);
	    srchRequest.setQuery(query);
	    srchRequest.setSize(0);
	    srchRequest.setTerminateAfter(1);
	    final SearchResponse response = srchRequest.get();
	    return response.getHits().totalHits() > 0;

	} catch (final IndexNotFoundException e) {
	    return false;
	}
    }

    /**
     * Deleting a job from ElasticSearch. Since this is a "dangerous" one-way
     * procedure, it cannot be done on jobs with many hits, and they have to be
     * erased directly through ES
     * 
     * @param jobName
     *            The name of the job to delete
     * @return Iff the job exited before calling deleteJob
     * @throws IOException
     *             If ElasticSearch has failures while deleting the job
     * @throws IllegalArgumentException
     *             If the job has too many hits
     */
    public boolean deleteJob(String jobName) throws IOException {

	final QueryBuilder query = QueryBuilders.termQuery(ElasticDaoConfig.FIELD_JOB_NAME, jobName);
	final SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(ElasticDaoConfig.INDEX_NAME);
	srchRequest.setQuery(query);
	srchRequest.setSize(ElasticDaoConfig.DELETION_THRESHOLD);

	final SearchHits hitsRes = srchRequest.get().getHits();
	if (hitsRes.totalHits() > ElasticDaoConfig.DELETION_THRESHOLD) {
	    throw new IllegalArgumentException(
		    "Job is too big to delete programatically. Please delete manually through ElasticSearch.");

	}
	if (hitsRes.getTotalHits() == 0) {
	    return false;
	}
	final BulkRequestBuilder bulkDelete = m_elasticClient.prepareBulk();
	for (final SearchHit hit : hitsRes.getHits()) {
	    final DeleteRequestBuilder deleteRequest = m_elasticClient.prepareDelete();
	    deleteRequest.setIndex(ElasticDaoConfig.INDEX_NAME);
	    deleteRequest.setType(ElasticDaoConfig.TYPE_HIT);
	    deleteRequest.setId(hit.getId());

	    bulkDelete.add(deleteRequest);
	}
	final BulkResponse deleteResponse = bulkDelete.execute().actionGet();
	if (deleteResponse.hasFailures()) {
	    throw new IOException(deleteResponse.buildFailureMessage());
	}
	refreshIndex();
	return true;
    }

    /**
     * Adding a hit to a certain job
     * 
     * @param hit
     *            A firewall Hit to be inserted to ElasticSearch
     * @param jobName
     *            The name of the job
     * @throws IOException
     *             If ElasticSearch cannot save the data
     */
    public void addHit(final Hit hit, final String jobName) throws IOException {
	addHits(Collections.singletonList(hit), jobName);
	refreshIndex();
    }

    /**
     * Adding hits to a certain job
     * 
     * @param hits
     *            A list of firewall Hits to be inserted to ElasticSearch
     * @param jobName
     *            The name of the job
     * @throws IOException
     *             If ElasticSearch cannot save the data
     */
    public void addHits(final Iterable<Hit> hits, final String jobName) throws IOException {
	final BulkRequestBuilder bulkRequest = m_elasticClient.prepareBulk();
	for (final Hit hit : hits) {

	    final XContentBuilder hitJson = XContentFactory.jsonBuilder();
	    hitJson.startObject();
	    hitJson.field(ElasticDaoConfig.FIELD_JOB_NAME, jobName);
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
	    bulkRequest.add(indexRequest);

	    hitJson.close();
	}
	final BulkResponse bulkResponse = bulkRequest.execute().actionGet();
	if (bulkResponse.hasFailures()) {
	    String error = "Bulk add request had failures. ";
	    // process failures by iterating through each bulk response item
	    for (final BulkItemResponse item : bulkResponse) {
		error += item.getFailureMessage() + " ";
	    }
	    throw new IOException(error);
	}
	refreshIndex();
    }

    @Override
    public int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter) throws IOException {
	final Integer cachedSize = m_totalHitsCache.get(new Triple<>(Integer.valueOf(jobName), rules, filter));
	if (cachedSize != null) {
	    return cachedSize.intValue();
	} else {
	    return getHitsList(jobName, rules, filter).getSize();
	}
    }

    @Override
    public ListDto<Hit> getHitsList(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException {
	final List<Hit> hits = Utility.newArrayList(getHits(jobName, rules, filter, true, 0, 0));

	// Create new list of the rules to clone the list - so modifications on
	// the original list will not change the list saved in the cache
	m_totalHitsCache.put(
		new UnmodifiableTriple<>(Integer.valueOf(jobName),
			Collections.unmodifiableList(Utility.newArrayList(rules)), filter),
		Integer.valueOf(hits.size()));
	return new ListDto<>(hits, 0, hits.size(), hits.size());
    }

    public Iterable<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, ParseException {
	return getHits(jobName, rules, filter, true, 0, 0);
    }

    @Override
    public void initJob(final String jobName, final Iterable<Hit> hits) throws IllegalArgumentException, IOException {
	addHits(hits, jobName);
    }

    /**
     * Refreshes the ElasticSearch index, which means updating it, so that the
     * next queries will be consistent will the previous ones
     */
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
     * @param jobName
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
    private Set<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter, final boolean all,
	    int startIndex, final int endIndex) {
	final QueryBuilder query = QueryBuilders.termQuery(ElasticDaoConfig.FIELD_JOB_NAME, jobName);

	final SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(ElasticDaoConfig.INDEX_NAME);
	srchRequest.setSearchType(SearchType.QUERY_AND_FETCH);
	srchRequest.setScroll(new TimeValue(ElasticDaoConfig.TIME_PER_SCROLL));
	srchRequest.setQuery(query);
	srchRequest.setSize(ElasticDaoConfig.HITS_PER_SCROLL);

	SearchResponse scrollResp = srchRequest.execute().actionGet();

	final Set<Hit> relevantHits = new HashSet<>();
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

	    // Get next batch - create new search request and then execute it
	    String oldScroller = scrollResp.getScrollId();
	    SearchScrollRequestBuilder srchScrollRequest;
	    srchScrollRequest = m_elasticClient.prepareSearchScroll(oldScroller);
	    srchScrollRequest.setScroll(new TimeValue(ElasticDaoConfig.TIME_PER_SCROLL));

	    scrollResp = srchScrollRequest.execute().actionGet();
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

    /**
     * Translates a SearchHit that represents a firewall Hit, into a firewall
     * Hit object
     * 
     * @param searchHit
     *            The outcome of an ElasticSearch search - a Hit, representing a
     *            firewall hit
     * @throws IllegalArgumentException
     *             If the searchHit is not in a valid hit format
     * @return A firewall Hit object with the values from the search
     */
    private static Hit toFirewallHit(final SearchHit searchHit) {
	final Map<String, Object> fields = searchHit.getSource();

	final List<Attribute> attrs = new ArrayList<>();

	final Object allAtributes = fields.get(ElasticDaoConfig.FIELD_ATTRIBUTES);
	if (!(allAtributes instanceof ArrayList)) {
	    throw new IllegalArgumentException("The searchHit it not in valid hit format");
	}
	for (final Object attribute : (ArrayList<?>) allAtributes) {
	    if (!(attribute instanceof Map)) {
		throw new IllegalArgumentException("The searchHit it not in valid hit format");
	    }
	    final Map<?, ?> attributeHash = (Map<?, ?>) attribute;
	    final int attrTypeID = ((Integer) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_TYPEID)).intValue();
	    final String attrValue = (String) attributeHash.get(ElasticDaoConfig.FIELD_ATTR_VALUE);
	    attrs.add(Attribute.createFromString(attrTypeID, attrValue));
	}
	return new Hit(attrs);
    }

}
