package breakingtherules.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.HitsDao;
import breakingtherules.firewall.Hit;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

@RestController
public class HitsController {

    /**
     * Used to get the hits that match the filter
     */
    @Autowired
    private HitsDao m_hitsDao;

    /**
     * Has the current job that is being worked on
     */
    @Autowired
    private Job m_job;

    /**
     * Answers the GET hits query
     * 
     * @return List of all the appropriate hits
     * @throws NoCurrentJobException
     * @throws IOException
     */
    @RequestMapping(value = "/hits", method = RequestMethod.GET)
    public List<Hit> hits() throws NoCurrentJobException, IOException {
	try {
	    List<Hit> hits = m_hitsDao.getHits(m_job, 0, 10);
	    return hits;

	} catch (NoCurrentJobException e) {
	    System.err.println("Tried recieving hits without initializing job.");
	    throw e;
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    throw e;
	}

    }
}
