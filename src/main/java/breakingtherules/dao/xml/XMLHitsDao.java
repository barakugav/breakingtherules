package breakingtherules.dao.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHitsNumber(java.lang.String,
     * java.util.List, breakingtherules.firewall.Filter)
     */
    @Override
    public int getHitsNumber(final String jobName, final List<Rule> rules, final Filter filter)
	    throws IOException, XMLParseException {
	return getHits(jobName, rules, filter).getSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see breakingtherules.dao.HitsDao#getHits(java.lang.String,
     * java.util.List, breakingtherules.firewall.Filter)
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

	// Load from file
	final Document repositoryDoc = XMLUtilities.readFile(fileName);

	// Get all hits from repository
	final NodeList hitsList = repositoryDoc.getElementsByTagName(XMLDaoConfig.HIT);

	// Extract all hits that match the filter
	final int length = hitsList.getLength();
	final ArrayList<Hit> hits = new ArrayList<>(length);
	for (int i = 0; i < length; i++) {
	    final Node hitNode = hitsList.item(i);
	    if (hitNode.getNodeType() == Node.ELEMENT_NODE) {
		final Element hitElm = (Element) hitNode;
		final Hit hit = createHit(hitElm);
		if (DaoUtilities.isMatch(hit, rules, filter)) {
		    hits.add(hit);
		}
	    }
	}
	hits.trimToSize();
	final int size = hits.size();
	return new ListDto<>(hits, 0, size, size);
    }

    @Override
    public Set<UniqueHit> getUniqueHits(final String jobName, final List<Rule> rules, final Filter filter) {
	return null;
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

	    final Element repoElm = doc.createElement(XMLDaoConfig.REPOSITORY);
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
	final String sourceStr = hitElm.getAttribute(Attribute.SOURCE_TYPE.toLowerCase());
	final String destinationStr = hitElm.getAttribute(Attribute.DESTINATION_TYPE.toLowerCase());
	final String serviceStr = hitElm.getAttribute(Attribute.SERVICE_TYPE.toLowerCase());

	if (sourceStr == null || sourceStr.isEmpty()) {
	    throw new XMLParseException("Source does not exist");
	} else if (destinationStr == null || destinationStr.isEmpty()) {
	    throw new XMLParseException("Destination does not exist");
	} else if (serviceStr == null || serviceStr.isEmpty()) {
	    throw new XMLParseException("Service does not exist");
	}

	// Convert strings to attributes
	final List<Attribute> attributes = new ArrayList<>();
	try {
	    attributes.add(Source.createFromString(sourceStr));
	    attributes.add(Destination.createFromString(destinationStr));
	    attributes.add(Service.createFromString(serviceStr));

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
	final Element elm = doc.createElement(XMLDaoConfig.HIT);
	for (final Attribute attribute : hit) {
	    elm.setAttribute(attribute.getType().toLowerCase(), attribute.toString());
	}
	return elm;
    }

}
