package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Pair;
import breakingtherules.utilities.Utility;

/**
 * Algorithm to get suggestions for rules. Based in information theory.
 * 
 * This algorithm is intended only for IP type attributes - i.e. Source and
 * Destination. This means that for other attributes, this algorithm backs up to
 * a different algorithm, i.e. SimpleAlgorithm.
 * 
 * This algorithm uses dynamic programming. It is based on a recursive rule to
 * decide if a certain node in the IP tree is worth separating, or if it is best
 * united (suggested on its own). The recursive rule also gives the node a
 * certain score, and the lower the score - the better the node, because we were
 * able to express the same amount of information with less bits (information
 * theory).
 * 
 * The recursive rule is: f(x) = min { |x| ( log( Sx ) - log ( P(x) ) )
 * f(x.right) + f(x.left) + K } Where: |x| is the number of hits under subnet x
 * Sx is the size of the subnet x (which is a power of 2) P(x) is the
 * probability of the subnet x, i.e. the percentage of hits that are in it out
 * of all hits K is a constant that allows the user to choose the permissiveness
 * of the rules they would like.
 */
public class InformationAlgorithm implements SuggestionsAlgorithm {

    /**
     * Number of suggestions that returned in each suggestions request
     */
    private static final int NUMBER_OF_SUGGESTIONS = 10;

    /**
     * Default value for the ruleWeight parameter
     */
    private static final double DEFAULT_RULE_WIEGHT = 3000;

    /**
     * An estimation to the percentage of nodes in the next layer (out of the
     * current layer length)
     */
    private static final double NEXT_LAYER_FACTOR = 0.75;

    /**
     * Allows configuration, if the user wants more general rules (high
     * ruleWeight) or more specific rules (low ruleWeight)
     */
    private double m_ruleWeight;

    /**
     * Used when the attribute type if not Source or Destination
     */
    private SimpleAlgorithm m_simpleAlgorithm;

    static {
	configCheck();
    }

    public InformationAlgorithm() {
	m_ruleWeight = DEFAULT_RULE_WIEGHT;
	m_simpleAlgorithm = new SimpleAlgorithm();
    }

    public void setRuleWeight(double weight) {
	System.out.println("New rule weight");
	m_ruleWeight = weight;
    }

    @Override
    public List<Suggestion> getSuggestions(List<Hit> hits, String attType) {
	hits = Utility.ensureUniqueness(hits);
	int attTypeId = Attribute.typeStrToTypeId(attType);

	checkHits(hits, attTypeId);

	switch (attTypeId) {
	case Attribute.DESTINATION_TYPE_ID:
	    return getSuggestionsDestination(hits);
	case Attribute.SOURCE_TYPE_ID:
	    return getSuggestionsSource(hits);
	default:
	    return m_simpleAlgorithm.getSuggestions(hits, attType);
	}
    }

    private static final Comparator<Suggestion> SUGGESTION_COMPARATOR_REV = new Comparator<Suggestion>() {
	@Override
	public int compare(Suggestion arg0, Suggestion arg1) {
	    return Integer.compare(arg1.getSize(), arg0.getSize());
	}
    };

    private List<Suggestion> getSuggestionsDestination(List<Hit> hits) {
	List<IPNode> nodes = asIPNodeList(hits, Attribute.DESTINATION_TYPE_ID);
	IPNode[] subnets = getIPSuggestions(nodes);
	List<Suggestion> suggestions = new ArrayList<Suggestion>();
	for (IPNode subnet : subnets) {
	    suggestions.add(new Suggestion(new Destination(subnet.ip), subnet.size, 1 / subnet.compressSize));
	}
	suggestions.sort(SUGGESTION_COMPARATOR_REV);
	return Utility.subList(suggestions, 0, NUMBER_OF_SUGGESTIONS);
    }

    private List<Suggestion> getSuggestionsSource(List<Hit> hits) {
	List<IPNode> nodes = asIPNodeList(hits, Attribute.SOURCE_TYPE_ID);
	IPNode[] subnets = getIPSuggestions(nodes);
	List<Suggestion> suggestions = new ArrayList<Suggestion>();
	for (IPNode subnet : subnets) {
	    suggestions.add(new Suggestion(new Source(subnet.ip), subnet.size, 1 / subnet.compressSize));
	}
	suggestions.sort(SUGGESTION_COMPARATOR_REV);
	return Utility.subList(suggestions, 0, NUMBER_OF_SUGGESTIONS);
    }

    private IPNode[] getIPSuggestions(List<IPNode> nodes) {
	if (nodes == null) {
	    throw new IllegalArgumentException("Nodes list can't be null");
	}
	if (nodes.size() == 0) {
	    return new IPNode[0];
	}
	if (nodes.size() == 1) {
	    return new IPNode[] { nodes.get(0) };
	}

	// # currentLayer - the current IP layer the algorithm is working on
	//
	// # listParent - the next IP layer that is currently constructed
	//
	List<IPNode> currentLayer, nextLayer;

	// # nodeA - the current node
	//
	// # nodeB - the next node, relevant only if brother of nodeA
	//
	// # parent - the parent node of nodeA (possible of nodeB too, if
	// brothers), currently constructed
	//
	IPNode nodeA, nodeB, parent;

	// # ipParentA - the parent IP of nodeA's IP
	//
	// # ipParentB - the parent IP of nodeB's IP
	//
	IP ipParentA, ipParentB;

	// # probability - probability of parent node out of the total hits
	//
	// # union - the optimal compress size if the parent node chosen as a
	// union single subnetwork
	//
	// # separated - the optimal compress size if the parent node chosen as
	// separated small subnetworks
	//
	double probability, union, separated;

	// # size - the number of hits in the constructed parent node
	//
	// # sizeTotal - the total number of hits, used to calculate probability
	// (constant value)
	//
	// # length - bound for index
	//
	int size, length;
	final int sizeTotal;

	// Sort the IPs, ensuring the assumption that if for a node there is a
	// brother, it will be next to it. This assumption will stay for next
	// layers too
	currentLayer = nodes;
	sortByIP(currentLayer);

	// Total size of elements (calculated out of loop for performance
	// improvements, and because it is constant value)
	sizeTotal = currentLayer.size();

	// Run until there are only one element in the list (all nodes are sub
	// children of the node)
	while ((length = currentLayer.size() - 1) > 0) {
	    nextLayer = new ArrayList<IPNode>((int) ((length + 1) * NEXT_LAYER_FACTOR));

	    // Run over all elements, for each element construct his parent
	    // element for the next layer by checking if his brother exist and
	    // if so - merge them, else construct the parent base only on the
	    // one current node. Run over all elements except the last one
	    // (length = size -1) so we don't get out of bounds when searching
	    // for brother (always in [curenntIndex + 1])
	    Iterator<Pair<IPNode, IPNode>> it = Utility.getDoubleIterator(currentLayer);
	    while (it.hasNext()) {
		Pair<IPNode, IPNode> pair = it.next();
		nodeA = pair.first;
		nodeB = pair.second;
		ipParentA = nodeA.ip.getParent();
		ipParentB = nodeB.ip.getParent();

		parent = new IPNode();
		parent.ip = ipParentA;

		// if nodeA and nodeB have same parent => brothers
		if (ipParentA.equals(ipParentB)) {
		    parent.size = size = nodeA.size + nodeB.size;

		    // size / totalSize
		    probability = size / (double) sizeTotal;

		    // size * (log(subnetwork size) + log (1 / probability)) + K
		    union = size * (Utility.log2(ipParentA.getSubnetSize()) + Utility.log2(1 / probability))
			    + m_ruleWeight;

		    // (nodeA optimal) + (nodeB optimal)
		    separated = nodeA.compressSize + nodeB.compressSize;

		    // choose optimal (minimum) choice between union subnetwork
		    // or separated small subnetworks. using <= prefer less
		    // subnetworks
		    if (union <= separated) {
			// using union subnetwork
			parent.compressSize = union;

			// subnetwork is the parent subnetwork
			parent.bestSubnets = new IPNode[] { parent };
		    } else {
			// using separated small subnetworks
			parent.compressSize = separated;

			// union the two subnetworks from both child nodes
			parent.bestSubnets = new IPNode[nodeA.bestSubnets.length + nodeB.bestSubnets.length];
			System.arraycopy(nodeA.bestSubnets, 0, parent.bestSubnets, 0, nodeA.bestSubnets.length);
			System.arraycopy(nodeB.bestSubnets, 0, parent.bestSubnets, nodeA.bestSubnets.length,
				nodeB.bestSubnets.length);
		    }

		    // Used the current node and next node, increase index by 2
		    if (it.hasNext()) {
			it.next();
		    }
		} else {
		    // nodeA and nodeB are not brothers, nodeB is not relevant.
		    // Copy all value from current node to parent node
		    parent.size = nodeA.size;
		    parent.compressSize = nodeA.compressSize;
		    parent.bestSubnets = nodeA.bestSubnets;
		}

		// Add the finished parent node to next layer list
		nextLayer.add(parent);
	    }

	    // Check last element in list - if was brother of one before last
	    // meaning no more action is require, else - create his parent node
	    // and copy his properties (no brother for sure)
	    nodeA = currentLayer.get(length - 1);
	    nodeB = currentLayer.get(length);
	    if (!IP.isBrothers(nodeA.ip, nodeB.ip)) {
		parent = new IPNode();
		parent.ip = nodeB.ip.getParent();
		parent.size = nodeB.size;
		parent.compressSize = nodeB.compressSize;
		parent.bestSubnets = nodeB.bestSubnets;
		nextLayer.add(parent);
	    }

	    // current layer is finished, move to next layer
	    currentLayer = nextLayer;
	}

	// list size is 1, the only element is the parent node of all others
	IPNode root = currentLayer.get(0);
	return root.bestSubnets;
    }

    private List<IPNode> asIPNodeList(List<Hit> list, int attTypeId) {
	List<IPNode> allNodes = new ArrayList<IPNode>(list.size());

	int listSize = list.size();
	for (int i = 0; i < listSize; i++) {
	    Hit hit = list.get(i);
	    IPNode ipNode = new IPNode();
	    ipNode.compressSize = m_ruleWeight;
	    ipNode.ip = ((IPAttribute) hit.getAttribute(attTypeId)).getIp();
	    ipNode.bestSubnets = new IPNode[] { ipNode };
	    ipNode.size = 1;
	    allNodes.add(ipNode);
	}

	sortByIP(allNodes);

	List<IPNode> uniqueNodes = new ArrayList<IPNode>();
	IPNode lastNode = null;
	for (IPNode node : allNodes) {
	    if (lastNode == null || !lastNode.ip.equals(node.ip)) {
		lastNode = node;
		uniqueNodes.add(node);
	    } else {
		lastNode.size++;
	    }
	}
	return uniqueNodes;
    }

    private static void checkHits(List<Hit> hits, int attId) {
	if (hits == null) {
	    throw new IllegalArgumentException("Hits list can't be null");
	}

	for (Hit hit : hits) {
	    if (hit == null) {
		throw new IllegalArgumentException("All hits shouldn't be null!");
	    }
	    Attribute att = hit.getAttribute(attId);
	    if (att == null) {
		throw new IllegalArgumentException("One of the hits doesn't have the desire attribute");
	    }
	}
    }

    @SuppressWarnings("unused")
    private static void configCheck() {
	if (DEFAULT_RULE_WIEGHT <= 0) {
	    throw new IllegalStateException("DEFAULT_RULE_WIEGHT should be > 0");
	}
	if (!(0 < NEXT_LAYER_FACTOR && NEXT_LAYER_FACTOR < 1)) {
	    throw new IllegalStateException("NEXT_LAYER_FACTOR should be > 0, < 1");
	}
    }

    private static final Comparator<IPNode> IP_COMPARATOR = new Comparator<IPNode>() {

	@Override
	public int compare(IPNode o1, IPNode o2) {
	    return o1.ip.compareTo(o2.ip);
	}
    };

    private static void sortByIP(List<IPNode> list) {
	list.sort(IP_COMPARATOR);
    }

    private static class IPNode {

	IP ip;

	int size;

	double compressSize;

	IPNode[] bestSubnets;

	@Override
	public String toString() {
	    String ans = "";
	    ans += ip.toString() + " " + size + " " + compressSize + " nets=" + Arrays.toString(bestSubnets);
	    for (IPNode subnet : bestSubnets) {
		ans += subnet.ip + ", ";
	    }
	    return ans;
	}

	@Override
	public boolean equals(Object o) {
	    if (o == this) {
		return true;
	    } else if (o == null) {
		return false;
	    } else if (!(o instanceof IPNode)) {
		return false;
	    }

	    IPNode other = (IPNode) o;
	    return ip.equals(other.ip);
	}

    }

}
