package breakingtherules.tests.dao.xml;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import breakingtherules.dao.xml.XMLHitsDao;
import breakingtherules.dao.xml.XMLParseException;
import breakingtherules.dao.xml.XMLUtilities;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;
import breakingtherules.tests.TestBase;

public class XMLHitsDaoTest extends TestBase {

    @Test
    public void getHitsByPathTest() throws XMLParseException, IOException {
	RepositoryDocument doc = new RepositoryDocument();

	int numberOfHits = 10;
	for (int id = 0; id < numberOfHits; id++) {
	    Element hitElm = doc.createElement("hit");
	    hitElm.setAttribute("id", "" + id);
	    hitElm.setAttribute("source", "100." + id + ".5.44");
	    hitElm.setAttribute("destination", id + ".55.48.127");
	    hitElm.setAttribute("service", "TCP 80");
	    doc.addElement(hitElm);
	}

	XMLHitsDao.parseHits(doc.getPath(), new ArrayList<Rule>(), Filter.ANY_FILTER);

	doc.destroy();
    }

    private static class RepositoryDocument {

	private String m_path;

	private Document m_doc;

	private Element m_repoElm;

	public RepositoryDocument() {
	    try {
		File file = File.createTempFile("tempRepository", ".xml");
		m_path = file.getAbsolutePath();
		m_doc = createDocumentInFile(m_path);

	    } catch (IOException e) {
		fail("Failed to created temp file. Coused by external factors. " + e.getMessage());
	    }
	}

	public String getPath() {
	    return m_path;
	}

	public Element createElement(String tagName) {
	    return m_doc.createElement(tagName);
	}

	public void addElement(Element elm) {
	    try {
		m_repoElm.appendChild(elm);
		XMLUtilities.writeFile(m_path, m_doc);

	    } catch (IOException e) {
		e.printStackTrace();
		fail("Failed to write to repository. Coused by external factors. " + e.getMessage());
	    }
	}

	public void destroy() {
	    File file = new File(m_path);
	    file.delete();
	}

	private Document createDocumentInFile(String path) {
	    try {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		m_repoElm = doc.createElement("Repository");
		doc.appendChild(m_repoElm);
		XMLUtilities.writeFile(path, doc);
		return doc;

	    } catch (IOException | ParserConfigurationException e) {
		e.printStackTrace();
		fail("Failed to create repository. Coused by external factors. " + e.getMessage());
		return null;
	    }
	}

    }

}
