package breakingtherules.dao.es;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import breakingtherules.dao.HitsDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

public class HitsElasticDao implements HitsDao {

    public static final String CLUSTER_NAME = "breakingtherules";
    public static final String INDEX_NAME = "btr";
    public static final String TYPE_HIT = "hit";

    private Node m_elasticNode;
    private Client m_elasticClient;

    public HitsElasticDao() {
	m_elasticNode = NodeBuilder.nodeBuilder().settings(Settings.settingsBuilder().put("http.enabled", false))
		.clusterName(CLUSTER_NAME).client(true).node();
	m_elasticClient = m_elasticNode.client();
    }

    public void cleanup() {
	m_elasticNode.close();
    }

    public void addHit(Hit hit) {
	try {
	    XContentBuilder hitJson = jsonBuilder().startObject().startArray("attributes");
	    for (Attribute attr : hit.getAttributes()) {
		hitJson.field(attr.getType(), attr.toString());
	    }
	    hitJson.endArray().endObject();
	    IndexResponse response = m_elasticClient.prepareIndex(INDEX_NAME, TYPE_HIT).setSource(hitJson).get();
	    System.out.println(response.toString());
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter) throws IOException {
	// TODO Auto-generated method stub
	return new ListDto<Hit>(new ArrayList<Hit>(), 0, 0, 0);
    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException {
	// TODO Auto-generated method stub
	return new ListDto<Hit>(new ArrayList<Hit>(), 0, 0, 0);
    }

}
