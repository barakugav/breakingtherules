package breakingtherules.dao;

import java.io.IOException;

import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Rule;

/**
 * Component that supply rules from repository
 */
public interface RulesDao {

    /**
     * Get all rules from repository
     * 
     * @param jobId
     *            id of the rules' job
     * @return all rules
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRules(int jobId) throws IOException;

    /**
     * Get rules from repository in range [startIndex, endIndex]
     * 
     * @param jobId
     *            id of the rules' job
     * @param startIndex
     *            the end index of the rules list, including this index
     * @param endIndex
     *            the end index of the rules list, including this index
     * @return rules in range [startIndex, endIndex]
     * @throws IOException
     *             if failed to read from memory
     */
    public ListDto<Rule> getRules(int jobId, int startIndex, int endIndex) throws IOException;

}
