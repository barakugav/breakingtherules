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
import breakingtherules.dao.csv.CSVParser;
import breakingtherules.session.Job;

/**
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Job
 */
@RestController
@MultipartConfig
public class JobController {

    /**
     * The session job.
     */
    @Autowired
    private Job m_job;

    /**
     * Request to handle a new job.
     * <p>
     * Note: a bug in Spring PUT - parameters should be sent through URL:
     * http://stackoverflow.com/questions/5894270/springmvc-is-not-recognizing-
     * request-body-parameters-if-using-put
     * 
     * @param jobName
     *            The name of the new job to handle
     * @return true if succeeded, else - false
     * @throws IOException
     *             if any I/O errors occurs when trying to update the
     *             statistics.
     * @throws ParseException
     *             if any parse errors occurs when trying to update the
     *             statistics.
     */
    @RequestMapping(value = "/job", method = RequestMethod.PUT)
    public boolean setJob(@RequestParam(value = "job_name") final String jobName) throws IOException, ParseException {
	m_job.setJob(jobName);
	return true;
    }

    /**
     * Create a new job and initialize it with CSV hits file.
     * 
     * @param jobName
     *            the new job's name
     * @param hitsFile
     *            the initialize CSV hits file.
     * @throws IOException
     *             if any I/O errors occurs when processing the file.
     * @throws ParseException
     *             if any parse errors occurs when processing the file.
     * @throws NullPointerException
     *             if the file is null.
     */
    @RequestMapping(value = "/job", method = RequestMethod.POST)
    public void newJob(@RequestParam(value = "job_name") final String jobName,
	    @RequestParam(value = "hits_file") final MultipartFile hitsFile) throws IOException, ParseException {
	m_job.createJob(jobName, hitsFile, CSVParser.DEFAULT_COLUMNS_TYPES);
    }

}
