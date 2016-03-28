package breakingtherules.services.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.utilities.Utility;

public class InformationAlgorithm implements SuggestionsAlgorithm {

    private static final int INIT_COMPRESS_SIZE = 1;

    private static final double RULE_WIEGHT = 5;

    private static final double NEXT_LAYER_FACTOR = 0.7;

    static {
	configCheck();
    }

    @SuppressWarnings("unused")
    private static void configCheck() {
	if (INIT_COMPRESS_SIZE <= 0)
	    throw new IllegalStateException("INIT_COMPRESS_SIZE should be > 0");
	if (RULE_WIEGHT <= 0)
	    throw new IllegalStateException("RULE_WIEGHT should be > 0");
	if (!(0 < NEXT_LAYER_FACTOR && NEXT_LAYER_FACTOR < 1))
	    throw new IllegalStateException("NEXT_LAYER_FACTOR should be > 0, < 1");
    }

    @Override
    public List<Suggestion> getSuggestions(List<Hit> hits, String attType) {
	hits = Utility.ensureUniqueness(hits);
	int attTypeId = Attribute.typeStrToTypeId(attType);
	if (attTypeId == Attribute.NULL_TYPE_ID) {
	    return null;
	}
	switch (attTypeId) {
	case Attribute.DESTINATION_TYPE_ID:
	    return getSuggestionsDestination(hits);
	case Attribute.SOURCE_TYPE_ID:
	    return getSuggestionsSource(hits);
	default:
	    return new SimpleAlgorithm().getSuggestions(hits, attType);
	}
    }

    private List<Suggestion> getSuggestionsDestination(List<Hit> hits) {
	List<IPNode> nodes = toIpNodeList(hits, Attribute.DESTINATION_TYPE_ID);
	IP[] subnets = getIPSuggestions(nodes);
	List<Suggestion> suggestions = new ArrayList<Suggestion>();
	for (IP subnet : subnets) {
	    suggestions.add(new Suggestion(new Destination(subnet)));
	}
	return suggestions;
    }

    private List<Suggestion> getSuggestionsSource(List<Hit> hits) {
	List<IPNode> nodes = toIpNodeList(hits, Attribute.SOURCE_TYPE_ID);
	IP[] subnets = getIPSuggestions(nodes);
	List<Suggestion> suggestions = new ArrayList<Suggestion>();
	for (IP subnet : subnets) {
	    suggestions.add(new Suggestion(new Destination(subnet)));
	}
	return suggestions;
    }

    private IP[] getIPSuggestions(List<IPNode> nodes) {
	if (nodes == null)
	    throw new IllegalArgumentException("Nodes list can't be null");
	if (nodes.size() == 0)
	    return new IP[0];
	if (nodes.size() == 1)
	    return new IP[] { nodes.get(0).ip };

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

	// # index - the current index of the node in the current layer (run
	// from 0 to list.size() - 2 because next element is always used. last
	// element checked outside the index loop)
	//
	// # size - the number of hits in the constructed parent node
	//
	// # sizeTotal - the total number of hits, used to calculate probability
	// (constant value)
	//
	// # length - bound for index
	//
	int index, size, length;
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
	System.out.println("start");
	while ((length = currentLayer.size() - 1) > 0) {
	    System.out.println(currentLayer);
	    nextLayer = new ArrayList<IPNode>((int) ((length + 1) * NEXT_LAYER_FACTOR));

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
			    + RULE_WIEGHT;

		    // (nodeA optimal) + (nodeB optimal)
		    separated = nodeA.compressSize + nodeB.compressSize;

		    // choose optimal (minimum) choice between union subnetwork
		    // or separated small subnetworks. using <= prefer less
		    // subnetworks
		    if (union <= separated) {
			// using union subnetwork
			parent.compressSize = union;

			// subnetwork is the parent subnetwork
			parent.subnets = new IP[] { ipParentA };
		    } else {
			// using separated small subnetworks
			parent.compressSize = separated;

			// union the two subnetworks from both child nodes
			parent.subnets = new IP[nodeA.subnets.length + nodeB.subnets.length];
			System.arraycopy(nodeA.subnets, 0, parent.subnets, 0, nodeA.subnets.length);
			System.arraycopy(nodeB.subnets, 0, parent.subnets, nodeA.subnets.length, nodeB.subnets.length);
		    }

		    // Used the current node and next node, increase index by 2
		    index += 2;
		} else {
		    // nodeA and nodeB are not brothers, nodeB is not relevant.
		    // Copy all value from current node to parent node
		    parent.size = nodeA.size;
		    parent.compressSize = nodeA.compressSize;
		    parent.subnets = nodeA.subnets;

		    // Used only current node, increase index by 1
		    index += 1;
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
		parent.subnets = nodeB.subnets;
		nextLayer.add(parent);
	    }

	    // current layer is finished, move to next layer
	    currentLayer = nextLayer;
	}
	System.out.println(currentLayer);
	System.out.println("end");

	// list size is 1, the only element is the parent node of all others
	IPNode root = currentLayer.get(0);
	return root.subnets;
    }

    private static List<IPNode> toIpNodeList(List<Hit> list, int attTypeId) {
	List<IPNode> allNodes = new ArrayList<IPNode>(list.size());

	int listSize = list.size();
	for (int i = 0; i < listSize; i++) {
	    Hit hit = list.get(i);
	    IPNode ipNode = new IPNode();
	    ipNode.compressSize = INIT_COMPRESS_SIZE;
	    ipNode.ip = ((IPAttribute) hit.getAttribute(attTypeId)).getIp();
	    System.out.println(ipNode.ip.toString());
	    ipNode.subnets = new IP[] { ipNode.ip };
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

    private static final Comparator<IPNode> COMPARATOR_IP = new Comparator<IPNode>() {

	@Override
	public int compare(IPNode o1, IPNode o2) {
	    return o1.ip.compareTo(o2.ip);
	}
    };

    private static void sortByIP(List<IPNode> list) {
	list.sort(COMPARATOR_IP);
    }

    private static class IPNode {

	IP ip;

	int size;

	double compressSize;

	IP[] subnets;

	@Override
	public String toString() {
	    return ip.toString() + " " + size + " " + compressSize + " nets=" + Arrays.toString(subnets);
	}

	public boolean equals(Object o) {
	    if (o == this) {
		return false;
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
