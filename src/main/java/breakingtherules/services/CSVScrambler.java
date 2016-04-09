package breakingtherules.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import breakingtherules.dao.csv.CSVParser;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
import breakingtherules.utilities.Pair;
import breakingtherules.utilities.TextPrinter;
import breakingtherules.utilities.Utility;

/**
 * The CSVScrambler is a tool used to scramble hits by changing their IPs
 * attribute (source, destination)
 */
public class CSVScrambler implements Runnable {

    /**
     * Input iterator of string lines
     */
    private Iterator<String> m_input;

    /**
     * Output file path
     */
    private String m_outputFile;

    /**
     * Configuration parameter of the indexes of the attributes in each hit line
     */
    private List<Integer> m_columnsTypes;

    /**
     * Constructor
     */
    public CSVScrambler() {
	m_input = null;
	m_outputFile = null;
	m_columnsTypes = null;
    }

    /**
     * Set input by file
     * 
     * @param filePath
     *            path to file
     * @throws IOException
     *             if fails to read from file
     */
    public void setInputFromFile(String filePath) throws IOException {
	if (filePath == null) {
	    throw new IllegalArgumentException("File path can't be null");
	}
	File repoFile = new File(filePath);
	if (!repoFile.exists()) {
	    throw new FileNotFoundException("File not found: " + filePath);
	} else if (!repoFile.canRead()) {
	    throw new IOException("File read is not permitted!");
	}

	BufferedReader reader = new BufferedReader(new FileReader(repoFile));
	List<String> lines = new ArrayList<String>();
	String line;
	while ((line = reader.readLine()) != null) {
	    lines.add(line);
	}
	reader.close();

	setInput(lines.iterator());
    }

    /**
     * Set the input iterator for this scrambler
     * 
     * @param input
     *            input lines iterator
     */
    public void setInput(Iterator<String> input) {
	Objects.requireNonNull(input, "Input can't be null!");
	m_input = input;
    }

    /**
     * Set the output path
     * 
     * @param path
     *            path to output file
     */
    public void setOutputFile(String path) {
	Objects.requireNonNull(path, "Output path can't be null!");
	m_outputFile = path;
    }

    /**
     * Set the columns types of the CSV file
     * 
     * @param columnsTypes
     *            configuration of the CSV file
     */
    public void setcolumnsTypes(List<Integer> columnsTypes) {
	Objects.requireNonNull(columnsTypes, "columns types can't be null!");
	m_columnsTypes = columnsTypes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
	if (m_input == null) {
	    throw new IllegalStateException("Input wasn't set!");
	} else if (m_outputFile == null) {
	    throw new IllegalStateException("Output file wasn't set!");
	} else if (m_columnsTypes == null) {
	    throw new IllegalStateException("columns typs wasn't set!");
	}

	try {
	    List<Hit> hits = CSVParser.fromCSV(m_columnsTypes, m_input);

	    // Scramble by source
	    Node sourceTree = buildTree(hits, Attribute.SOURCE_TYPE_ID);
	    scrambleTree(sourceTree);
	    hits = rebuildHits(sourceTree, Attribute.SOURCE_TYPE_ID);

	    // Scramble by destination
	    Node destinationTree = buildTree(hits, Attribute.DESTINATION_TYPE_ID);
	    scrambleTree(destinationTree);
	    hits = rebuildHits(destinationTree, Attribute.DESTINATION_TYPE_ID);

	    Collections.shuffle(hits); // Because why not

	    CSVParser.toCSV(m_columnsTypes, hits, m_outputFile);

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Run the scrambler from command line
     */
    public static void main(String[] args) {
	CSVScramblerRunner.run(args);
    }

    /**
     * The CSVScramblerRunner is a class used to run the CSVScrambler with the
     * CMD flags. Used by {@link CSVScrambler#main(String[])}
     */
    public static class CSVScramblerRunner implements Runnable {

	/**
	 * Arguments given to this runner
	 */
	private String[] m_args;

	/**
	 * All flags used as input to this runner
	 */
	public static final String HELP_FLAG = "--help";
	public static final String HELP_FLAG_SHORT = "-h";
	public static final String INPUT_FLAG = "--input";
	public static final String INPUT_FLAG_SHORT = "-i";
	public static final String OUTPUT_FLAG = "--output";
	public static final String OUTPUT_FLAG_SHORT = "-o";
	public static final String SOURCE_FLAG = "--source";
	public static final String SOURCE_FLAG_SHORT = "-s";
	public static final String DESTINATION_FLAG = "--destination";
	public static final String DESTINATION_FLAG_SHORT = "-d";
	public static final String SERVICE_PROTOCOL_FLAG = "--service-protocol";
	public static final String SERVICE_PROTOCOL_FLAG_SHORT = "-spr";
	public static final String SERVICE_PORT_FLAG = "--service-port";
	public static final String SERVICE_PORT_FLAG_SHORT = "-spo";

	/**
	 * Constructor
	 */
	public CSVScramblerRunner() {
	    m_args = null;
	}

	/**
	 * Set the arguments for this runner
	 * 
	 * @param args
	 *            arguments for runner
	 */
	public void setArgs(String[] args) {
	    this.m_args = args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
	    if (m_args == null) {
		m_args = new String[0];
	    }
	    run(m_args);
	}

	/**
	 * Run and activate a CSVScrambler and configured it by the arguments
	 * 
	 * @param args
	 *            array of flags and configurations
	 */
	public static void run(String args[]) {
	    try {
		TextPrinter printer = new TextPrinter();
		CSVScrambler scrambler = null;
		boolean success = true;

		List<String> argsList = Arrays.asList(args);
		if (args.length == 0) {
		    printer.println("No arguments given. Use " + HELP_FLAG + " for help");
		} else if (argsList.contains(HELP_FLAG) || argsList.contains(HELP_FLAG_SHORT)) {
		    helpMessage(printer);
		} else {

		    Map<String, String> flags = new HashMap<String, String>();

		    // Read flags
		    if (success) {
			success &= readFlags(args, flags, printer);
		    }

		    // Check flags
		    if (success) {
			success &= checkFlags(flags, printer);
		    }

		    // Minimize flags
		    if (success) {
			success &= minimizeFlags(flags, printer);
		    }

		    // Parse flags and prepare to run
		    if (success) {
			scrambler = parseFlags(flags);
			printer.println("Start scramblering...");
		    }

		    if (!success) {
			printer.println("Use " + HELP_FLAG + " for help");
		    }
		}

		if (success && scrambler != null) {
		    scrambler.run();
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}

	/**
	 * Append the builder with the help message
	 * 
	 * @param printer
	 *            text printer used to print a message to the user
	 */
	private static void helpMessage(TextPrinter printer) {
	    String title = "========= CSV Scrambler =========";

	    String message = "The CSV Scrambler is a tool used to scramble IPs of hits from and to CSV files.";

	    String flags = "Arguments flags:";

	    String inputFlag = INPUT_FLAG_SHORT + " " + INPUT_FLAG + " <input file>";
	    String outputFlag = OUTPUT_FLAG_SHORT + " " + OUTPUT_FLAG + " <output file>";
	    String sourceFlag = SOURCE_FLAG_SHORT + " " + SOURCE_FLAG + " <column number>";
	    String destinationFlag = DESTINATION_FLAG_SHORT + " " + DESTINATION_FLAG + " <column number>";
	    String serviceProtocolFlag = SERVICE_PROTOCOL_FLAG_SHORT + " " + SERVICE_PROTOCOL_FLAG + " <column number>";
	    String servicePortFlag = SERVICE_PORT_FLAG_SHORT + " " + SERVICE_PORT_FLAG + " <column number>";

	    String inputInfo = "input file for scrambler";
	    String outputInfo = "output file for scrambler, optinal, input file will be overriden if not provided";
	    String sourceInfo = "column number in the input file of source";
	    String destinationInfo = "column number in the input file of destination";
	    String serviceProtocolInfo = "column number in the input file of service protocol code";
	    String servicePortInfo = "column number in the input file of service port";

	    String example1 = "For example: " + INPUT_FLAG + " input.csv " + OUTPUT_FLAG + " output.csv " + SOURCE_FLAG
		    + " 0 " + DESTINATION_FLAG + " 1 " + SERVICE_PROTOCOL_FLAG + " 2 " + SERVICE_PORT_FLAG + " 3";
	    String example2 = "Or shorter: " + INPUT_FLAG_SHORT + " input.csv " + OUTPUT_FLAG_SHORT + " output.csv "
		    + SOURCE_FLAG_SHORT + " 0 " + DESTINATION_FLAG_SHORT + " 1 " + SERVICE_PROTOCOL_FLAG_SHORT + " 2 "
		    + SERVICE_PORT_FLAG_SHORT + " 3";

	    printer.println();
	    printer.println(title);
	    printer.println();
	    printer.println(message);
	    printer.println();
	    printer.println(flags);
	    printer.println();
	    printer.println(inputFlag);
	    printer.printIndentedln(inputInfo);
	    printer.println();
	    printer.println(outputFlag);
	    printer.printIndentedln(outputInfo);
	    printer.println();
	    printer.println(sourceFlag);
	    printer.printIndentedln(sourceInfo);
	    printer.println();
	    printer.println(destinationFlag);
	    printer.printIndentedln(destinationInfo);
	    printer.println();
	    printer.println(serviceProtocolFlag);
	    printer.printIndentedln(serviceProtocolInfo);
	    printer.println();
	    printer.println(servicePortFlag);
	    printer.printIndentedln(servicePortInfo);
	    printer.println();
	    printer.println(example1);
	    printer.println(example2);
	}

	/**
	 * Read all flags from args and fill the map with them
	 * 
	 * @param args
	 *            runner arguments
	 * @param flags
	 *            map of the flags, filled by this method
	 * @param printer
	 *            text printer used to print a message to the user
	 * @return true if no error detected
	 */
	private static boolean readFlags(String[] args, Map<String, String> flags, TextPrinter printer) {
	    boolean success = true;
	    String lastFlag = null;
	    for (String arg : args) {
		if (arg.startsWith("-")) {
		    if (flags.containsKey(arg)) {
			printer.println("More than one flags of type " + arg);
			success = false;
		    }
		    if (lastFlag != null) {
			printer.println("Expected value for flag " + lastFlag);
			success = false;
		    }
		    lastFlag = arg;
		} else {
		    if (lastFlag == null) {
			printer.println("Expected flag before " + arg);
			success = false;
		    } else {
			flags.put(lastFlag, arg);
			lastFlag = null;
		    }
		}
	    }
	    if (lastFlag != null) {
		printer.println("Expected value for flag " + lastFlag);
		success = false;
	    }
	    return success;
	}

	/**
	 * Operate a valid check on the flags
	 * 
	 * @param flags
	 *            the flags map
	 * @param printer
	 *            text printer used to print a message to the user
	 * @return true if no error detected
	 */
	private static boolean checkFlags(Map<String, String> flags, TextPrinter printer) {
	    boolean success = true;

	    // Input flag
	    if (!flags.containsKey(INPUT_FLAG) && !flags.containsKey(INPUT_FLAG_SHORT)) {
		printer.println("Arguments doesn't contains input file");
		success = false;
	    } else if (flags.containsKey(INPUT_FLAG) && flags.containsKey(INPUT_FLAG_SHORT)) {
		printer.println("Arguments contains 2 input files");
		success = false;
	    }

	    // Output flag
	    if (!flags.containsKey(OUTPUT_FLAG) && !flags.containsKey(OUTPUT_FLAG_SHORT)) {
		String message = "Arguments doesn't contains output file"
			+ (success ? ", using the input file as output" : "");
		printer.println(message);
	    } else if (flags.containsKey(OUTPUT_FLAG) && flags.containsKey(OUTPUT_FLAG_SHORT)) {
		printer.println("Arguments contains 2 output files");
		success = false;
	    }

	    // Attributes columns flags
	    if (flags.containsKey(SOURCE_FLAG) && flags.containsKey(SOURCE_FLAG_SHORT)) {
		printer.println("Arguments contains 2 source flags");
		success = false;
	    }
	    if (flags.containsKey(DESTINATION_FLAG) && flags.containsKey(DESTINATION_FLAG_SHORT)) {
		printer.println("Arguments contains 2 destination flags");
		success = false;
	    }
	    if (flags.containsKey(SERVICE_PROTOCOL_FLAG) && flags.containsKey(SERVICE_PROTOCOL_FLAG_SHORT)) {
		printer.println("Arguments contains 2 service ");
		success = false;
	    }
	    if (flags.containsKey(SERVICE_PORT_FLAG) && flags.containsKey(SERVICE_PORT_FLAG_SHORT)) {
		printer.println("Arguments contains 2 service port flags");
		success = false;
	    }

	    return success;
	}

	/**
	 * Minimize the flags and discard short flags, enforce using only full
	 * flags names
	 * 
	 * @param flags
	 *            current flags map
	 * @param printer
	 *            text printer used to print a message to the user
	 * @return if no error detected
	 */
	private static boolean minimizeFlags(Map<String, String> flags, TextPrinter printer) {
	    boolean success = true;

	    Map<String, String> minimizedFlags = new HashMap<String, String>();
	    for (String flag : flags.keySet()) {
		switch (flag) {
		case INPUT_FLAG:
		case INPUT_FLAG_SHORT:
		    minimizedFlags.put(INPUT_FLAG, flags.get(flag));
		    break;
		case OUTPUT_FLAG:
		case OUTPUT_FLAG_SHORT:
		    minimizedFlags.put(OUTPUT_FLAG, flags.get(flag));
		    break;
		case SOURCE_FLAG:
		case SOURCE_FLAG_SHORT:
		    minimizedFlags.put(SOURCE_FLAG, flags.get(flag));
		    break;
		case DESTINATION_FLAG:
		case DESTINATION_FLAG_SHORT:
		    minimizedFlags.put(DESTINATION_FLAG, flags.get(flag));
		    break;
		case SERVICE_PROTOCOL_FLAG:
		case SERVICE_PROTOCOL_FLAG_SHORT:
		    minimizedFlags.put(SERVICE_PROTOCOL_FLAG, flags.get(flag));
		    break;
		case SERVICE_PORT_FLAG:
		case SERVICE_PORT_FLAG_SHORT:
		    minimizedFlags.put(SERVICE_PORT_FLAG, flags.get(flag));
		    break;
		default:
		    printer.println("Unknown flag " + flag);
		    success = false;
		}
	    }
	    flags.clear();
	    flags.putAll(minimizedFlags);

	    return success;
	}

	/**
	 * Parse the flags map and create the CSVScrambler
	 * 
	 * @param flags
	 *            flags map
	 * @return CSVScrambler configurated by the flags
	 * @throws IOException
	 *             if IO error occurs
	 */
	private static CSVScrambler parseFlags(Map<String, String> flags) throws IOException {
	    String input = flags.get(INPUT_FLAG);
	    String output = flags.containsKey(OUTPUT_FLAG) ? flags.get(OUTPUT_FLAG) : input;
	    String source = flags.get(SOURCE_FLAG);
	    String destination = flags.get(DESTINATION_FLAG);
	    String serviceProtocol = flags.get(SERVICE_PROTOCOL_FLAG);
	    String servicePort = flags.get(SERVICE_PORT_FLAG);

	    List<Integer> columnsTypes = new ArrayList<Integer>();
	    if (source != null) {
		Utility.put(columnsTypes, Integer.parseInt(source), CSVParser.SOURCE);
	    }
	    if (destination != null) {
		Utility.put(columnsTypes, Integer.parseInt(destination), CSVParser.DESCRIPTION);
	    }
	    if (serviceProtocol != null) {
		Utility.put(columnsTypes, Integer.parseInt(serviceProtocol), CSVParser.SERVICE_PROTOCOL);
	    }
	    if (servicePort != null) {
		Utility.put(columnsTypes, Integer.parseInt(servicePort), CSVParser.SERVICE_PORT);
	    }

	    CSVScrambler scrambler = new CSVScrambler();
	    scrambler.setInputFromFile(input);
	    scrambler.setOutputFile(output);
	    scrambler.setcolumnsTypes(columnsTypes);
	    return scrambler;
	}

    }

    /**
     * Build node tree from hit list
     * 
     * @param hits
     *            list of the hits
     * @param ipAttId
     *            id of the IP attribute
     * @return root to built tree
     */
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

	while (currentLayer.get(0).ip.getConstPrefixLength() > 0) {
	    List<Node> nextLayer = new ArrayList<Node>();

	    Iterator<Pair<Node, Node>> it = Utility.getDoubleIterator(currentLayer);
	    while (it.hasNext()) {
		Pair<Node, Node> pair = it.next();
		Node parent = buildNode(pair.first, pair.second);
		if (IP.isBrothers(pair.first.ip, pair.second.ip)) {
		    if (it.hasNext()) {
			it.next();
		    }
		}
		nextLayer.add(parent);
	    }

	    // Last node
	    Node last = currentLayer.get(currentLayer.size() - 1);
	    if (currentLayer.size() >= 2) {
		Node beforeLast = currentLayer.get(currentLayer.size() - 2);
		if (!IP.isBrothers(beforeLast.ip, last.ip)) {
		    Node parent = buildNode(last, beforeLast);
		    nextLayer.add(parent);
		}
	    } else {
		Node parent = buildNode(last, null);
		nextLayer.add(parent);
	    }

	    currentLayer = nextLayer;
	}

	return currentLayer.get(0);
    }

    /**
     * Build a single node from it's two children
     * 
     * @param a
     *            first child
     * @param b
     *            second child
     * @return parent node of the children
     */
    private static Node buildNode(Node a, Node b) {
	Node parent = new Node();
	parent.ip = a.ip.getParent();
	boolean isFirstLeftChild = a.ip.getLastBit();
	if (isFirstLeftChild) {
	    parent.left = a;
	} else {
	    parent.right = a;
	}
	if (b != null) {
	    if (IP.isBrothers(a.ip, b.ip)) {
		if (isFirstLeftChild) {
		    parent.right = b;
		} else {
		    parent.left = b;
		}
	    }
	}
	return parent;
    }

    /**
     * Convert list of hits to list of leaf nodes list
     * 
     * @param hits
     *            list of hits
     * @param ipAttId
     *            id of IP attribute
     * @return list of leaf nodes built from hits
     */
    private static List<Node> fromHits(List<Hit> hits, int ipAttId) {
	List<Node> nodes = new ArrayList<Node>();

	for (Hit hit : hits) {
	    Leaf lastNode = nodes.size() == 0 ? null : (Leaf) nodes.get(nodes.size() - 1);
	    if (lastNode != null && lastNode.ip.equals(fromHit(hit, ipAttId).getIp())) {
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

    /**
     * Get the IP attribute from hit
     * 
     * @param hit
     *            the hit
     * @param ipAttId
     *            id of IP attribute
     * @return IPAttribute of the hit
     */
    private static IPAttribute fromHit(Hit hit, int ipAttId) {
	return (IPAttribute) hit.getAttribute(ipAttId);
    }

    /**
     * Scramble a tree
     * 
     * @param root
     *            root of the tree
     */
    private static void scrambleTree(Node root) {
	scrambleTree(root, new Random());
    }

    /**
     * Scramble a tree
     * 
     * @param node
     *            parent node of a sub tree
     * @param rand
     *            random object used to scramble
     */
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

    /**
     * Build hits back from scrambled tree
     * 
     * @param root
     *            root to scrambled tree
     * @param ipAttId
     *            id of IP attribute
     * @return list of hits built from tree
     */
    private static List<Hit> rebuildHits(Node root, int ipAttId) {
	return rebuildHits(root, ipAttId, new boolean[0]);
    }

    /**
     * Build hits back from scrambled sub tree
     * 
     * @param node
     *            parent of sub tree
     * @param ipAttId
     *            id of IP attribute
     * @param prefix
     *            boolean array of right and left decisions until this node
     * @return list of bits built from sub tree
     */
    private static List<Hit> rebuildHits(Node node, int ipAttId, boolean[] prefix) {
	if (node == null) {
	    return new ArrayList<Hit>();
	}
	if (node instanceof Leaf) {
	    Leaf leaf = (Leaf) node;
	    List<Hit> hits = new ArrayList<Hit>(leaf.hits.size());
	    for (Hit hit : leaf.hits) {
		hits.add(buildHit(hit, ipAttId, prefix));
	    }
	    return hits;
	}

	boolean[] leftPrefix = new boolean[prefix.length + 1];
	boolean[] rightPrefix = new boolean[prefix.length + 1];

	System.arraycopy(prefix, 0, leftPrefix, 0, prefix.length);
	System.arraycopy(prefix, 0, rightPrefix, 0, prefix.length);
	leftPrefix[prefix.length] = false;
	rightPrefix[prefix.length] = true;

	List<Hit> left = rebuildHits(node.left, ipAttId, leftPrefix);
	List<Hit> right = rebuildHits(node.right, ipAttId, rightPrefix);
	List<Hit> allHits = new ArrayList<Hit>();
	allHits.addAll(left);
	allHits.addAll(right);
	return allHits;
    }

    /**
     * Build a single hit
     * 
     * @param hit
     *            the current hit
     * @param ipAttId
     *            id of IP attribute
     * @param prefix
     *            boolean array of right and left decisions until the hit's node
     * @return new hit with new IP
     */
    private static Hit buildHit(Hit hit, int ipAttId, boolean[] prefix) {
	Hit newHit = hit.clone();
	IPAttribute attribute = (IPAttribute) newHit.getAttribute(ipAttId);
	IP currentIp = attribute.getIp();
	IP newIp = IP.fromBooleans(prefix, currentIp.getClass());
	attribute.setIp(newIp);
	return newHit;
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

    /**
     * Check the desire attribute
     * 
     * @param attribute
     *            the checked attribute
     */
    private static void attributeCheck(IPAttribute attribute) {
	if (attribute == null) {
	    throw new IllegalArgumentException("One of the hits doesn't have the desire ip attribute");
	}
	if (attribute.getIp().getConstPrefixLength() != attribute.getIp().getMaxLength()) {
	    throw new IllegalArgumentException("One of the hits contains IP of a subnet! (should be specific IP)");
	}
    }

    /**
     * Check a hit if it is null
     * 
     * @param hit
     *            the checked hit
     */
    private static void hitNullCheck(Hit hit) {
	if (hit == null) {
	    throw new IllegalArgumentException("All hits shouldn't be null!");
	}
    }

    /**
     * Check if the expected IP class is equals to the actual IP class
     * 
     * @param expected
     *            expected class
     * @param actual
     *            actual class
     */
    private static void ipClassCheck(String expected, String actual) {
	if (!expected.equals(actual)) {
	    throw new IllegalArgumentException("Not all hits have the same IP type");
	}
    }

    /**
     * The Node object used to build a tree of the IPs from the hits. Then used
     * to scramble the tree by switching the children of the nodes
     */
    private static class Node {

	/**
	 * Constructor
	 */
	public Node() {
	    ip = null;
	    left = null;
	    right = null;
	}

	/**
	 * IP of the node
	 */
	IP ip;

	/**
	 * Left child of the node
	 */
	Node left;

	/**
	 * Right child of the node
	 */
	Node right;

	@Override
	public String toString() {
	    return ip.toString();
	}

    }

    /**
     * The Leaf object extends the Node and holds a list of hits that matched
     * the leaf IP
     */
    private static class Leaf extends Node {

	/**
	 * Constructor
	 */
	public Leaf() {
	    super();
	    hits = new ArrayList<Hit>();
	}

	/**
	 * List of hits matching this leaf's IP
	 */
	List<Hit> hits;

	@Override
	public String toString() {
	    return super.toString() + " " + hits.toString();
	}

    }

}
