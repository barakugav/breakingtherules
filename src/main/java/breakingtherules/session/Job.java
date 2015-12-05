package breakingtherules.session;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import breakingtherules.dao.HitsDao;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Job {

    public static final int NO_CURRENT_JOB = -1;

    @Autowired
    private HitsDao m_hitsDao;

    /**
     * Index of the job
     * 
     * Every job has a unique index. In the start of the session, there is no
     * active job, so the job_id is NO_CURRENT_JOB.
     */
    private int m_jobId = NO_CURRENT_JOB;

    /**
     * Suggestions for the job
     * 
     * All the suggestions offered by the algorithm, and their statistics. To be
     * passed to the user.
     */
    private JobSuggestions m_suggestions;

    /**
     * The current hit filter.
     * 
     * This is the filter that the user inputs, and receives only the hits that
     * match this filter.
     */
    private Filter m_filter;

    /**
     * All the rules of the current job
     * 
     * First - original base rule. Not modified Others - created with the user,
     * constantly modified.
     */
    private List<Rule> m_rules;

    /***************************/

    /**
     * Restore all relevant parameters about the job to be worked on.
     * 
     * @param id
     *            The id of the job that needs to be worked on.
     */
    public void setJob(int id) {
	m_jobId = id;
	m_suggestions = new JobSuggestions();
	m_filter = new Filter();
	m_suggestions.update();
    }

    /**
     * Return all the suggestions computed by the algorithm.
     * 
     * @return Current job's suggestions.
     * @throws NoCurrentJobException
     */
    public JobSuggestions getSuggestions() throws NoCurrentJobException {
	if (m_jobId == NO_CURRENT_JOB)
	    throw new NoCurrentJobException();
	return m_suggestions;
    }

    /**
     * 
     * @return All the rules for the current job
     */
    public List<Rule> getRules() {
	return m_rules;
    }

    /**
     * Uses the filter to filter out relevant hits.
     * 
     * @return All the hits caught by the filter.
     * @throws IOException
     */
    public List<Hit> getRelevantHits() throws NoCurrentJobException, IOException {
	List<Hit> hits = m_hitsDao.getHits(this).hits;
	return hits;
    }

    /**
     * Every job has its own repository location (until there will be a proper
     * database)
     * 
     * @return path to repository
     * @throws NoCurrentJobException
     */
    public String getRepositoryLocation() throws NoCurrentJobException {
	if (m_jobId == NO_CURRENT_JOB)
	    throw new NoCurrentJobException();
	return "repository/" + m_jobId + "/repository.xml";
    }

    public void setFilter(Filter f) {
	m_filter = f;
	m_suggestions.update();
    }

    public Filter getFilter() {
	return m_filter;
    }
}
