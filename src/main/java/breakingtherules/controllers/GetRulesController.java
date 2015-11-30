package breakingtherules.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.RulesDao;
import breakingtherules.firewall.Rule;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

@RestController
public class GetRulesController {

    /**
     * Used to get the rules
     */
    @Autowired
    RulesDao rulesDao;

    /**
     * Has the current job that is being worked on
     */
    @Autowired
    Job job;

    @RequestMapping(value = "/rules", method = RequestMethod.GET)
    public List<Rule> rules() throws NoCurrentJobException {

	// TODO structure issue - what if multiple requests do `loadRepository`?
	// Applies to HitsDao and RulesDao

	try {
	    rulesDao.loadRepository(job.getRepositoryLocation());
	} catch (NoCurrentJobException e) {
	    System.err.println("Tried recieving hits without initializing job.");
	    throw new NoCurrentJobException();
	}

	return rulesDao.getRules();
    }
}
