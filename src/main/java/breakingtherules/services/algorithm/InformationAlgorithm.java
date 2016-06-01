package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import breakingtherules.dao.HitsDao;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.firewall.IPv4;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
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
    private final SimpleAlgorithm m_simpleAlgorithm;

    // /**
    // * Number of suggestions that returned in each suggestions request
    // */
    // private static final int NUMBER_OF_SUGGESTIONS = 10;

    /**
     * Default value for the ruleWeight parameter
     */
    private static final double DEFAULT_RULE_WIEGHT = 1000;

    /**
     * An estimation to the percentage of nodes in the next layer (out of the
     * current layer length)
     */
    private static final double NEXT_LAYER_FACTOR = 0.75;

    private static final double UNIQUE_LIST_FACTOR = 0.25;

    /**
     * Comparator of suggestions, comparing them by their sizes.
     */
    private static final Comparator<Suggestion> SUGGESTION_SIZE_COMPARATOR = new Comparator<Suggestion>() {

	@Override
	public int compare(final Suggestion s1, final Suggestion s2) {
	    return s1.getSize() - s2.getSize();
	}
    };

    /**
     * Comparator of IPNodes, comparing them by their IPs.
     */
    private static final Comparator<IPNode> IP_COMPARATOR = new Comparator<IPNode>() {

	@Override
	public int compare(final IPNode o1, final IPNode o2) {
	    return o1.ip.compareTo(o2.ip);
	}
    };

    static {
	configCheck();
    }

    /**
     * Construct new Information algorithm with default rule weight
     */
    public InformationAlgorithm() {
	this(DEFAULT_RULE_WIEGHT);
    }

    /**
     * Construct new Information algorithm with specified rule weight
     * 
     * @param ruleWeight
     *            weight of each rule used by this algorithm
     * @throws IllegalArgumentException
     *             if ruleWeight is NaN or negative
     */
    public InformationAlgorithm(final double ruleWeight) {
	m_simpleAlgorithm = new SimpleAlgorithm();
	setRuleWeight(ruleWeight);
    }

    /**
     * Set the rule weight of this algorithm to new one
     * 
     * @param weight
     *            new rule weight value
     * @throws IllegalArgumentException
     *             if weight is NaN or negative
     */
    public void setRuleWeight(final double weight) {
	if (Double.isNaN(weight))
	    throw new IllegalArgumentException("Rule weight can't be NaN");
	if (weight < 0)
	    throw new IllegalArgumentException("Rule weight can't be negative: " + weight);
	m_ruleWeight = weight;
    }

    @Override
    public List<Suggestion> getSuggestions(HitsDao dao, int jobId, List<Rule> rules, Filter filter, String attType,
	    int amount) throws Exception {
	final int attTypeId = Attribute.typeStrToTypeId(attType);
	if (attTypeId == Attribute.UNKOWN_ATTRIBUTE_ID) {
	    throw new IllegalArgumentException("Unkown attribute: " + attType);
	}
	if (attTypeId != Attribute.DESTINATION_TYPE_ID && attTypeId != Attribute.SOURCE_TYPE_ID) {
	    return m_simpleAlgorithm.getSuggestions(dao, jobId, rules, filter, attType, amount);
	}

	Set<Hit> hits = dao.getUnique(jobId, rules, filter);
	List<Suggestion> suggestions;
	switch (attTypeId) {
	case Attribute.DESTINATION_TYPE_ID:
	    suggestions = getSuggestionsDestination(hits, amount);
	    break;
	case Attribute.SOURCE_TYPE_ID:
	    suggestions = getSuggestionsSource(hits, amount);
	    break;
	default:
	    throw new InternalError("Attribute type wasn't destination not source after checking it was one of them.");
	}

	// Clean cache
	IPv4.refreshCache();
	Source.refreshCache();
	Destination.refreshCache();
	Service.refreshCache();

	return suggestions;
    }

    /**
     * Get suggestions for hits about destination attribute
     * 
     * @param hits
     *            iterable object of hits - the input for the suggestions
     * @return list of destination suggestion
     * @throws NullPointerException
     *             if hits are null, or one of the hits are null
     * @throws IllegalArgumentException
     *             if one of the hits doesn't contains destination attribute
     */
    private List<Suggestion> getSuggestionsDestination(final Iterable<Hit> hits, int amount) {
	// Calculate suggestions
	List<SubnetSuggestion> subnets = getIPSuggestions(hits, Attribute.DESTINATION_TYPE_ID);

	final List<Suggestion> suggestions = new ArrayList<>(subnets.size());
	for (final SubnetSuggestion subnet : subnets) {
	    suggestions.add(new Suggestion(Destination.create(subnet.ip), subnet.size, subnet.score));
	}
	subnets = null; // Free memory

	// Sort suggestions from small to big, so reverse list after sort
	suggestions.sort(SUGGESTION_SIZE_COMPARATOR);
	Collections.reverse(suggestions);

	return Utility.subList(suggestions, 0, amount);
    }

    /**
     * Get suggestions for hits about source attribute
     * 
     * @param hits
     *            iterable object of hits - the input for the suggestions
     * @return list of source suggestion
     * @throws NullPointerException
     *             if hits are null, or one of the hits are null
     * @throws IllegalArgumentException
     *             if one of the hits doesn't contains the source attribute
     */
    private List<Suggestion> getSuggestionsSource(final Iterable<Hit> hits, int amount) {
	// Calculate suggestions
	List<SubnetSuggestion> subnets = getIPSuggestions(hits, Attribute.SOURCE_TYPE_ID);

	final List<Suggestion> suggestions = new ArrayList<>(subnets.size());
	for (final SubnetSuggestion subnet : subnets) {
	    suggestions.add(new Suggestion(Source.create(subnet.ip), subnet.size, subnet.score));
	}
	subnets = null; // Free memory

	// Sort suggestions from small to big, so reverse list after sort
	suggestions.sort(SUGGESTION_SIZE_COMPARATOR);
	Collections.reverse(suggestions);

	return Utility.subList(suggestions, 0, amount);
    }

    /**
     * Get suggestions for nodes about their IPs
     * 
     * @param hits
     *            list of hits - input for the suggestion
     * @param attTypeId
     *            type id of the attribute of the suggestions
     * @return list of IP suggestion
     */
    private List<SubnetSuggestion> getIPSuggestions(final Iterable<Hit> hits, final int attTypeId) {
	// Creates lowest layer nodes from hits
	List<IPNode> nodes = toIPNodeList(hits, attTypeId);

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
	// # ruleWeight - weight the algorithm will give to each created rule.
	// If this value is high, less rules will be created.
	//
	int size, length, index;
	final int sizeTotal;
	final double ruleWeight = m_ruleWeight;

	// Sort the IPs, ensuring the assumption that if for a node there is a
	// brother, it will be next to it. This assumption will stay for next
	// layers too.
	currentLayer = nodes;
	nodes = null; // Free memory
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
		if (ipA.isBrother(nodeB.ip)) {
		    parent.size = size = nodeA.size + nodeB.size;

		    // size / totalSize
		    probability = size / (double) sizeTotal;

		    // union = size * (log(subnetwork size) +
		    // log(1/probability)) + ruleWeight
		    union = size * (ipParentA.getSubnetBitsNum() + Utility.log2(1 / probability)) + ruleWeight;

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
			parent.bestSubnets = nodeA.bestSubnets.transferElementsFrom(nodeB.bestSubnets);
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
	    if (!nodeA.ip.isBrother(ipB)) {
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
	return root.bestSubnets.toArrayList();
    }

    /**
     * Create list of IPNodes from iterable of hits
     * 
     * @param hits
     *            iterable object of hits
     * @param ipAttTypeId
     *            id of the IP attribute
     * @return list of IPNodes constructed from the hits
     * @throws NullPointerException
     *             if hits are null, or one of the hits are null
     * @throws IllegalArgumentException
     *             if one of the hits doesn't contains the desire attribute
     */
    private List<IPNode> toIPNodeList(final Iterable<Hit> hits, final int ipAttTypeId) {
	// If hits are collection, init with size, else init with default size
	final int aproxNodesNumber = hits instanceof Collection<?> ? ((Collection<?>) hits).size() : 10;
	final List<IPNode> allNodes = new ArrayList<>(aproxNodesNumber);

	for (final Iterator<Hit> it = hits.iterator(); it.hasNext();) {
	    final Hit hit = it.next();
	    final IPNode ipNode = new IPNode();
	    final IPAttribute att = (IPAttribute) hit.getAttribute(ipAttTypeId);
	    if (att == null) {
		throw new IllegalArgumentException("One of the hits doesn't have the desire attribute");
	    }
	    ipNode.ip = att.getIp();
	    ipNode.compressSize = m_ruleWeight;
	    ipNode.size = 1;
	    ipNode.bestSubnets = new UnionList<>(ipNode.toSuggestion());
	    allNodes.add(ipNode);
	}

	allNodes.sort(IP_COMPARATOR);

	// Init list with approximate size depends on nodes list size
	final ArrayList<IPNode> uniqueNodes = new ArrayList<>((int) (allNodes.size() * UNIQUE_LIST_FACTOR));
	final Iterator<IPNode> it = allNodes.iterator();
	if (it.hasNext()) {
	    IPNode lastNode = it.next();
	    uniqueNodes.add(lastNode);

	    while (it.hasNext()) {
		final IPNode node = it.next();
		if (lastNode.ip.equals(node.ip)) {
		    lastNode.size++;
		} else {
		    uniqueNodes.add(node);
		    lastNode = node;
		}
	    }
	}
	uniqueNodes.trimToSize();
	return uniqueNodes;
    }

    /**
     * Configuration check. Used to check the static final fields of this
     * algorithm.
     * 
     * @throws InternalError
     *             if one of the fiels is not legal
     */
    @SuppressWarnings("unused")
    private static void configCheck() {
	if (Double.isNaN(DEFAULT_RULE_WIEGHT)) {
	    throw new InternalError("DEFAULT_RULE_WEIGHT is Nan");
	}
	if (!(0 < DEFAULT_RULE_WIEGHT)) {
	    throw new InternalError("DEFAULT_RULE_WIEGHT(" + DEFAULT_RULE_WIEGHT + ") should be > 0");
	}
	if (!(0 < NEXT_LAYER_FACTOR && NEXT_LAYER_FACTOR < 1)) {
	    throw new InternalError("NEXT_LAYER_FACTOR(" + NEXT_LAYER_FACTOR + ") should be > 0, < 1");
	}
    }

    /**
     * The IPNode class is node in the Information IPs tree.
     * <p>
     * The node contains the following fields:
     * <ul>
     * <li>ip: IP of the node (if the node is leaf in the tree) or IP of the
     * subnetwork of the node (if the node is a inner node in the tree).</li>
     * <li>size: number of different hits under the ip subnetwork (exactly
     * equals hits count as one, but different hits, even if they have the same
     * IP, count as two).</li>
     * <li>compressSize: value of compression size by the InformationAlgorithm.
     * If this value is high, the node is a strong node.</li>
     * <li>bestSubnets: list of best subnetworks that contained in the IP
     * subnetwork of the node.</li>
     * </ul>
     */
    private static class IPNode {

	private IP ip;

	private int size;

	private double compressSize;

	private UnionList<SubnetSuggestion> bestSubnets;

	@Override
	public boolean equals(final Object o) {
	    if (o == this) {
		return true;
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
	    final StringBuilder builder = new StringBuilder();
	    builder.append(ip);
	    builder.append(" size=");
	    builder.append(size);
	    builder.append(" compressSize=");
	    builder.append(compressSize);
	    builder.append(" nets=");
	    if (bestSubnets == null) {
		builder.append("null");
	    } else {
		builder.append('[');
		final Iterator<SubnetSuggestion> it = bestSubnets.iterator();
		final String spacer = ", ";

		if (it.hasNext()) { // Have at least one elements
		    do {
			final SubnetSuggestion node = it.next();
			builder.append(node.ip);
			if (!it.hasNext())
			    break;
			builder.append(spacer);
		    } while (true);
		}
		builder.append(']');
	    }
	    return builder.toString();
	}

	private SubnetSuggestion toSuggestion() {
	    final SubnetSuggestion suggestion = new SubnetSuggestion();
	    suggestion.ip = ip;
	    suggestion.size = size;
	    suggestion.score = 1 / compressSize;
	    return suggestion;
	}

    }

    private static class SubnetSuggestion {

	private IP ip;

	private int size;

	private double score;

	@Override
	public String toString() {
	    return ip.toString() + " size=" + size + " score=" + score;
	}
    }

}
