package breakingtherules.dao;

import java.io.IOException;
import java.util.List;

import breakingtherules.firewall.Rule;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

/**
 * Component that supply data from repository
 */
public interface RulesDao {

    /**
     * Get rules from repository by filter in current job
     * 
     * @param currentJob
     *            the current job
     * @return list of rules that match the filter
     */
    public List<Rule> getRules(Job currentJob) throws IOException, NoCurrentJobException;

}
