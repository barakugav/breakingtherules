package breakingtherules.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import breakingtherules.dao.csv.CSVParseException;
import breakingtherules.dao.csv.CSVParser;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.IPAttribute;
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
    private String m_inputFile;

    /**
     * Output file path
     */
    private String m_outputFile;

    /**
     * Configuration parameter of the indexes of the attributes in each hit line
     */
    private List<Integer> m_columnsTypes;

    /**
     * Construct new CSVScrambler
     */
    public CSVScrambler() {
	m_inputFile = null;
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
    public void setInput(final String filePath) throws IOException {
	// Check that file path is valid
	final File repoFile = new File(filePath);
	if (!repoFile.exists()) {
	    throw new FileNotFoundException("File not found: " + filePath);
	} else if (!repoFile.canRead()) {
	    throw new IOException("File read is not permitted!");
	}

	m_inputFile = filePath;
    }

    /**
     * Set the output path
     * 
     * @param path
     *            path to output file
     */
    public void setOutput(final String path) {
	m_outputFile = Objects.requireNonNull(path, "Output path can't be null!");
    }

    /**
     * Set the columns types of the CSV file
     * 
     * @param columnsTypes
     *            configuration of the CSV file
     */
    public void setColumnsTypes(final List<Integer> columnsTypes) {
	m_columnsTypes = Objects.requireNonNull(columnsTypes, "columns types can't be null!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
	if (m_inputFile == null) {
	    throw new IllegalStateException("Input wasn't set!");
	} else if (m_outputFile == null) {
	    throw new IllegalStateException("Output file wasn't set!");
	} else if (m_columnsTypes == null) {
	    throw new IllegalStateException("columns typs wasn't set!");
	}

	try {
	    run(m_inputFile, m_outputFile, m_columnsTypes);

	} catch (final IOException | CSVParseException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Run the scrambler from command line
     * 
     * @param args
     *            All the arguments for the scrambler,
     *            {@link CSVScrambler.CSVScramblerRunner}
     */
    public static void main(final String[] args) {
	try {
	    CSVScramblerRunner.run(args);
	} catch (final Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * The CSVScramblerRunner is a class used to run the CSVScrambler with the
     * CMD flags. Used by {@link CSVScrambler#main(String[])}.
     */
    public static class CSVScramblerRunner implements Runnable {

	/**
	 * Arguments given to this runner
	 */
	private String[] m_args;

	/**
	 * 'help' flag.
	 */
	public static final String HELP_FLAG = "--help";

	/**
	 * short version of {@link #HELP_FLAG}.
	 */
	public static final String HELP_FLAG_SHORT = "-h";

	/**
	 * 'input' flag.
	 */
	public static final String INPUT_FLAG = "--input";

	/**
	 * short version of {@link #INPUT_FLAG}.
	 */
	public static final String INPUT_FLAG_SHORT = "-i";

	/**
	 * 'output' flag.
	 */
	public static final String OUTPUT_FLAG = "--output";

	/**
	 * short version of {@link #OUTPUT_FLAG}.
	 */
	public static final String OUTPUT_FLAG_SHORT = "-o";

	/**
	 * 'source column index' flag.
	 */
	public static final String SOURCE_FLAG = "--source";

	/**
	 * short version of {@link #SOURCE_FLAG}.
	 */
	public static final String SOURCE_FLAG_SHORT = "-s";

	/**
	 * 'destination column index' flag.
	 */
	public static final String DESTINATION_FLAG = "--destination";

	/**
	 * short version of {@link #DESTINATION_FLAG}.
	 */
	public static final String DESTINATION_FLAG_SHORT = "-d";

	/**
	 * 'service protocol code column index' flag.
	 */
	public static final String SERVICE_PROTOCOL_FLAG = "--service-protocol";

	/**
	 * short version of {@link #SERVICE_PROTOCOL_FLAG}.
	 */
	public static final String SERVICE_PROTOCOL_FLAG_SHORT = "-spr";

	/**
	 * 'service port column index' flag.
	 */
	public static final String SERVICE_PORT_FLAG = "--service-port";

	/**
	 * short version of {@link #SERVICE_PORT_FLAG}.
	 */
	public static final String SERVICE_PORT_FLAG_SHORT = "-spo";

	/**
	 * Construct new CSVScramblerRunner with no args
	 */
	public CSVScramblerRunner() {
	    m_args = null;
	}

	/**
	 * Set the arguments for this runner
	 * 
	 * @param args
	 *            arguments for runner. Null is allowed to clean the args.
	 */
	public void setArgs(final String[] args) {
	    this.m_args = args;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
	    if (m_args == null) {
		m_args = new String[0];
	    }
	    try {
		run(m_args);
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}

	/**
	 * Run and activate a CSVScrambler and configured it by the arguments
	 * 
	 * @param args
	 *            array of flags and configurations
	 * @throws IOException
	 *             if any I/O errors occurs
	 */
	public static void run(final String args[]) throws IOException {
	    final TextPrinter printer = new TextPrinter();
	    final Map<String, String> flags = new HashMap<>();
	    CSVScrambler scrambler = null;
	    boolean success = true;

	    final List<String> argsList = Arrays.asList(args);
	    if (args.length == 0) {
		printer.println("No arguments given. Use " + HELP_FLAG + " for help");
	    } else if (argsList.contains(HELP_FLAG) || argsList.contains(HELP_FLAG_SHORT)) {
		helpMessage(printer);
	    } else {

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
		}
	    }

	    if (!success) {
		printer.println("Use " + HELP_FLAG + " for help");
	    }
	    if (success && scrambler != null) {
		printer.println("Start scramblering...");
		scrambler.run();
		printer.println("Finished scramblering!");
	    }
	}

	/**
	 * Append the builder with the help message
	 * 
	 * @param printer
	 *            text printer used to print a message to the user
	 */
	private static void helpMessage(final TextPrinter printer) {
	    String title = "========= CSV Scrambler =========";

	    String message = "The CSV Scrambler is a tool used to scramble IPs of hits from and to CSV files.";

	    String flags = "Arguments flags:";

	    String inputFlag = INPUT_FLAG_SHORT + " " + INPUT_FLAG + " <input file>";
	    String outputFlag = OUTPUT_FLAG_SHORT + " " + OUTPUT_FLAG + " <output file>";
	    String sourceFlag = SOURCE_FLAG_SHORT + " " + SOURCE_FLAG + " <column number>";
	    String destinationFlag = DESTINATION_FLAG_SHORT + " " + DESTINATION_FLAG + " <column number>";
	    String serviceProtocolFlag = SERVICE_PROTOCOL_FLAG_SHORT + " " + SERVICE_PROTOCOL_FLAG + " <column number>";
	    String servicePortFlag = SERVICE_PORT_FLAG_SHORT + " " + SERVICE_PORT_FLAG + " <column number>";

	    String inputInfo = "Input file for scrambler";
	    String outputInfo = "Output file for scrambler. Optinal, input file will be overriden if not provided";
	    String sourceInfo = "Column number in the input file of source";
	    String destinationInfo = "Column number in the input file of destination";
	    String serviceProtocolInfo = "Column number in the input file of service protocol code";
	    String servicePortInfo = "Column number in the input file of service port";

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
	private static boolean readFlags(final String[] args, final Map<String, String> flags,
		final TextPrinter printer) {
	    boolean success = true;
	    String lastFlag = null;
	    for (final String arg : args) {
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
	private static boolean checkFlags(final Map<String, String> flags, final TextPrinter printer) {
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
		final String message = "Arguments doesn't contains output file"
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
	private static boolean minimizeFlags(final Map<String, String> flags, final TextPrinter printer) {
	    boolean success = true;

	    final Map<String, String> minimizedFlags = new HashMap<>();
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
	    if (!flags.containsKey(OUTPUT_FLAG)) {
		// output wan't set - using input file as output
		flags.put(OUTPUT_FLAG, flags.get(INPUT_FLAG));
	    }

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
	private static CSVScrambler parseFlags(final Map<String, String> flags) throws IOException {
	    final String input = flags.get(INPUT_FLAG);
	    final String output = flags.containsKey(OUTPUT_FLAG) ? flags.get(OUTPUT_FLAG) : input;
	    final String source = flags.get(SOURCE_FLAG);
	    final String destination = flags.get(DESTINATION_FLAG);
	    final String serviceProtocol = flags.get(SERVICE_PROTOCOL_FLAG);
	    final String servicePort = flags.get(SERVICE_PORT_FLAG);

	    final List<Integer> columnsTypes = new ArrayList<>();
	    if (source != null) {
		Utility.put(columnsTypes, Integer.parseInt(source), CSVParser.SOURCE);
	    }
	    if (destination != null) {
		Utility.put(columnsTypes, Integer.parseInt(destination), CSVParser.DESTINATION);
	    }
	    if (serviceProtocol != null) {
		Utility.put(columnsTypes, Integer.parseInt(serviceProtocol), CSVParser.SERVICE_PROTOCOL);
	    }
	    if (servicePort != null) {
		Utility.put(columnsTypes, Integer.parseInt(servicePort), CSVParser.SERVICE_PORT);
	    }

	    final CSVScrambler scrambler = new CSVScrambler();
	    scrambler.setInput(input);
	    scrambler.setOutput(output);
	    scrambler.setColumnsTypes(columnsTypes);
	    return scrambler;
	}

    }

    /**
     * Run the scrambler
     * 
     * @param inputFile
     *            path to input file
     * @param outputFile
     *            path to output path
     * @param columnsTypes
     *            orders of the attributes in the input file
     * @throws IOException
     *             if an I/O errors occur
     * @throws CSVParseException
     *             if fails to parse file
     */
    private static void run(String inputFile, String outputFile, final List<Integer> columnsTypes)
	    throws IOException, CSVParseException {
	Objects.requireNonNull(inputFile);
	Objects.requireNonNull(outputFile);
	Objects.requireNonNull(columnsTypes);

	File tempFile = null;

	try {
	    final Path outputPath = Paths.get(outputFile);
	    final Path tempFilePath = Files.createTempFile("tempCSVScramblerFile", ".csv");
	    tempFile = tempFilePath.toFile();

	    if (columnsTypes.contains(CSVParser.SOURCE)) {
		Node tree = buildTree(inputFile, columnsTypes, Attribute.SOURCE_TYPE_ID);
		scrambleTree(tree);
		mutateHits(inputFile, tempFilePath.toString(), columnsTypes, Attribute.SOURCE_TYPE_ID, tree);

		Files.copy(tempFilePath, outputPath, StandardCopyOption.REPLACE_EXISTING);
		inputFile = outputFile;
	    }
	    if (columnsTypes.contains(CSVParser.DESTINATION)) {
		Node tree = buildTree(inputFile, columnsTypes, Attribute.DESTINATION_TYPE_ID);
		scrambleTree(tree);
		mutateHits(inputFile, tempFilePath.toString(), columnsTypes, Attribute.DESTINATION_TYPE_ID, tree);

		Files.copy(tempFilePath, outputPath, StandardCopyOption.REPLACE_EXISTING);
		inputFile = outputFile;
	    }

	} finally {
	    if (tempFile != null) {
		tempFile.delete();
	    }
	}
    }

    /**
     * Build a tree of the IP attribute of the this in the input file
     * 
     * @param inputFile
     *            path to input file
     * @param columnsTypes
     *            orders of the attributes in the input file
     * @param ipAttId
     *            id of the IP attribute
     * @return root node to the built tree
     * @throws IOException
     *             if an I/O errors occur
     * @throws CSVParseException
     *             if fails to parse file
     */
    private static Node buildTree(final String inputFile, final List<Integer> columnsTypes, final int ipAttId)
	    throws IOException, CSVParseException {
	final Map<IP, Node> existingNodes = new HashMap<>();
	final CSVParser parser = new CSVParser(columnsTypes);

	int lineNumber = 0;
	try (final BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
	    for (String line; (line = reader.readLine()) != null;) {
		lineNumber++;
		final Hit hit = parser.parseHit(line);
		IP ip = ((IPAttribute) hit.getAttribute(ipAttId)).getIp();

		if (existingNodes.containsKey(ip)) {
		    continue;
		}
		Node node = new Node(ip);
		existingNodes.put(node.ip, node);

		while (ip.hasParent()) {
		    ip = ip.getParent();
		    Node parent = existingNodes.get(ip);
		    if (parent != null) {
			parent.attach(node);
			break;
		    }
		    parent = new Node(ip);
		    existingNodes.put(parent.ip, parent);
		    parent.attach(node);
		    node = parent;
		}
	    }
	} catch (final CSVParseException e) {
	    throw new CSVParseException("In line " + lineNumber + ": ", e);
	}

	Node root = null;
	if (!existingNodes.isEmpty()) {
	    root = existingNodes.values().iterator().next();
	    while (root.ip.hasParent()) {
		root = existingNodes.get(root.ip.getParent());
	    }
	}

	return root;

    }

    /**
     * Scramble a tree
     * 
     * @param root
     *            root of the tree
     */
    private static void scrambleTree(final Node root) {
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
    private static void scrambleTree(final Node node, final Random rand) {
	if (node == null) {
	    return;
	}

	// Switch children order in 50% chance
	if (rand.nextBoolean()) {
	    Node temp = node.left;
	    node.left = node.right;
	    node.right = temp;
	}

	// Scramble recursive
	scrambleTree(node.left, rand);
	scrambleTree(node.right, rand);
    }

    /**
     * Update the IP attributes of all hits from the input file and write them
     * to a new file
     * 
     * @param inputFile
     *            path to input file
     * @param outputFile
     *            path to output file
     * @param columnsTypes
     *            orders of the attributes in the input file
     * @param ipAttId
     *            id of the IP attribute
     * @param tree
     *            root node of the scrambled IP attribute tree
     * @throws IOException
     *             if an I/O errors occur
     * @throws CSVParseException
     *             if fails to parse file
     */
    private static void mutateHits(final String inputFile, final String outputFile, final List<Integer> columnsTypes,
	    final int ipAttId, final Node tree) throws IOException, CSVParseException {
	CSVParser parser = new CSVParser(columnsTypes);

	int lineNumber = 0;
	try (final BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
	    try (final Writer writer = new FileWriter(outputFile)) {

		final String lineSeparator = System.lineSeparator();
		for (String line; (line = reader.readLine()) != null;) {
		    lineNumber++;
		    Hit hit = parser.parseHit(line);
		    hit = mutateHit(hit, ipAttId, tree);
		    line = parser.toCSV(hit);
		    writer.append(line + lineSeparator);
		}
	    }
	} catch (final CSVParseException e) {
	    throw new CSVParseException("In line " + lineNumber + ": ", e);
	}
    }

    /**
     * Update one hit to match the scrambled tree
     * 
     * @param hit
     *            the hit
     * @param ipAttId
     *            id of the IP attribute
     * @param tree
     *            root node of the scrambled IP attribute tree
     * @return the hit mutation.
     */
    private static Hit mutateHit(final Hit hit, final int ipAttId, Node tree) {
	IP ip = ((IPAttribute) hit.getAttribute(ipAttId)).getIp();

	final List<Boolean> prefix = new ArrayList<>(ip.getSize());
	while (!tree.ip.equals(ip)) {
	    if (tree.left != null && tree.left.ip.contains(ip)) {
		prefix.add(Boolean.FALSE);
		tree = tree.left;
	    } else {
		prefix.add(Boolean.TRUE);
		tree = tree.right;
	    }
	}
	return mutateHit(hit, ipAttId, prefix);
    }

    /**
     * Update a single hit
     * 
     * @param hit
     *            the current hit
     * @param ipAttId
     *            id of IP attribute
     * @param prefix
     *            boolean array of right and left decisions until the hit's node
     * @return new hit with new IP
     */
    private static Hit mutateHit(final Hit hit, final int ipAttId, final List<Boolean> prefix) {
	final IPAttribute attribute = (IPAttribute) hit.getAttribute(ipAttId);
	final IP currentIp = attribute.getIp();
	final IP newIp = IP.parseIPFromBits(prefix, currentIp.getClass());
	return hit.createMutation(attribute.createMutation(newIp));
    }

    /**
     * The Node object used to build a tree of the IPs from the hits. Then used
     * to scramble the tree by switching the children of the nodes
     */
    private static class Node {

	/**
	 * IP of the node
	 */
	private final IP ip;

	/**
	 * Left child of the node
	 */
	private Node left;

	/**
	 * Right child of the node
	 */
	private Node right;

	/**
	 * Construct new Node
	 * 
	 * @param ip
	 *            ip of the node
	 */
	public Node(final IP ip) {
	    this.ip = ip;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return ip.toString();
	}

	/**
	 * Attach a child to this node.
	 * <p>
	 * This method is <b>NOT SAFE</b> and should be used carefully. The
	 * method doesn't check if the child is actually the child of this node.
	 * 
	 * @param child
	 *            the child of this node to attach
	 */
	private void attach(Node child) {
	    if (child.ip.getLastBit()) {
		left = child;
	    } else {
		right = child;
	    }
	}

    }

}
