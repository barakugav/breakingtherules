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
     * Get list of his by filter of a job
     * 
     * @param job
     *            the current job
     * @param startIndex
     *            start index of the hits list
     * @param endIndex
     *            end index of the hits list
     * @return list of hits in range [startIndex, endIndex]
     * @throws IOException
     *             if failed to load repository
     * @throws NoCurrentJobException
     *             if there is no current job
     */
    public List<Hit> getHits(Job job, int startIndex, int endIndex) throws IOException, NoCurrentJobException;

}
