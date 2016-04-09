package breakingtherules.dao.es;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

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
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
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

    public static final String CLUSTER_NAME = "breakingtherules";
    public static final String INDEX_NAME = "btr";
    public static final String TYPE_HIT = "hit";

    public static final String FIELD_ID = "id";
    public static final String FIELD_ATTRIBUTES = "attributes";
    public static final String FIELD_JOB_ID = "jobId";
    public static final String FIELD_ATTR_TYPEID = "typeId";
    public static final String FIELD_ATTR_VALUE = "value";

    private Node m_elasticNode;

    private Client m_elasticClient;

    /**
     * Cache for the number of hits, for a specific job, with certain Rules and
     * Filter. This prevents reading ALL of the job's hits to determine the
     * number of relevant hits. Useful for getHits with startIndex and endIndex
     */
    private HashMap<Triple<Integer, List<Rule>, Filter>, Integer> m_totalHitsCache;

    public HitsElasticDao() {
	m_elasticNode = NodeBuilder.nodeBuilder().settings(Settings.settingsBuilder().put("http.enabled", false))
		.clusterName(CLUSTER_NAME).client(true).node();
	m_elasticClient = m_elasticNode.client();
	m_totalHitsCache = new HashMap<Triple<Integer, List<Rule>, Filter>, Integer>();
    }

    public void cleanup() {
	m_elasticNode.close();
    }

    private void refreshIndex() {
	m_elasticClient.admin().indices().prepareRefresh(INDEX_NAME).get();
    }

    public boolean doesJobExist(int jobId) {
	try {
	    QueryBuilder query = termQuery(FIELD_JOB_ID, jobId);
	    SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(INDEX_NAME).setQuery(query).setSize(0)
		    .setTerminateAfter(1);
	    SearchResponse response = srchRequest.get();
	    return response.getHits().totalHits() > 0;
	} catch (IndexNotFoundException e) {
	    return false;
	}
    }

    public void deleteJob(int jobId) {
	System.out.println("deleting");
	int TOO_BIG = 3000;
	QueryBuilder query = termQuery(FIELD_JOB_ID, jobId);
	SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(INDEX_NAME).setQuery(query).setSize(TOO_BIG);
	SearchResponse response = srchRequest.get();
	if (response.getHits().totalHits() > TOO_BIG) {
	    System.out
		    .println("Job is too big to delete programatically. Please delete manually through ElasticSearch.");
	}
	if (response.getHits().getTotalHits() == 0) {
	    System.out.println("nothing to delete");
	    return;
	}
	BulkRequestBuilder bulkDelete = m_elasticClient.prepareBulk();
	for (SearchHit h : response.getHits().getHits()) {
	    System.out.println(h.getId());
	    DeleteRequestBuilder deleteRequest = m_elasticClient.prepareDelete().setIndex(INDEX_NAME).setType(TYPE_HIT)
		    .setId(h.getId());
	    bulkDelete.add(deleteRequest);
	}
	BulkResponse deleteResponse = bulkDelete.execute().actionGet();
	if (deleteResponse.hasFailures()) {
	    throw new RuntimeException(deleteResponse.buildFailureMessage());
	}
	refreshIndex();
    }

    public void addHit(Hit hit, int jobId) {
	addHits(Collections.singletonList(hit), jobId);
	refreshIndex();
    }

    public void addHits(List<Hit> hits, int jobId) {
	BulkRequestBuilder bulkRequest = m_elasticClient.prepareBulk();
	for (Hit hit : hits) {
	    try {
		XContentBuilder hitJson = jsonBuilder().startObject();
		hitJson.field(FIELD_ID, hit.getId());
		hitJson.field(FIELD_JOB_ID, jobId);
		hitJson.startArray(FIELD_ATTRIBUTES);
		for (Attribute attr : hit.getAttributes()) {
		    hitJson.startObject();
		    hitJson.field(FIELD_ATTR_TYPEID, attr.getTypeId());
		    hitJson.field(FIELD_ATTR_VALUE, attr.toString());
		    hitJson.endObject();
		}
		hitJson.endArray().endObject();

		IndexRequestBuilder indexRequest = m_elasticClient.prepareIndex(INDEX_NAME, TYPE_HIT);
		indexRequest.setSource(hitJson);
		indexRequest.setId(elasticHitId(jobId, hit.getId()));
		bulkRequest.add(indexRequest);

	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	BulkResponse bulkResponse = bulkRequest.execute().actionGet();
	if (bulkResponse.hasFailures()) {
	    System.out.println("Bulk add request had failures.");
	    // process failures by iterating through each bulk response item
	    for (BulkItemResponse item : bulkResponse) {
		System.out.println(item.getFailureMessage());
	    }
	}
	refreshIndex();
    }

    private String elasticHitId(int jobId, int hitId) {
	return jobId + "x" + hitId;
    }

    private Hit toFirewallHit(SearchHit searchHit) {
	Map<String, Object> fields = searchHit.getSource();

	int hitId = (int) fields.get(FIELD_ID);
	List<Attribute> attrs = new ArrayList<Attribute>();

	Object allAtributes = fields.get(FIELD_ATTRIBUTES);
	if (!(allAtributes instanceof ArrayList<?>)) {
	    System.out.println("Unexpected hit format");
	    return null;
	}
	for (Object attribute : (ArrayList<?>) allAtributes) {
	    if (!(attribute instanceof HashMap<?, ?>)) {
		System.out.println("Unexpected hit format");
		return null;
	    }
	    HashMap<?, ?> attributeHash = (HashMap<?, ?>) attribute;
	    int attrTypeID = (int) attributeHash.get(FIELD_ATTR_TYPEID);
	    String attrValue = (String) attributeHash.get(FIELD_ATTR_VALUE);
	    attrs.add(Attribute.createFromString(attrTypeID, attrValue));
	}

	return new Hit(hitId, attrs);

    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter) throws IOException {
	List<Hit> hits = getHits(jobId, rules, filter, true, 0, 0);
	m_totalHitsCache.put(new Triple<Integer, List<Rule>, Filter>(jobId, rules, filter), hits.size());
	return new ListDto<Hit>(hits, 0, hits.size(), hits.size());
    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException {
	Integer total = m_totalHitsCache.get(new Triple<Integer, List<Rule>, Filter>(jobId, rules, filter));
	if (total == null) {
	    ListDto<Hit> allHits = getHits(jobId, rules, filter); // which saves
								  // to cache
	    List<Hit> hits = Utility.subList(allHits.getData(), startIndex, endIndex - startIndex);
	    return new ListDto<Hit>(hits, startIndex, endIndex, allHits.getData().size());
	} else {
	    List<Hit> hits = getHits(jobId, rules, filter, false, startIndex, endIndex);
	    return new ListDto<Hit>(hits, startIndex, endIndex, total);
	}
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
    private List<Hit> getHits(int jobId, List<Rule> rules, Filter filter, boolean all, int startIndex, int endIndex) {
	int TIME_PER_SCROLL = 60000; // in milliseconds
	int HITS_PER_SCROLL = 200;

	QueryBuilder query = termQuery(FIELD_JOB_ID, jobId);
	SortBuilder sort = SortBuilders.fieldSort(FIELD_ID).order(SortOrder.ASC);

	SearchRequestBuilder srchRequest = m_elasticClient.prepareSearch(INDEX_NAME)
		.setSearchType(SearchType.QUERY_AND_FETCH).setScroll(new TimeValue(TIME_PER_SCROLL)).setQuery(query)
		.addSort(sort).setSize(HITS_PER_SCROLL);
	SearchResponse scrollResp = srchRequest.execute().actionGet();

	ArrayList<Hit> relevantHits = new ArrayList<Hit>();
	int i = 0; // to only take the relevant indices.
	if (all)
	    startIndex = 0;

	// Scroll until no hits are returned or endIndex has been reached
	while (true) {
	    System.out.println(scrollResp.getHits().getTotalHits());
	    // Go over search results
	    for (SearchHit srchHit : scrollResp.getHits().getHits()) {
		// Add the hit to the answer list, if it passes rules and
		// filters
		Hit firewallHit = toFirewallHit(srchHit);
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

	    // Get next batch
	    scrollResp = m_elasticClient.prepareSearchScroll(scrollResp.getScrollId())
		    .setScroll(new TimeValue(TIME_PER_SCROLL)).execute().actionGet();
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
    private static boolean isMatch(List<Rule> rules, Filter filter, Hit hit) {
	if (!filter.isMatch(hit)) {
	    return false;
	}
	for (Rule rule : rules) {
	    if (rule.getId() > 1 && rule.isMatch(hit)) {
		return false;
	    }
	}
	return true;
    }

}
