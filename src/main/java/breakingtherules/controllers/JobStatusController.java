package breakingtherules.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dto.StatusDto;
import breakingtherules.firewall.Rule;
import breakingtherules.session.Job;

@RestController
public class JobStatusController {
    
    @Autowired
    private Job m_job;

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public StatusDto getStatus() {
	return createStatusDto(m_job);
    }
    
    private StatusDto createStatusDto(Job job) {
	Rule orig = job.getOriginalRule();
	int createdRules = job.getRules().size();
	int totalHitsCount = job.getTotalHitsCount();
	int coveredHitsCount = job.getCoveredHitsCount();
	int filteredHitsCount = job.getFilteredHitsCount();
	return new StatusDto(orig, createdRules, totalHitsCount, coveredHitsCount, filteredHitsCount);
    }
}
