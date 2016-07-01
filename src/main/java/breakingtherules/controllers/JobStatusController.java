package breakingtherules.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dto.JobStatusDto;
import breakingtherules.firewall.Rule;
import breakingtherules.session.JobManager;

/**
 * This controller allows the user to get the status of the current working job.
 * <p>
 * The status is the number of hits in every category, the amount of work
 * achieved and left, etc.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 * @see Job
 */
@RestController
public class JobStatusController {

    // TODO - move to JobController (?)

    /**
     * The session job manager
     */
    @Autowired
    private JobManager m_jobManager;

    /**
     * Get the current status of the job - the number of hits in every category,
     * the amount of work achieved and left, etc.
     * 
     * @return The job-status of the current job
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public JobStatusDto getStatus() {
	return createStatusDto(m_jobManager);
    }

    /**
     * Helper function that uses the Job API to create a JobStatusDTO
     * 
     * @param job
     *            The job that needs to be queried
     * @return Information about the status of the given job
     */
    private static JobStatusDto createStatusDto(JobManager job) {
	Rule orig = job.getOriginalRule();
	int createdRules = job.getRules().size();
	int totalHitsCount = job.getTotalHitsCount();
	int coveredHitsCount = job.getCoveredHitsCount();
	int filteredHitsCount = job.getFilteredHitsCount();
	return new JobStatusDto(orig, createdRules, totalHitsCount, coveredHitsCount, filteredHitsCount);
    }
}
