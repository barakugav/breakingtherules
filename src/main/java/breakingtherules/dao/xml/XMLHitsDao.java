package breakingtherules.dao.xml;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import breakingtherules.dao.AbstractCachedHitsDao;
import breakingtherules.dao.DaoUtilities;
import breakingtherules.dao.HitsDao;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * Implementation of {@link HitsDao} by XML repository.
 * <p>
 * Able to read hits from repository files, save the hits for each repository
 * for next hits request.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class XMLHitsDao extends AbstractCachedHitsDao {

    /**
     * Supplier function of hits.
     * <p>
     * 
     * @see #getHitsSupplier()
     */
    private static final Function<String, Set<Hit>> HITS_SUPPLIER = jobName -> {
	try {
	    final Set<Hit> hits = new HashSet<>();
	    parseHits(XMLDaoConfig.getHitsFile(jobName), Collections.emptyList(), Filter.ANY_FILTER, hits);
	    return hits;

	} catch (final IOException e) {
	    throw new UncheckedIOException(e);
	} catch (final XMLParseException e) {
	    throw new UncheckedXMLParseException(e);
	}
    };

    /**
     * {@inheritDoc}
     */
    @Override
    public void initJob(final String jobName, final Iterable<Hit> hits) throws IOException {
	writeHits(hits, XMLDaoConfig.getHitsFile(jobName));
    }

    /**
     * Get hits from repository that match all rules and filter by repository
     * string path.
     * 
     * @param fileName
     *            name of the file.
     * @param rules
     *            list of the current rules, act like additional filter
     * @param filter
     *            filter of the hits
     * @return all hits that match all rules and filter.
     * @throws IOException
     *             if failed to read from memory.
     * @throws XMLParseException
     *             if any XML parse error occurs in the data.
     */
    public static List<Hit> parseHits(final String fileName, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	final ArrayList<Hit> hits = new ArrayList<>();
	parseHits(fileName, rules, filter, hits);
	return hits;
    }

    /**
     * Write a list of hits to file in XML format
     * 
     * @param hits
     *            list of the hits
     * @param fileName
     *            output file path
     * @throws IOException
     *             if fails to write to file
     */
    public static void writeHits(final Iterable<Hit> hits, final String fileName) throws IOException {
	try {
	    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    final DocumentBuilder builder = factory.newDocumentBuilder();
	    final Document doc = builder.newDocument();

	    final Element repoElm = doc.createElement(XMLDaoConfig.TAG_REPOSITORY);
	    for (final Hit hit : hits) {
		final Element elm = createElement(doc, hit);
		repoElm.appendChild(elm);
	    }
	    doc.appendChild(repoElm);

	    XMLUtilities.writeFile(fileName, doc);
	} catch (final ParserConfigurationException e) {
	    throw new IOException(e);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Function<String, Set<Hit>> getHitsSupplier() {
	return HITS_SUPPLIER;
    }

    /**
     * Unchecked version of {@link XMLParseException}.
     * <p>
     * The UncheckedXMLParseException is a wrapper for a checked
     * {@link XMLParseException}.
     * <p>
     * Used when implementing or overriding a method that doesn't throw super
     * class exception of {@link XMLParseException}.
     * <p>
     * This exception is similar to {@link UncheckedIOException}.
     * <p>
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     */
    protected static class UncheckedXMLParseException extends UncheckedParseException {

	@SuppressWarnings("javadoc")
	private static final long serialVersionUID = 6371272539188428352L;

	/**
	 * Construct new UncheckedXMLParseException without a message.
	 * 
	 * @param cause
	 *            the original checked {@link XMLParseException}.
	 */
	protected UncheckedXMLParseException(final XMLParseException cause) {
	    super(cause);
	}

	/**
	 * Construct new UncheckedXMLParseException with a message.
	 * 
	 * @param message
	 *            the exception's message.
	 * @param cause
	 *            the original checked {@link XMLParseException}.
	 */
	protected UncheckedXMLParseException(final String message, final XMLParseException cause) {
	    super(message, cause);
	}

	/**
	 * Get the {@link XMLParseException} cause of this unchecked exception.
	 * <p>
	 */
	@Override
	public synchronized XMLParseException getCause() {
	    return (XMLParseException) super.getCause();
	}

    }

    /**
     * Parse all hits from file to a destination collection.
     * 
     * @param fileName
     *            the input file name.
     * @param rules
     *            list of rules to filter by them.
     * @param filter
     *            filter to filter by it.
     * @param destination
     *            the destination collection of all hits parsed from the file
     *            and matched all rules and the filter.
     * @throws XMLParseException
     *             if the data in the file is invalid.
     * @throws IOException
     *             if any I/O errors occurs.
     */
    private static void parseHits(final String fileName, final List<Rule> rules, final Filter filter,
	    final Collection<? super Hit> destination) throws XMLParseException, IOException {
	// Load from file
	final Document repositoryDoc;
	try {
	    repositoryDoc = XMLUtilities.readFile(fileName);
	} catch (final SAXException e) {
	    throw new XMLParseException(e);
	}

	// Get all hits from repository
	final NodeList hitsList = repositoryDoc.getElementsByTagName(XMLDaoConfig.TAG_HIT);

	// Extract all hits that match the filter
	final int length = hitsList.getLength();
	for (int i = 0; i < length; i++) {
	    final Node hitNode = hitsList.item(i);
	    if (hitNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element hitElm = (Element) hitNode;
		final Hit hit = createHit(hitElm);
		if (DaoUtilities.isMatch(hit, rules, filter)) {
		    destination.add(hit);
		}
	    }
	}
    }

    /**
     * Creates {@link Hit} object from {@link Element} XML object
     * 
     * @param hitElm
     *            XML element with hit attributes
     * @return hit object with the element attributes
     * @throws XMLParseException
     *             if failed to parse element to hit
     */
    private static Hit createHit(final Element hitElm) throws XMLParseException {
	// Read attributes from element
	final String source = hitElm.getAttribute(XMLDaoConfig.TAG_SOURCE);
	final String destination = hitElm.getAttribute(XMLDaoConfig.TAG_DESTINATION);
	final String service = hitElm.getAttribute(XMLDaoConfig.TAG_SERVICE);

	if (source == null || source.isEmpty()) {
	    throw new XMLParseException("Source does not exist");
	}
	if (destination == null || destination.isEmpty()) {
	    throw new XMLParseException("Destination does not exist");
	}
	if (service == null || service.isEmpty()) {
	    throw new XMLParseException("Service does not exist");
	}

	// Convert strings to attributes
	final List<Attribute> attributes = new ArrayList<>();
	try {
	    attributes.add(Source.valueOf(source));
	    attributes.add(Destination.valueOf(destination));
	    attributes.add(Service.valueOf(service));

	} catch (final Exception e) {
	    throw new XMLParseException(e);
	}
	return new Hit(attributes);
    }

    /**
     * Create new XML element of hit.
     * <p>
     * 
     * @param doc
     *            the parent document.
     * @param hit
     *            the parsed hit.
     * @return XML element that represent the hit.
     */
    private static Element createElement(final Document doc, final Hit hit) {
	final Element elm = doc.createElement(XMLDaoConfig.TAG_HIT);
	for (final Attribute attribute : hit) {
	    elm.setAttribute(attribute.getType().toLowerCase(), attribute.toString());
	}
	return elm;
    }

}
