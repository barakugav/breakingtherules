package breakingtherules.dao;

import java.util.List;

import breakingtherules.firewall.Rule;

/**
 * Component that supply data from repository
 */
public interface RulesDao {

    /**
     * Load repository from XML file
     * 
     * @param path
     *            String path to repository
     * @return null if success, else - error message
     */
    public String loadRepository(String path);

    /**
     * Get rules from repository by filter
     * 
     * @return array of rules that match the filter
     */
    public List<Rule> getRules();

}
