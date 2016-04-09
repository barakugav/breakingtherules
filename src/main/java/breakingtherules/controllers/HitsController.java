package breakingtherules.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
     * @return List of all the appropriate hits
     * @throws IOException
     *             if failed
     * @throws NoCurrentJobException
     */
    @RequestMapping(value = "/hits", method = RequestMethod.GET)
    public ListDto<Hit> hits(int startIndex, int endIndex) throws NoCurrentJobException, IOException {
	try {
	    return m_job.getHits(startIndex, endIndex);

	} catch (NoCurrentJobException e) {
	    System.err.println("Tried recieving hits without initializing job.");
	    throw e;
	} catch (IOException e) {
	    System.err.println(e.getMessage());
	    throw e;
	}
    }

}
