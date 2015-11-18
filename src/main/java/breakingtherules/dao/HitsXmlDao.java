package breakingtherules.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

/**
 * Implementation of {@link HitsDao} by XML repository
 */
@Component
public class HitsXmlDao implements HitsDao {

    public List<Hit> getHits(Job job, int startIndex, int endIndex) throws IOException, NoCurrentJobException {
	// Load from file
	String repoPath = job.getRepositoryLocation();
	Document repositoryDoc = loadRepository(repoPath);

	// Get all hits from repository
	NodeList hitsList = repositoryDoc.getElementsByTagName("hit");

	// Extract only hits that match the filter
	List<Hit> matchedHits = new ArrayList<Hit>();
	for (int i = 0; i < hitsList.getLength() && matchedHits.size() < endIndex; i++) {
	    Node hitNode = hitsList.item(i);
	    if (hitNode.getNodeType() == Element.ELEMENT_NODE) {
		Element hitElm = (Element) hitNode;

		Hit hit = createHit(hitElm);

		Filter filter = job.getFilter();
		if (filter.isMatch(hit))
		    matchedHits.add(hit);
	    }
	}

	return matchedHits.subList(startIndex, Math.min(endIndex, matchedHits.size()));
    }

    /**
     * Creates {@link Hit} object from {@link Element} XML object
     * 
     * @param hitElm
     *            XML element with hit attributes
     * @return hit object with the element attributes
     */
    private Hit createHit(Element hitElm) {
	// Read attributes from element
	String id = hitElm.getAttribute("id");
	String source = hitElm.getAttribute("source");
	String destination = hitElm.getAttribute("destination");
	String service = hitElm.getAttribute("service");

	// Convert strings to attributes
	int intID = Integer.parseInt(id);
	Source sourceObj = new Source(source);
	Destination destinationObj = new Destination(destination);
	Service serviceObj = new Service(service);

	// Create attributes vector
	Vector<Attribute> attributes = new Vector<Attribute>();
	attributes.add(sourceObj);
	attributes.add(destinationObj);
	attributes.add(serviceObj);

	return new Hit(intID, attributes);
    }

    /**
     * Load document from a file
     * 
     * @param path
     *            String path to repository file
     * @return repository document loaded from file
     * @throws IOException
     *             if read from file failed
     */
    private static Document loadRepository(String path) throws IOException {
	Document repositoryDoc;

	try {
	    File repoFile = new File(path);

	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    repositoryDoc = builder.parse(repoFile);
	    return repositoryDoc;

	} catch (IOException | ParserConfigurationException | SAXException e) {
	    e.printStackTrace();
	    throw new IOException(e.getMessage());
	}
    }

}
