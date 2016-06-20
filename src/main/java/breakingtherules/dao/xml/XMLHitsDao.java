package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import breakingtherules.dao.DaoUtilities;
import breakingtherules.dao.HitsDao;
import breakingtherules.dao.UniqueHit;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.utilities.MutableInteger;

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
public class XMLHitsDao implements HitsDao {

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	return getHits(jobName, rules, filter).getSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListDto<Hit> getHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	return getHitsByPath(XMLDaoConfig.getHitsFile(jobName), rules, filter);
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
    public ListDto<Hit> getHitsByPath(final String fileName, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	final ArrayList<Hit> hits = new ArrayList<>();
	parseHits(fileName, rules, filter, hits);
	hits.trimToSize();
	final int size = hits.size();
	return new ListDto<>(hits, 0, size, size);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<UniqueHit> getUniqueHits(final String jobName, final List<Rule> rules, final Filter filter)
	    throws XMLParseException, IOException {

	final Map<Hit, MutableInteger> hitsCount = new HashMap<>();
	parseHits(XMLDaoConfig.getHitsFile(jobName), rules, filter, new AbstractSet<Hit>() {

	    @Override
	    public boolean add(final Hit hit) {
		hitsCount.computeIfAbsent(hit, MutableInteger.zeroInitializerFunction).value++;
		return true;
	    }

	    @Override
	    public Iterator<Hit> iterator() {
		return hitsCount.keySet().iterator();
	    }

	    @Override
	    public int size() {
		return hitsCount.size();
	    }

	});

	final Set<UniqueHit> uniqueHits = new HashSet<>();
	for (final Iterator<Map.Entry<Hit, MutableInteger>> it = hitsCount.entrySet().iterator(); it.hasNext();) {
	    final Map.Entry<Hit, MutableInteger> entry = it.next();
	    final Hit hit = entry.getKey();
	    final int amount = entry.getValue().value;
	    uniqueHits.add(new UniqueHit(hit, amount));
	    it.remove();
	}
	return uniqueHits;
    }

    @Override
    public void initJob(String jobName, List<Hit> hits) throws IOException {
	toXml(hits, XMLDaoConfig.getHitsFile(jobName));
    }

    /**
     * Write a list of hits to file in XML format
     * 
     * @param hits
     *            list of the hits
     * @param filePath
     *            output file path
     * @throws IOException
     *             if fails to write to file
     */
    public static void toXml(final List<Hit> hits, final String filePath) throws IOException {
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

	    XMLUtilities.writeFile(filePath, doc);
	} catch (final ParserConfigurationException e) {
	    throw new IOException(e);
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
	final Document repositoryDoc = XMLUtilities.readFile(fileName);

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
	    attributes.add(Source.createFromString(source));
	    attributes.add(Destination.createFromString(destination));
	    attributes.add(Service.createFromString(service));

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
