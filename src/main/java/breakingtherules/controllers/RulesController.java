package breakingtherules.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

    @RequestMapping(value = "/rule", method = RequestMethod.GET)
    public List<Rule> rules() throws NoCurrentJobException {
	return m_job.getRules();
    }

    @RequestMapping(value = "/rule", method = RequestMethod.POST)
    public void createRule() throws IllegalArgumentException, NoCurrentJobException, IOException {
	m_job.addCurrentFilterToRules();
    }

    @RequestMapping(value = "/rule", method = RequestMethod.DELETE)
    public void deleteRule(@RequestParam("id") int ruleId)
	    throws IllegalArgumentException, NoCurrentJobException, IOException {
	m_job.deleteRule(ruleId);
    }

}
