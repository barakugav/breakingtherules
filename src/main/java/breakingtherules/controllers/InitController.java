package breakingtherules.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.ParseException;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

@RestController
public class InitController {

    /**
     * The session job to be initiated
     */
    @Autowired
    private Job m_job;

    /**
     * Request to handle a new job.
     * 
     * Note: a bug in Spring PUT - parameters should be sent through URL:
     * http://stackoverflow.com/questions/5894270/springmvc-is-not-recognizing-
     * request-body-parameters-if-using-put
     * 
     * @param jobName
     *            The name of the new job to handle
     * @return true if succeeded, else - false
     * @throws IOException
     *             if any I/O error occurs
     * @throws NoCurrentJobException
     *             if job wan't set yet
     * @throws ParseException 
     */
    @RequestMapping(value = "/job", method = RequestMethod.PUT)
    public boolean init(@RequestParam(value = "job_name") String jobName) throws IOException, NoCurrentJobException, ParseException {
	m_job.setJob(jobName);
	return true;
    }

}
