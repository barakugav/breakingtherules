package breakingtherules.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.firewall.Rule;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

@RestController
public class RulesController {


    /**
     * Has the current job that is being worked on
     */
    @Autowired
    private Job m_job;

    @RequestMapping(value = "/rules", method = RequestMethod.GET)
    public List<Rule> rules() throws NoCurrentJobException, IOException {

	try {
	    return m_job.getRules();
	} catch (NoCurrentJobException e) {
	    System.err.println("Tried recieving hits without initializing job.");
	    throw e;
	}

    }
}
