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

/**
 * Implementation of {@link HitsDao} by xml repository
 */
@Component
public class HitsXmlDao implements HitsDao {

	/**
	 * Document of this repository
	 */
	private Document m_doc;

	/**
	 * Constructor
	 */
	public HitsXmlDao() {
		loadRepository("repository/repository.xml");
	}

	public String loadRepository(String path) {
		try {
			File repoFile = new File(path);

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			m_doc = builder.parse(repoFile);
			return null;

		} catch (IOException e) {
			e.printStackTrace();
			return "IO Exception";
		} catch (SAXException e) {
			e.printStackTrace();
			return "SAX Exception";
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return "Parser Configuration Exception";
		}
	}

	public List<Hit> getHits(Filter filter) {
		// Get all hits from repository
		NodeList hitsList = m_doc.getElementsByTagName("hit");

		// Extract only hits that match the filter
		List<Hit> matchedHits = new ArrayList<Hit>();
		for (int i = 0; i < hitsList.getLength(); i++) {
			Node hitNode = hitsList.item(i);
			if (hitNode.getNodeType() == Element.ELEMENT_NODE) {
				Element hitElm = (Element) hitNode;

				Hit hit = createHit(hitElm);

				if (filter.isMatch(hit)) {
					matchedHits.add(hit);
				}
			}
		}

		return matchedHits;
	}

	/**
	 * Creates {@link Hit} object from {@link Element} xml object
	 * 
	 * @param hitElm
	 *            xml element with hit attributes
	 * @return hit object with the element attributes
	 */
	private Hit createHit(Element hitElm) {
		// Read attributes from element
		String id = hitElm.getAttribute("id");
		String source = hitElm.getAttribute("source");
		String destination = hitElm.getAttribute("destination");
		String service = hitElm.getAttribute("service");

		// Convert strings to attributes
		int newId = Integer.parseInt(id);
		Source newSource = new Source(source);
		Destination newDestination = new Destination(destination);
		Service newService = new Service(service);

		// Create attributes vector
		Vector<Attribute> attributes = new Vector<Attribute>();
		attributes.add(newSource);
		attributes.add(newDestination);
		attributes.add(newService);

		return new Hit(newId, attributes);
	}

}
