package breakingtherules.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.ParseException;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Hit;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

@RestController
public class HitsController {

    /**
     * Has the current job that is being worked on
     */
    @Autowired
    private Job m_job;

    /**
     * Answers the GET hits query
     * 
     * @param startIndex
     *            Index of the first hit wanted, inclusive
     * @param endIndex
     *            Index of the last hit wanted, exclusive
     * @return List of all the appropriate hits
     * @throws IOException
     *             if failed
     * @throws NoCurrentJobException
     *             if job wasn't initialize
     * @throws ParseException
     */
    @RequestMapping(value = "/hits", method = RequestMethod.GET)
    public ListDto<Hit> hits(int startIndex, int endIndex) throws NoCurrentJobException, IOException, ParseException {
	try {
	    return m_job.getHits(startIndex, endIndex);

	} catch (NoCurrentJobException e) {
	    System.err.println("Tried recieving hits without initializing job.");
	    throw e;
	}
    }

}
