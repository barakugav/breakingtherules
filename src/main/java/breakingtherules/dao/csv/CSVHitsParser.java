package breakingtherules.dao.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import breakingtherules.dao.AbstractParser;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.IP;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.util.Utility;

/**
 * Parser used to read and write hits to and from CSV file.
 * <p>
 * Caches can be set to reduce object creations and memory use.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 * @see CSVHitsDao
 */
public class CSVHitsParser extends AbstractParser {

    /**
     * The types of each column in the CSV file.
     */
    private final int[] m_columnsTypes;

    /**
     * The column index of source attribute in the CSV file or -1 if doesn't
     * exist.
     */
    private final int m_sourceIndex;

    /**
     * The column index of destination attribute in the CSV file or -1 if
     * doesn't exist.
     */
    private final int m_destinationIndex;

    /**
     * The column index of service protocol in the CSV file or -1 if doesn't
     * exist.
     */
    private final int m_serviceProtocolIndex;

    /**
     * The column index of service port in the CSV file or -1 if doesn't exist.
     */
    private final int m_servicePortIndex;

    /**
     * Boolean flag indicates if the {@link #m_columnsTypes} configuration
     * contains a {@link #SOURCE_VAL}.
     * <p>
     * This flag is used to determine if the the parser should look for (and
     * parser) source attributes.
     */
    private final boolean m_containsSource;

    /**
     * Boolean flag indicates if the {@link #m_columnsTypes} configuration
     * contains a {@link #DESTINATION_VAL}.
     * <p>
     * This flag is used to determine if the the parser should look for (and
     * parser) destination attributes.
     */
    private final boolean m_containsDestination;

    /**
     * Boolean flag indicates if the {@link #m_columnsTypes} configuration
     * contains a {@link #SERVICE_PROTOCOL_VAL} and {@link #SERVICE_PORT_VAL}.
     * <p>
     * This flag is used to determine if the the parser should look to (and
     * parser) service attributes.
     */
    private final boolean m_containsService;

    /**
     * The number of expected attributes for each parser hit.
     */
    private final int m_numberOfAtts;

    /**
     * The symbol's value for source attribute column.
     */
    private static final int SOURCE_VAL = 0;

    /**
     * The symbol's value for destination attribute column.
     */
    private static final int DESTINATION_VAL = 1;

    /**
     * The symbol's value for service protocol column.
     */
    private static final int SERVICE_PROTOCOL_VAL = 2;

    /**
     * The symbol's value for service port column.
     */
    private static final int SERVICE_PORT_VAL = 3;

    /**
     * The symbol for source attribute column.
     */
    public static final Integer SOURCE = Integer.valueOf(SOURCE_VAL);

    /**
     * The symbol for destination attribute column.
     */
    public static final Integer DESTINATION = Integer.valueOf(DESTINATION_VAL);

    /**
     * The symbol for service protocol column.
     */
    public static final Integer SERVICE_PROTOCOL = Integer.valueOf(SERVICE_PROTOCOL_VAL);

    /**
     * The symbol for service port column.
     */
    public static final Integer SERVICE_PORT = Integer.valueOf(SERVICE_PORT_VAL);

    /**
     * Default columns types.
     */
    public static final List<Integer> DEFAULT_COLUMNS_TYPES;

    static {
	final List<Integer> list = new ArrayList<>();
	list.add(SOURCE);
	list.add(DESTINATION);
	list.add(SERVICE_PORT);
	list.add(SERVICE_PROTOCOL);
	DEFAULT_COLUMNS_TYPES = Collections.unmodifiableList(list);
    }

    /**
     * Construct new CSVParser.
     * <p>
     * Initialize the parser with columns order.
     *
     * @param columnsTypes
     *            type of each column in the CSV file.
     * @throws NullPointerException
     *             if the columns types list is null, or one the elements in it
     *             is null.
     */
    public CSVHitsParser(final List<Integer> columnsTypes) {
	m_columnsTypes = new int[columnsTypes.size()];
	for (int i = m_columnsTypes.length; i-- != 0;)
	    m_columnsTypes[i] = columnsTypes.get(i).intValue();

	m_sourceIndex = columnsTypes.indexOf(SOURCE);
	m_destinationIndex = columnsTypes.indexOf(DESTINATION);
	m_serviceProtocolIndex = columnsTypes.indexOf(SERVICE_PROTOCOL);
	m_servicePortIndex = columnsTypes.indexOf(SERVICE_PORT);

	m_containsSource = m_sourceIndex >= 0;
	m_containsDestination = m_destinationIndex >= 0;
	m_containsService = m_serviceProtocolIndex >= 0 && m_servicePortIndex >= 0;

	m_numberOfAtts = (m_containsSource ? 1 : 0) + (m_containsDestination ? 1 : 0) + (m_containsService ? 1 : 0);
    }

    /**
     * Parse a hit from a string line.
     * <p>
     *
     * @param line
     *            string line.
     * @return hit parsed from the line.
     * @throws CSVParseException
     *             if the line is invalid.
     * @throws NullPointerException
     *             if the line is null.
     */
    public Hit parseHit(final String line) throws CSVParseException {
	final String[] words = Utility.breakToWords(line, Utility.TAB, Utility.SPACE);
	final List<Attribute> atts = new ArrayList<>(m_numberOfAtts);
	try {
	    if (m_containsSource) {
		final String sourceStr = words[m_sourceIndex];
		atts.add(sourceCache != null ? sourceCache.valueOf(sourceStr) : Source.valueOf(sourceStr));
	    }
	    if (m_containsDestination) {
		final String destinationStr = words[m_destinationIndex];
		atts.add(destinationCache != null ? destinationCache.valueOf(destinationStr)
			: Destination.valueOf(destinationStr));
	    }
	    if (m_containsService) {
		final short serviceProtocol = Service.parseProtocolCode(words[m_serviceProtocolIndex]);
		final int servicePort = Service.parsePort(words[m_servicePortIndex]);
		atts.add(serviceCache != null ? serviceCache.valueOf(serviceProtocol, servicePort)
			: Service.valueOf(serviceProtocol, servicePort));
	    }
	} catch (final IllegalArgumentException e) {
	    throw new CSVParseException(e);
	}
	return new Hit(atts);
    }

    /**
     * Parse hit to CSV string line.
     * <p>
     *
     * @param hit
     *            parsed hit.
     * @return CSV string line representation of the hit.
     * @throws CSVParseException
     *             if the hit lack one of the attributes, or any other errors
     *             occurs.
     * @throws NullPointerException
     *             if the hit is null.
     */
    public String toCSV(final Hit hit) throws CSVParseException {
	final StringBuilder builder = new StringBuilder();
	for (final int colomnsType : m_columnsTypes) {
	    String colomnValue;
	    switch (colomnsType) {
	    case SOURCE_VAL:
		final Source source = (Source) hit.getAttribute(AttributeType.SOURCE);
		colomnValue = toCSVSource(source);
		break;
	    case DESTINATION_VAL:
		final Destination destination = (Destination) hit.getAttribute(AttributeType.DESTINATION);
		colomnValue = toCSVDestination(destination);
		break;
	    case SERVICE_PROTOCOL_VAL:
		Service service = (Service) hit.getAttribute(AttributeType.SERVICE);
		colomnValue = toCSVServiceProtocol(service);
		break;
	    case SERVICE_PORT_VAL:
		service = (Service) hit.getAttribute(AttributeType.SERVICE);
		colomnValue = toCSVServicePort(service);
		break;
	    default:
		colomnValue = null;
	    }

	    if (colomnValue != null && !colomnValue.isEmpty())
		Utility.addWord(builder, colomnValue, true);
	}
	return builder.toString();
    }

    /**
     * Parse all hits that are in a file.
     *
     * @param columnsTypes
     *            configuration of columns types
     * @param fileName
     *            name of the file.
     * @return list of hits built from the CSV file
     * @throws IOException
     *             if IO errors occurs
     * @throws CSVParseException
     *             if fails to parse file
     */
    public static Iterable<Hit> parseAllHits(final List<Integer> columnsTypes, final String fileName)
	    throws IOException, CSVParseException {
	return parseHits(columnsTypes, fileName, false);
    }

    /**
     * Parse all unique hits that are in a file.
     *
     * @param columnsTypes
     *            configuration of columns types
     * @param fileName
     *            name of the file.
     * @return all unique hits built from the CSV file
     * @throws IOException
     *             if IO errors occurs
     * @throws CSVParseException
     *             if fails to parse file
     */
    public static Iterable<Hit> parseUniqueHits(final List<Integer> columnsTypes, final String fileName)
	    throws IOException, CSVParseException {
	return parseHits(columnsTypes, fileName, true);
    }

    /**
     * Write to a file hits by CSV format
     *
     * @param columnsTypes
     *            configuration of columns types.
     * @param hits
     *            list of the list.
     * @param outputPath
     *            path to output file.
     * @throws IOException
     *             if IO errors occurs.
     * @throws CSVParseException
     *             if fails to parse hits.
     * @throws NullPointerException
     *             if the columns types list is null, or one of the elements in
     *             it is null.
     */
    public static void toCSV(final List<Integer> columnsTypes, final Iterable<Hit> hits, final String outputPath)
	    throws IOException, CSVParseException {
	final File outputFile = new File(outputPath);
	if (outputFile.exists() && !outputFile.canWrite())
	    throw new IOException("File already exist and can't be over written");

	try (final BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
	    if (columnsTypes.contains(SERVICE_PORT) ^ columnsTypes.contains(SERVICE_PROTOCOL))
		System.err.println("Warning: Choose service port and service protocol or neither of them! "
			+ "Will procede and write hits to file, but the hits' service attribute won't be readable.");
	    for (final Hit hit : hits) {
		if (columnsTypes.contains(SOURCE) && hit.getAttribute(AttributeType.SOURCE) == null)
		    throw new IllegalArgumentException("Source attribute is missing in one of the hits");
		if (columnsTypes.contains(DESTINATION) && hit.getAttribute(AttributeType.DESTINATION) == null)
		    throw new IllegalArgumentException("Destination attribute is missing in one of the hits");
		if (columnsTypes.contains(SERVICE_PROTOCOL) && columnsTypes.contains(SERVICE_PORT)
			&& hit.getAttribute(AttributeType.SERVICE) == null)
		    throw new IllegalArgumentException("Source attribute is missing in one of the hits");
	    }

	    final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	    for (final Hit hit : hits)
		if (hit != null) {
		    final String line = parser.toCSV(hit);
		    if (!line.isEmpty()) {
			writer.write(line);
			writer.newLine();
		    }
		}
	}
    }

    private static List<Hit> parseHits(final List<Integer> columnsTypes, final String fileName, final boolean unique)
	    throws IOException, CSVParseException {
	final File file = new File(fileName);
	if (!file.exists())
	    throw new FileNotFoundException("File not found: " + fileName);
	if (!file.canRead())
	    throw new IOException("File read is not permitted!");

	if (columnsTypes.contains(SERVICE_PORT) ^ columnsTypes.contains(SERVICE_PROTOCOL))
	    System.err.println("Warning: Choose service port and service protocol or neither of them! "
		    + "Ignoring both for now.");

	final CSVHitsParser parser = new CSVHitsParser(columnsTypes);
	final IP.Cache ipsCache = new IP.Cache();
	parser.setSourceCache(new Source.Cache(ipsCache));
	parser.setDestinationCache(new Destination.Cache(ipsCache));
	parser.setServiceCache(new Service.Cache());

	final List<Hit> hits = new ArrayList<>();
	try (final BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
	    if (unique) {
		final Set<String> uniqueLines = Collections.newSetFromMap(new ConcurrentHashMap<>());

		// Read first lines to estimate number of lines in the file.
		double sumOfLinesSizes = 0;
		final int NUMBER_OF_TESTED_LINES = 10000;
		int i = 0;
		for (String line; i < NUMBER_OF_TESTED_LINES && (line = reader.readLine()) != null; i++) {
		    uniqueLines.add(line);
		    sumOfLinesSizes += line.length(); // UTF-8
		}
		final double averageLineSize = sumOfLinesSizes / NUMBER_OF_TESTED_LINES;
		final long fileSize = Files.size(file.toPath());
		final long estimatedNumberOfLines = (long) ((fileSize - sumOfLinesSizes) / averageLineSize * 1.05);

		// Create spliterator over the file (using the lines number
		// estimation)
		final Spliterator<String> spliterator = Spliterators.spliterator(new Iterator<String>() {

		    String nextLine = null;

		    @Override
		    public boolean hasNext() {
			if (nextLine != null)
			    return true;
			try {
			    nextLine = reader.readLine();
			    return nextLine != null;
			} catch (final IOException e) {
			    throw new UncheckedIOException(e);
			}
		    }

		    @Override
		    public String next() {
			if (nextLine != null || hasNext()) {
			    final String line = nextLine;
			    nextLine = null;
			    return line;
			}
			throw new NoSuchElementException();
		    }
		}, estimatedNumberOfLines, Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.SIZED);

		// Set up executor service
		final ExecutorService executorService = Executors
			.newFixedThreadPool(Math.min(16, Runtime.getRuntime().availableProcessors()));

		// Allocate all jobs
		final Consumer<String> job = line -> uniqueLines.add(line);
		for (Spliterator<String> s; (s = spliterator.trySplit()) != null;) {
		    final Spliterator<String> split = s;
		    executorService.execute(() -> split.forEachRemaining(job));
		}
		spliterator.forEachRemaining(job);

		// Wait to all jobs to finish
		executorService.shutdown();

		// Parse all unique lines
		for (final String line : uniqueLines)
		    hits.add(parser.parseHit(line));

	    } else
		for (String line; (line = reader.readLine()) != null;)
		    if (!line.isEmpty())
			hits.add(parser.parseHit(line));
	}
	return hits;

    }

    /**
     * Convert destination object to destination string in CSV format
     *
     * @param destination
     *            the destination
     * @return CSV destination string
     * @throws CSVParseException
     *             if destination is null
     */
    private static String toCSVDestination(final Destination destination) throws CSVParseException {
	if (destination == null)
	    throw new CSVParseException("Destination doesn't exist");
	return destination.toString();
    }

    /**
     * Convert service object to service port string in CSV format
     *
     * @param service
     *            the service
     * @return CSV service port string
     *
     * @throws CSVParseException
     *             if the service is null.
     */
    private static String toCSVServicePort(final Service service) throws CSVParseException {
	if (service == null)
	    throw new CSVParseException("Service doesn't exist");
	return Integer.toString(service.getPortRangeStart());
    }

    /**
     * Convert service object to service protocol string in CSV format
     *
     * @param service
     *            the service
     * @return CSV service protocol string
     * @throws CSVParseException
     *             if the service is null.
     */
    private static String toCSVServiceProtocol(final Service service) throws CSVParseException {
	if (service == null)
	    throw new CSVParseException("Service doesn't exist");
	return Integer.toString(service.getProtocolCode());
    }

    /**
     * Convert source object to source string in CSV format
     *
     * @param source
     *            the source
     * @return CSV source string
     * @throws CSVParseException
     *             if source is null
     */
    private static String toCSVSource(final Source source) throws CSVParseException {
	if (source == null)
	    throw new CSVParseException("Source doesn't exist");
	return source.toString();
    }

}
