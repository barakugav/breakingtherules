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

/**
 * Controller that allows requests and creation of rules.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see Rule
 * @see Job
 */
@RestController
public class RulesController {

    /**
     * The session job.
     */
    @Autowired
    private Job m_job;

    /**
     * Get the current job's rules file, in XML format.
     * 
     * @param request
     *            The HTTP request
     * @param response
     *            The HTTP response
     * @return A file that holds all of the current job's rules
     */
    @RequestMapping(value = "/rulesFile", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource rulesFile(@SuppressWarnings("unused") final HttpServletRequest request,
	    final HttpServletResponse response) {
	response.setHeader("Content-Disposition", "attachment; filename=\"rules.xml\"");
	// TODO - remove 'request' parameter (?) - unused
	return m_job.getRulesFile();
    }

    /**
     * Get the current job's rules.
     * 
     * @return the rules of the current job.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    @RequestMapping(value = "/rule", method = RequestMethod.GET)
    public List<Rule> rules() {
	// TODO - change name to 'getRules'
	return m_job.getRules();
    }

    /**
     * Create new rule from the current filter.
     * 
     * @throws IOException
     *             if any I/O errors occurs when trying to update the
     *             statistics.
     * @throws ParseException
     *             if any parse errors occurs when trying to update the
     *             statistics.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    @RequestMapping(value = "/rule", method = RequestMethod.POST)
    public void createRule() throws IOException, ParseException {
	m_job.addCurrentFilterToRules();
    }

    /**
     * Delete a rule from the current rules list.
     * 
     * @param ruleIndex
     *            index of the rules.
     * @throws IOException
     *             if any I/O errors occurs when trying to update the
     *             statistics.
     * @throws ParseException
     *             if any parse errors occurs when trying to update the
     *             statistics.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    @RequestMapping(value = "/rule", method = RequestMethod.DELETE)
    public void deleteRule(@RequestParam("index") final int ruleIndex) throws IOException, ParseException {
	m_job.deleteRule(ruleIndex);
    }

}
