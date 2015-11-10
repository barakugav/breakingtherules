package breakingtherules.dao;

import java.util.List;

import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;

/**
 * Component that supply data from repository
 */
public interface HitsDao {

    /**
     * Load repository from XML file
     * 
     * @param path
     *            String path to repository
     * @return null if success, else - error message
     */
    public String loadRepository(String path);

    /**
     * Get hits from repository by filter
     * 
     * @param filter
     *            filter of the hits
     * @return array of hits that match the filter
     */
    public List<Hit> getHits(Filter filter);

}
