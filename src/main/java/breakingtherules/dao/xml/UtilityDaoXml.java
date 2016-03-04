package breakingtherules.dao.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The UtilityXmlDao class is used XML DAOs, and include only static method -
 * all helper method
 * 
 * @see HitsDaoXml
 * @see RulesDaoXml
 */
public class UtilityDaoXml {

    /**
     * Read document from a file
     * 
     * @param path
     *            string path to file
     * @return document with the file data
     * @throws IOException
     *             if reading from file failed
     */
    public static Document readFile(String path) throws IOException {
	try {
	    File repoFile = new File(path);
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document fileDocument = builder.parse(repoFile);
	    return fileDocument;

	} catch (IOException | SAXException | ParserConfigurationException e) {
	    e.printStackTrace();
	    throw new IOException("Unable to load file: " + e.getMessage());
	}
    }

    /**
     * Write a document to a file
     * 
     * @param path
     *            string path to file
     * @param doc
     *            document to write
     * @throws IOException
     *             if failed to write to memory
     */
    public static void writeFile(String path, Document doc) throws IOException {
	try {
	    File file = new File(path);
	    TransformerFactory factory = TransformerFactory.newInstance();
	    Transformer transformer = factory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	    DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(file);
	    transformer.transform(source, result);

	} catch (TransformerException e) {
	    e.printStackTrace();
	    throw new IOException(e.getMessage());
	}
    }

}
