package breakingtherules.controllers;

import java.io.IOException;

import javax.servlet.annotation.MultipartConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import breakingtherules.dao.ParseException;
import breakingtherules.dao.csv.CSVParseException;
import breakingtherules.dao.csv.CSVParser;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

@RestController
@MultipartConfig
public class JobController {

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
    public boolean setJob(@RequestParam(value = "job_name") String jobName) throws IOException, ParseException {
	m_job.setJob(jobName);
	return true;
    }

    @RequestMapping(value = "/job", method = RequestMethod.POST)
    public void newJob(@RequestParam(value = "job_name") String jobName,
	    @RequestParam(value = "hits_file") MultipartFile hitsFile) throws IOException, CSVParseException {
	m_job.createJob(jobName, hitsFile, CSVParser.DEFAULT_COLUMNS_TYPES);
    }

}
