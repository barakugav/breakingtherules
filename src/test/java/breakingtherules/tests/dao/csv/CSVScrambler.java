package breakingtherules.tests.dao.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import breakingtherules.dao.csv.CSVParser;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.utilities.Pair;
import breakingtherules.utilities.Utility;

public class CSVScrambler implements Runnable {

    private String m_inputFile;

    private String m_outputFile;

    private List<Integer> m_colomnsTypes;

    public CSVScrambler() {
	m_inputFile = null;
	m_outputFile = null;
	m_colomnsTypes = null;
    }

    public void setInputFile(String path) {
	Objects.requireNonNull(path, "Input path can't be null!");
	m_inputFile = path;
    }

    public void setOutputFile(String path) {
	Objects.requireNonNull(path, "Output path can't be null!");
	m_outputFile = path;
    }

    public void setColomnsTypes(List<Integer> colomnsTypes) {
	Objects.requireNonNull(colomnsTypes, "Colomns types can't be null!");
	m_colomnsTypes = colomnsTypes;
    }

    @Override
    public void run() {
	if (m_inputFile == null) {
	    throw new IllegalStateException("Input file wasn't set!");
	} else if (m_outputFile == null) {
	    throw new IllegalStateException("Output file wasn't set!");
	} else if (m_colomnsTypes == null) {
	    throw new IllegalStateException("Colomns typs wasn't set!");
	}

	try {
	    List<Hit> hits = CSVParser.fromCSV(m_colomnsTypes, m_inputFile);

	    // Scramble by source
	    Node sourceTree = buildTree(hits, Attribute.SOURCE_TYPE_ID);
	    scrambleTree(sourceTree);
	    hits = rebuildHits(sourceTree, Attribute.SOURCE_TYPE_ID);

	    // Scramble by destination
	    Node destinationTree = buildTree(hits, Attribute.DESTINATION_TYPE_ID);
	    scrambleTree(destinationTree);
	    hits = rebuildHits(sourceTree, Attribute.DESTINATION_TYPE_ID);

	    CSVParser.toCSV(m_colomnsTypes, hits, m_outputFile);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private static Node buildTree(List<Hit> hits, final int ipAttId) {
	checkHits(hits, ipAttId);
	// After this check, we can assume that the list is valid (as detailed
	// in checkHits documentation) for all our operations

	hits.sort(new Comparator<Hit>() {

	    @Override
	    public int compare(Hit o1, Hit o2) {
		IPAttribute att1 = (IPAttribute) o1.getAttribute(ipAttId);
		IPAttribute att2 = (IPAttribute) o2.getAttribute(ipAttId);
		return att1.compareTo(att2);
	    }
	});

	List<Node> currentLayer = fromHits(hits, ipAttId);

	while (continueLoop(currentLayer)) {
	    List<Node> nextLayer = new ArrayList<Node>();

	    Iterator<Pair<Node, Node>> it = Utility.getDoubleIterator(currentLayer);
	    while (it.hasNext()) {
		Pair<Node, Node> pair = it.next();
		Node parent = new Node();
		parent.ip = pair.first.ip.getParent();
		boolean isFirstLeftChild = pair.first.ip.getBit(pair.first.ip.getConstPrefixLength()) == 0;
		if (isFirstLeftChild) {
		    parent.left = pair.first;
		} else {
		    parent.right = pair.second;
		}
		if (IP.isBrothers(pair.first.ip, pair.second.ip)) {
		    if (isFirstLeftChild) {
			parent.right = pair.second;
		    } else {
			parent.left = pair.second;
		    }
		    it.next();
		}
		nextLayer.add(parent);
	    }

	    currentLayer = nextLayer;
	}

	return currentLayer.get(0);
    }

    private static List<Node> fromHits(List<Hit> hits, int ipAttId) {
	List<Node> nodes = new ArrayList<Node>();

	for (Hit hit : hits) {
	    Leaf lastNode = nodes.size() == 0 ? null : (Leaf) nodes.get(nodes.size() - 1);
	    if (lastNode != null && lastNode.ip.equals(fromHit(hit, ipAttId))) {
		lastNode.hits.add(hit);
	    } else {
		Leaf node = new Leaf();
		node.ip = fromHit(hit, ipAttId).getIp();
		node.left = null;
		node.right = null;
		node.hits.add(hit);
		nodes.add(node);
	    }
	}

	return nodes;
    }

    private static IPAttribute fromHit(Hit hit, int ipAttId) {
	return (IPAttribute) hit.getAttribute(ipAttId);
    }

    private static boolean continueLoop(List<Node> currentLayer) {
	if (currentLayer.size() > 1) {
	    return true;
	}

	Node node = currentLayer.get(0);
	return node.ip.getConstPrefixLength() > 0;
    }

    private static void scrambleTree(Node root) {
	scrambleTree(root, new Random());
    }

    private static void scrambleTree(Node node, Random rand) {
	if (node == null) {
	    return;
	}
	if (node instanceof Leaf) {
	    return;
	}
	if (rand.nextBoolean()) {
	    Node temp = node.left;
	    node.left = node.right;
	    node.right = temp;
	}
	scrambleTree(node.left, rand);
	scrambleTree(node.right, rand);
    }

    private static List<Hit> rebuildHits(Node root, int ipAttId) {
	Hit[] hits = rebuildHits(root, ipAttId, new boolean[0]);
	return Arrays.asList(hits);
    }

    private static Hit[] rebuildHits(Node node, int ipAttId, boolean[] prefix) {
	if (node == null) {
	    return new Hit[0];
	}
	if (node instanceof Leaf) {
	    Leaf leaf = (Leaf) node;
	    Hit[] hits = new Hit[leaf.hits.size()];

	    return hits;
	}

	boolean[] leftPrefix = new boolean[prefix.length + 1];
	boolean[] rightPrefix = new boolean[prefix.length + 1];

	System.arraycopy(prefix, 0, leftPrefix, 0, prefix.length);
	System.arraycopy(prefix, 0, rightPrefix, 0, prefix.length);
	leftPrefix[prefix.length] = false;
	rightPrefix[prefix.length] = true;

	Hit[] left = rebuildHits(node.left, ipAttId, leftPrefix);
	Hit[] right = rebuildHits(node.right, ipAttId, rightPrefix);
	Hit[] hits = new Hit[left.length + right.length];
	System.arraycopy(left, 0, hits, 0, left.length);
	System.arraycopy(right, 0, hits, left.length, right.length);
	return hits;
    }

    /**
     * Check the valid state of the hit list. The list isn't valid if:
     * <ul>
     * <li>List is null</li>
     * <li>One of the hits is null</li>
     * <li>Not every hit have the desire attribute</li>
     * <li>Not every hit have the same IP type (IPv4 or IPv6) of the desire
     * attribute</li>
     * </ul>
     * 
     * @param hits
     *            the hit list
     * @param ipAttId
     *            the desire attribute
     * @throws IllegalArgumentException
     *             if one of the above doesn't enforced
     */
    private static void checkHits(List<Hit> hits, int ipAttId) {
	if (hits == null) {
	    throw new IllegalArgumentException("Hits list can't be null");
	}
	if (hits.isEmpty()) {
	    return;
	}

	Hit firstHit = hits.get(0);
	hitNullCheck(firstHit);
	IPAttribute firstHitAtt = (IPAttribute) firstHit.getAttribute(ipAttId);
	attributeCheck(firstHitAtt);
	// "IPv4" or "IPv6"
	String expectedIpClass = firstHitAtt.getIp().getClass().getName();

	for (Hit hit : hits) {
	    hitNullCheck(hit);
	    IPAttribute att = (IPAttribute) hit.getAttribute(ipAttId);
	    attributeCheck(att);
	    String actualIpClass = att.getIp().getClass().getName();
	    ipClassCheck(expectedIpClass, actualIpClass);
	}
    }

    private static void attributeCheck(IPAttribute attribute) {
	if (attribute == null) {
	    throw new IllegalArgumentException("One of the hits doesn't have the desire ip attribute");
	}
	if (attribute.getIp().getConstPrefixLength() != attribute.getIp().getMaxLength()) {
	    throw new IllegalArgumentException("One of the hits contains IP of a subnet! (should be specific IP)");
	}
    }

    private static void hitNullCheck(Hit hit) {
	if (hit == null) {
	    throw new IllegalArgumentException("All hits shouldn't be null!");
	}
    }

    private static void ipClassCheck(String expected, String actual) {
	if (!expected.equals(actual)) {
	    throw new IllegalArgumentException("Not all hits have the same IP type");
	}
    }

    private static class Node {

	public Node() {
	    ip = null;
	    left = null;
	    right = null;
	}

	IP ip;

	Node left;

	Node right;

    }

    private static class Leaf extends Node {

	public Leaf() {
	    hits = new ArrayList<Hit>();
	}

	List<Hit> hits;

    }

}
