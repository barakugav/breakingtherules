package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.UnionList;
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
     * Allows configuration, if the user wants more general rules (high
     * ruleWeight) or more specific rules (low ruleWeight)
     */
    private double m_ruleWeight;

    /**
     * Used when the attribute type if not Source or Destination
     */
    private SimpleAlgorithm m_simpleAlgorithm;

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

    private static final Comparator<Suggestion> SUGGESTION_COMPARATOR = new Comparator<Suggestion>() {

	@Override
	public int compare(Suggestion s1, Suggestion s2) {
	    return s1.getSize() - s2.getSize();
	}
    };

    private static final Comparator<IPNode> IP_COMPARATOR = new Comparator<IPNode>() {

	@Override
	public int compare(IPNode o1, IPNode o2) {
	    return o1.ip.compareTo(o2.ip);
	}
    };

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
    public List<Suggestion> getSuggestions(Iterable<Hit> hits, String attType) {
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

    private List<Suggestion> getSuggestionsDestination(Iterable<Hit> hits) {
	List<IPNode> nodes = asIPNodeList(hits, Attribute.DESTINATION_TYPE_ID);
	List<SubnetSuggestion> subnets = getIPSuggestions(nodes);
	List<Suggestion> suggestions = new ArrayList<>(subnets.size());
	for (SubnetSuggestion subnet : subnets) {
	    suggestions.add(new Suggestion(new Destination(subnet.ip), subnet.size, subnet.score));
	}

	// Sort suggestions from small to big, so reverse list after sort
	suggestions.sort(SUGGESTION_COMPARATOR);
	Collections.reverse(suggestions);

	return Utility.subList(suggestions, 0, NUMBER_OF_SUGGESTIONS);
    }

    private List<Suggestion> getSuggestionsSource(Iterable<Hit> hits) {
	List<IPNode> nodes = asIPNodeList(hits, Attribute.SOURCE_TYPE_ID);
	List<SubnetSuggestion> subnets = getIPSuggestions(nodes);
	List<Suggestion> suggestions = new ArrayList<>();
	for (SubnetSuggestion subnet : subnets) {
	    suggestions.add(new Suggestion(new Source(subnet.ip), subnet.size, subnet.score));
	}

	// Sort suggestions from small to big, so reverse list after sort
	suggestions.sort(SUGGESTION_COMPARATOR);
	Collections.reverse(suggestions);

	return Utility.subList(suggestions, 0, NUMBER_OF_SUGGESTIONS);
    }

    private List<SubnetSuggestion> getIPSuggestions(List<IPNode> nodes) {
	if (nodes == null) {
	    throw new IllegalArgumentException("Nodes list can't be null");
	}
	if (nodes.size() == 0) {
	    return new ArrayList<>();
	}
	if (nodes.size() == 1) {
	    List<SubnetSuggestion> res = new ArrayList<>();
	    res.add(nodes.get(0).toSuggestion());
	    return res;
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

	// # ipA - the IP of node A
	//
	// # ipB - the IP of node B
	//
	// # ipParentA - the parent IP of nodeA's IP
	//
	IP ipA, ipB, ipParentA;

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
	int size, length, index;
	final int sizeTotal;

	// Sort the IPs, ensuring the assumption that if for a node there is a
	// brother, it will be next to it. This assumption will stay for next
	// layers too
	currentLayer = nodes;
	currentLayer.sort(IP_COMPARATOR);

	// Total size of elements (calculated out of loop for performance
	// improvements, and because it is constant value)
	sizeTotal = currentLayer.size();

	// Run until there are only one element in the list (all nodes are sub
	// children of the node)
	while ((length = currentLayer.size() - 1) > 0) {
	    nextLayer = new ArrayList<>((int) ((length + 1) * NEXT_LAYER_FACTOR));

	    // Run over all elements, for each element construct his parent
	    // element for the next layer by checking if his brother exist and
	    // if so - merge them, else construct the parent base only on the
	    // one current node. Run over all elements except the last one
	    // (length = size -1) so we don't get out of bounds when searching
	    // for brother (always in [curenntIndex + 1])
	    index = 0;
	    while (index < length) {
		nodeA = currentLayer.get(index);
		nodeB = currentLayer.get(index + 1);
		ipA = nodeA.ip;
		ipParentA = ipA.getParent();

		parent = new IPNode();
		parent.ip = ipParentA;

		// If nodeA and nodeB are brothers:
		if (IP.isBrothers(ipA, nodeB.ip)) {
		    parent.size = size = nodeA.size + nodeB.size;

		    // size / totalSize
		    probability = size / (double) sizeTotal;

		    // union = size * (log(subnetwork size) +
		    // log(1/probability)) + ruleWeight
		    union = size * (ipParentA.getSubnetBitsNum() + Utility.log2(1 / probability)) + m_ruleWeight;

		    // separated = (nodeA optimal) + (nodeB optimal)
		    separated = nodeA.compressSize + nodeB.compressSize;

		    // choose optimal (minimum) choice between union subnetwork
		    // or separated small subnetworks. using <= prefer less
		    // subnetworks
		    if (union <= separated) {
			// using union subnetwork
			parent.compressSize = union;

			// subnetwork is the parent subnetwork
			parent.bestSubnets = new UnionList<>(parent.toSuggestion());
		    } else {
			// using separated small subnetworks
			parent.compressSize = separated;

			// union the two subnetworks from both child nodes
			parent.bestSubnets = nodeA.bestSubnets.unionTo(nodeB.bestSubnets);
		    }

		    // Used the current node and next node, increase index by 2
		    index++;
		} else {
		    // nodeA and nodeB are not brothers, nodeB is not relevant.
		    // Copy all value from current node to parent node
		    parent.size = nodeA.size;
		    parent.compressSize = nodeA.compressSize;
		    parent.bestSubnets = nodeA.bestSubnets;
		}

		// Add the finished parent node to next layer list
		nextLayer.add(parent);
		index++;
	    }

	    // Check last element in list - if was brother of one before last
	    // meaning no more action is require, else - create his parent node
	    // and copy his properties (no brother for sure)
	    nodeA = currentLayer.get(length - 1);
	    nodeB = currentLayer.get(length);
	    ipB = nodeB.ip;
	    if (!IP.isBrothers(nodeA.ip, ipB)) {
		parent = new IPNode();
		parent.ip = ipB.getParent();
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
	return root.bestSubnets.toList();
    }

    private List<IPNode> asIPNodeList(Iterable<Hit> list, int attTypeId) {
	List<IPNode> allNodes = new ArrayList<>();

	for (Iterator<Hit> it = list.iterator(); it.hasNext();) {
	    Hit hit = it.next();
	    IPNode ipNode = new IPNode();
	    ipNode.compressSize = m_ruleWeight;
	    ipNode.ip = ((IPAttribute) hit.getAttribute(attTypeId)).getIp();
	    ipNode.bestSubnets = new UnionList<>(ipNode.toSuggestion());
	    ipNode.size = 1;
	    allNodes.add(ipNode);
	}

	allNodes.sort(IP_COMPARATOR);

	List<IPNode> uniqueNodes = new ArrayList<>();
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

    private static void checkHits(Iterable<Hit> hits, int attId) {
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
	if (!(0 < DEFAULT_RULE_WIEGHT)) {
	    throw new InternalError("DEFAULT_RULE_WIEGHT should be > 0");
	}
	if (!(0 < NEXT_LAYER_FACTOR && NEXT_LAYER_FACTOR < 1)) {
	    throw new InternalError("NEXT_LAYER_FACTOR should be > 0, < 1");
	}
    }

    private static class IPNode {

	IP ip;

	int size;

	double compressSize;

	UnionList<SubnetSuggestion> bestSubnets;

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

	@Override
	public int hashCode() {
	    return ip.hashCode();
	}

	@Override
	public String toString() {
	    StringBuilder builder = new StringBuilder();
	    builder.append(ip);
	    builder.append(' ');
	    builder.append(size);
	    builder.append(' ');
	    builder.append(compressSize);
	    builder.append(" nets=");
	    if (bestSubnets == null) {
		builder.append("null");
	    } else {
		builder.append('[');
		final String spacer = ", ";
		for (SubnetSuggestion node : bestSubnets) {
		    builder.append(node.ip);
		    builder.append(spacer);
		}
		builder.append(']');
	    }
	    return builder.toString();
	}

	private SubnetSuggestion toSuggestion() {
	    SubnetSuggestion node = new SubnetSuggestion();
	    node.ip = ip;
	    node.size = size;
	    node.score = 1 / compressSize;
	    return node;
	}

    }

    private static class SubnetSuggestion {
	IP ip;
	int size;
	double score;

	@Override
	public String toString() {
	    return ip.toString() + " " + size + " " + score;
	}
    }

}
