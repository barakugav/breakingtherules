package breakingtherules.dao.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Utility;

/**
 * The CSVParser used to read and write CSV file to and from hits.
 */
public class CSVParser {

    /**
     * IDs for attributes used by this parser
     */
    public static final int SOURCE = 0;
    public static final int DESCRIPTION = 1;
    public static final int SERVICE_PROTOCOL = 2;
    public static final int SERVICE_PORT = 3;

    /**
     * Default columns types
     */
    public static final List<Integer> DEFAULT_COLUMNS_TYPES;

    /**
     * Service protocol codes and names
     */
    private static final int UDP_CODE = 0;
    private static final int TCP_CODE = 1;
    private static final String UDP = "UDP";
    private static final String TCP = "TCP";

    static {
	DEFAULT_COLUMNS_TYPES = Arrays.asList(new Integer[] { SOURCE, DESCRIPTION, SERVICE_PORT, SERVICE_PROTOCOL });
    }

    /**
     * Create hit list by job id
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param jobId
     *            id of the job
     * @return list of the hits
     * @throws IOException
     *             if IO errors occurs
     */
    public static List<Hit> fromCSV(List<Integer> columnsTypes, int jobId) throws IOException {
	return fromCSV(columnsTypes, CSVDaoConfig.getHitsFile(jobId));
    }

    /**
     * Create hit list by file path
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param filePath
     *            path to file
     * @return list of hits built from the CSV file
     * @throws IOException
     *             if IO errors occurs
     */
    public static List<Hit> fromCSV(List<Integer> columnsTypes, String filePath) throws IOException {
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
	try {
	    final BufferedReader buffer = reader;
	    return fromCSV(columnsTypes, new Iterator<String>() {

		String line = buffer.readLine();

		@Override
		public boolean hasNext() {
		    return line != null;
		}

		@Override
		public String next() {
		    if (line == null)
			throw new NoSuchElementException();
		    String next = line;
		    try {
			line = buffer.readLine();
		    } catch (IOException e) {
			throw new RuntimeException(e);
		    }
		    return next;
		}
	    });
	} finally {
	    reader.close();
	}
    }

    /**
     * Create it list from string lines iterator
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param lines
     *            string lines iterator
     * @return hits built from the lines
     */
    public static List<Hit> fromCSV(List<Integer> columnsTypes, Iterator<String> lines) {
	if (columnsTypes == null) {
	    throw new IllegalArgumentException("Colomns types list can't be null");
	}
	if (columnsTypes.contains(SERVICE_PORT) ^ columnsTypes.contains(SERVICE_PROTOCOL)) {
	    throw new IllegalArgumentException("Choose service port and service protocol or neither of them");
	}
	if (lines == null) {
	    throw new IllegalArgumentException("Line can't be null!");
	}

	List<Hit> hits = new ArrayList<Hit>();
	int id = 1;
	int lineNumber = 1;

	try {
	    while (lines.hasNext()) {
		String line = lines.next();
		if (line.isEmpty()) {
		    continue;
		}

		List<String> words = Utility.breakToWords(line);
		List<Attribute> attributes = new ArrayList<Attribute>();

		if (columnsTypes.contains(SOURCE)) {
		    String source = words.get(columnsTypes.indexOf(SOURCE));
		    attributes.add(fromCSVSource(source));
		}
		if (columnsTypes.contains(DESCRIPTION)) {
		    String destination = words.get(columnsTypes.indexOf(DESCRIPTION));
		    attributes.add(fromCSVDestination(destination));
		}
		if (columnsTypes.contains(SERVICE_PROTOCOL) && columnsTypes.contains(SERVICE_PORT)) {
		    String protocol = words.get(columnsTypes.indexOf(SERVICE_PROTOCOL));
		    String port = words.get(columnsTypes.indexOf(SERVICE_PORT));
		    attributes.add(fromCSVService(port, protocol));
		}

		hits.add(new Hit(id++, attributes));

		lineNumber++;
	    }
	} catch (CSVParseException e) {
	    throw new CSVParseException("In line " + lineNumber + ": ", e);
	}

	return hits;
    }

    /**
     * Write to a file hits by CSV format
     * 
     * @param columnsTypes
     *            configuration of columns types
     * @param hits
     *            list of the list
     * @param outputPath
     *            path to output file
     * @throws IOException
     *             if IO errors occurs
     */
    public static void toCSV(List<Integer> columnsTypes, List<Hit> hits, String outputPath) throws IOException {
	if (outputPath == null) {
	    throw new IllegalArgumentException("Repo path can't be null");
	}
	File outputFile = new File(outputPath);
	if (!outputFile.canWrite()) {
	    throw new IOException("File can't be written");
	}

	BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
	try {

	    if (columnsTypes == null) {
		throw new IllegalArgumentException("Colomns types list can't be null");
	    }
	    if (columnsTypes.contains(null)) {
		throw new IllegalArgumentException("Colomns types list can't contains nulls");
	    }
	    if (columnsTypes.contains(SERVICE_PORT) ^ columnsTypes.contains(SERVICE_PROTOCOL)) {
		throw new IllegalArgumentException("Choose service port and service protocol or neither of them");
	    }
	    if (hits == null) {
		throw new IllegalArgumentException("Hits list can't be null");
	    }
	    for (Hit hit : hits) {
		if (columnsTypes.contains(SOURCE) && hit.getAttribute(Attribute.SOURCE_TYPE_ID) == null) {
		    throw new IllegalArgumentException("Source attribute is missing in one of the hits");
		}
		if (columnsTypes.contains(DESCRIPTION) && hit.getAttribute(Attribute.DESTINATION_TYPE_ID) == null) {
		    throw new IllegalArgumentException("Destination attribute is missing in one of the hits");
		}
		if (columnsTypes.contains(SOURCE) && hit.getAttribute(Attribute.SOURCE_TYPE_ID) == null) {
		    throw new IllegalArgumentException("Source attribute is missing in one of the hits");
		}
	    }

	    for (Hit hit : hits) {
		if (hit == null) {
		    continue;
		}

		String line = "";
		for (int colomnsType : columnsTypes) {
		    String colomnValue = "";
		    switch (colomnsType) {
		    case SOURCE:
			Source source = (Source) hit.getAttribute(Attribute.SOURCE_TYPE_ID);
			colomnValue = toCSVSource(source);
			break;
		    case DESCRIPTION:
			Destination destination = (Destination) hit.getAttribute(Attribute.DESTINATION_TYPE_ID);
			colomnValue = toCSVDestination(destination);
			break;
		    case SERVICE_PROTOCOL:
			Service service = (Service) hit.getAttribute(Attribute.SERVICE_TYPE_ID);
			colomnValue = toCSVServiceProtocol(service);
			break;
		    case SERVICE_PORT:
			service = (Service) hit.getAttribute(Attribute.SERVICE_TYPE_ID);
			colomnValue = toCSVServicePort(service);
			break;
		    }

		    if (!colomnValue.isEmpty()) {
			line = Utility.addWord(line, colomnValue, true);
		    }
		}

		if (!line.isEmpty()) {
		    writer.write(line);
		    writer.newLine();
		}
	    }
	} finally {
	    writer.close();
	}
    }

    /**
     * Parse CSV string to source
     * 
     * @param source
     *            source string in CSV format
     * @return Source object
     */
    private static Source fromCSVSource(String source) {
	try {
	    return new Source(source);
	} catch (IllegalArgumentException e) {
	    throw new ParseException("Unable to parse source: ", e);
	}
    }

    /**
     * Parse CSV string to destination
     * 
     * @param destination
     *            destination string in CSV format
     * @return Destination object
     */
    private static Destination fromCSVDestination(String destination) {
	try {
	    return new Destination(destination);
	} catch (IllegalArgumentException e) {
	    throw new ParseException("Unable to parse destination: ", e);
	}
    }

    /**
     * Parse CSV string to service
     * 
     * @param port
     *            port string in CSV format
     * @param protocol
     *            protocol string in CSV format
     * @return Service object
     */
    private static Service fromCSVService(String port, String protocol) {
	int portNum, protocolInt;
	try {
	    portNum = Integer.parseInt(port);
	} catch (NumberFormatException e) {
	    throw new CSVParseException("Unable to parse port to integer: ", e);
	}
	try {
	    protocolInt = Integer.parseInt(protocol);
	} catch (NumberFormatException e) {
	    throw new CSVParseException("Unable to parse protocol code to integer: ", e);
	}

	String protocolStr;
	switch (protocolInt) {
	case UDP_CODE:
	    protocolStr = UDP;
	    break;
	case TCP_CODE:
	    protocolStr = TCP;
	    break;
	default:
	    throw new CSVParseException("Unknown protocol number " + protocolInt);
	}

	try {
	    return new Service(protocolStr, portNum);
	} catch (IllegalArgumentException e) {
	    throw new ParseException("Unable to parse service: ", e);
	}
    }

    /**
     * Convert source object to source string in CSV format
     * 
     * @param source
     *            the source
     * @return CSV source string
     */
    private static String toCSVSource(Source source) {
	if (source == null) {
	    throw new CSVParseException("Source doesn't exist");
	}
	return source.getIp().toString();
    }

    /**
     * Convert destination object to destination string in CSV format
     * 
     * @param destination
     *            the destination
     * @return CSV destination string
     */
    private static String toCSVDestination(Destination destination) {
	if (destination == null) {
	    throw new CSVParseException("Destination doesn't exist");
	}
	return destination.getIp().toString();
    }

    /**
     * Convert service object to service protocol string in CSV format
     * 
     * @param service
     *            the service
     * @return CSV service protocol string
     */
    private static String toCSVServiceProtocol(Service service) {
	switch (service.getProtocol()) {
	case UDP:
	    return "" + UDP_CODE;
	case TCP:
	    return "" + TCP_CODE;
	default:
	    throw new CSVParseException("Unknown protocol (" + service.getProtocol() + ")");
	}
    }

    /**
     * Convert service object to service port string in CSV format
     * 
     * @param service
     *            the service
     * @return CSV service port string
     */
    private static String toCSVServicePort(Service service) {
	return "" + service.getPortRangeStart();
    }

}
