package breakingtherules.dao;

import java.io.IOException;
import java.util.List;

import breakingtherules.firewall.Hit;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

/**
 * Component that supply data from repository
 */
public interface HitsDao {

    /**
     * Get a list of hits from DAO
     * 
     * @param currentJob
     *            the current job
     * @param startIndex
     *            start index of the hits list
     * @param endIndex
     *            end index of the hits list
     * @return list of the hits from DAO
     * @throws IOException
     */
    public List<Hit> getHits(Job currentJob, int startIndex, int endIndex) throws IOException, NoCurrentJobException;

}
