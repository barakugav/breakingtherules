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
import java.util.List;

import breakingtherules.dao.ParseException;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.Utility;

public class CSVParser {

    public static final int SOURCE = 0;
    public static final int DESCRIPTION = 1;
    public static final int SERVICE_PROTOCOL = 2;
    public static final int SERVICE_PORT = 3;

    public static final List<Integer> DEFAULT_COLOMNS_TYPES;

    private static final int UDP_CODE = 0;
    private static final int TCP_CODE = 1;
    private static final String UDP = "UDP";
    private static final String TCP = "TCP";

    static {
	DEFAULT_COLOMNS_TYPES = Arrays.asList(new Integer[] { SOURCE, DESCRIPTION, SERVICE_PORT, SERVICE_PROTOCOL });
    }

    public static List<Hit> fromCSV(List<Integer> colomnsTypes, int jobId) throws IOException {
	return fromCSV(colomnsTypes, CSVDaoConfig.getHitsFile(jobId));
    }

    public static List<Hit> fromCSV(List<Integer> colomnsTypes, String repoPath) throws IOException {
	if (repoPath == null) {
	    throw new IllegalArgumentException("Repo path can't be null");
	}
	File repoFile = new File(repoPath);
	if (!repoFile.exists()) {
	    throw new FileNotFoundException("File not found: " + repoPath);
	} else if (!repoFile.canRead()) {
	    throw new IOException("File read is not permitted!");
	}

	BufferedReader reader = new BufferedReader(new FileReader(repoFile));
	try {
	    return fromCSV(colomnsTypes, reader);
	} finally {
	    reader.close();
	}
    }

    public static List<Hit> fromCSV(List<Integer> colomnsTypes, BufferedReader reader) throws IOException {
	if (colomnsTypes == null) {
	    throw new IllegalArgumentException("Colomns types list can't be null");
	}
	if (colomnsTypes.contains(SERVICE_PORT) ^ colomnsTypes.contains(SERVICE_PROTOCOL)) {
	    throw new IllegalArgumentException("Choose service port and service protocol or neither of them");
	}
	if (reader == null) {
	    throw new IllegalArgumentException("Reader can't be null!");
	}

	List<Hit> hits = new ArrayList<Hit>();
	int id = 1;
	int lineNumber = 1;

	try {
	    String line;
	    while ((line = reader.readLine()) != null) {
		if (line.isEmpty()) {
		    continue;
		}

		List<String> words = Utility.breakToWords(line);
		List<Attribute> attributes = new ArrayList<Attribute>();

		if (colomnsTypes.contains(SOURCE)) {
		    String source = words.get(colomnsTypes.indexOf(SOURCE));
		    attributes.add(fromCSVSource(source));
		}
		if (colomnsTypes.contains(DESCRIPTION)) {
		    String destination = words.get(colomnsTypes.indexOf(DESCRIPTION));
		    attributes.add(fromCSVDestination(destination));
		}
		if (colomnsTypes.contains(SERVICE_PROTOCOL) && colomnsTypes.contains(SERVICE_PORT)) {
		    String protocol = words.get(colomnsTypes.indexOf(SERVICE_PROTOCOL));
		    String port = words.get(colomnsTypes.indexOf(SERVICE_PORT));
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

    public static void toCSV(List<Integer> colomnsTypes, List<Hit> hits, String outputPath) throws IOException {

	if (outputPath == null) {
	    throw new IllegalArgumentException("Repo path can't be null");
	}
	File outputFile = new File(outputPath);
	if (!outputFile.canWrite()) {
	    throw new IOException("File can't be written");
	}

	BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
	try {
	    toCSV(colomnsTypes, hits, writer);
	} finally {
	    writer.close();
	}
    }

    public static void toCSV(List<Integer> colomnsTypes, List<Hit> hits, BufferedWriter writer) throws IOException {
	if (colomnsTypes == null) {
	    throw new IllegalArgumentException("Colomns types list can't be null");
	}
	if (colomnsTypes.contains(null)) {
	    throw new IllegalArgumentException("Colomns types list can't contains nulls");
	}
	if (colomnsTypes.contains(SERVICE_PORT) ^ colomnsTypes.contains(SERVICE_PROTOCOL)) {
	    throw new IllegalArgumentException("Choose service port and service protocol or neither of them");
	}
	if (hits == null) {
	    throw new IllegalArgumentException("Hits list can't be null");
	}
	for (Hit hit : hits) {
	    if (colomnsTypes.contains(SOURCE) && hit.getAttribute(Attribute.SOURCE_TYPE_ID) == null) {
		throw new IllegalArgumentException("Source attribute is missing in one of the hits");
	    }
	    if (colomnsTypes.contains(DESCRIPTION) && hit.getAttribute(Attribute.DESTINATION_TYPE_ID) == null) {
		throw new IllegalArgumentException("Destination attribute is missing in one of the hits");
	    }
	    if (colomnsTypes.contains(SOURCE) && hit.getAttribute(Attribute.SOURCE_TYPE_ID) == null) {
		throw new IllegalArgumentException("Source attribute is missing in one of the hits");
	    }
	}

	for (Hit hit : hits) {
	    if (hit == null) {
		continue;
	    }

	    String line = "";
	    for (int colomnsType : colomnsTypes) {
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
    }

    private static Source fromCSVSource(String source) {
	try {
	    return new Source(source);
	} catch (IllegalArgumentException e) {
	    throw new ParseException("Unable to parse source: ", e);
	}
    }

    private static Destination fromCSVDestination(String destination) {
	try {
	    return new Destination(destination);
	} catch (IllegalArgumentException e) {
	    throw new ParseException("Unable to parse destination: ", e);
	}
    }

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

    private static String toCSVSource(Source source) {
	if (source == null) {
	    throw new CSVParseException("Source doesn't exist");
	}
	return source.getIp().toString();
    }

    private static String toCSVDestination(Destination destination) {
	if (destination == null) {
	    throw new CSVParseException("Destination doesn't exist");
	}
	return destination.getIp().toString();
    }

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

    private static String toCSVServicePort(Service service) {
	return "" + service.getPortRangeStart();
    }

}
