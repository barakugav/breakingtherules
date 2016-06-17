package breakingtherules.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.ParseException;
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

    @RequestMapping(value = "/rulesFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource rulesFile(@SuppressWarnings("unused") HttpServletRequest request,
	    HttpServletResponse response) {
	response.setHeader("Content-Disposition", "attachment; filename=\"rules.xml\"");
	return new FileSystemResource(m_job.getRulesFilePath());
    }

    @RequestMapping(value = "/rule", method = RequestMethod.GET)
    public List<Rule> rules() throws NoCurrentJobException {
	return m_job.getRules();
    }

    @RequestMapping(value = "/rule", method = RequestMethod.POST)
    public void createRule() throws NoCurrentJobException, IOException, ParseException {
	m_job.addCurrentFilterToRules();
    }

    @RequestMapping(value = "/rule", method = RequestMethod.DELETE)
    public void deleteRule(@RequestParam("index") int ruleIndex)
	    throws NoCurrentJobException, IOException, ParseException {
	m_job.deleteRule(ruleIndex);
    }

}
