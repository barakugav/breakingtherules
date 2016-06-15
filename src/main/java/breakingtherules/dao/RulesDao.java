package breakingtherules.dao;

import java.io.IOException;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Rule;

/**
 * Component that supply rules from repository
 */
public interface RulesDao {

    /**
     * @param jobName
     *            The name of the job that is in question
     * @return The original rule of the given job
     * @throws IOException
     *             if any I/O error occurs
     */
    public Rule getOriginalRule(String jobName) throws IOException;

    /**
     * Get all rules from repository
     * 
     * @param jobName
     *            Name of the rules' job
     * @return all rules created
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRules(String jobName) throws IOException;

    /**
     * Get rules from repository in range [startIndex, endIndex]
     * 
     * @param jobName
     *            name of the rules' job
     * @param startIndex
     *            the end index of the rules list, including this index
     * @param endIndex
     *            the end index of the rules list, including this index
     * @return rules in range [startIndex, endIndex]
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRules(String jobName, int startIndex, int endIndex) throws IOException;

}
