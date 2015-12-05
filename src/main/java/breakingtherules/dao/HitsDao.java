package breakingtherules.dao;

import java.io.IOException;

import breakingtherules.dto.HitsDto;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

/**
 * Component that supply data from repository
 */
public interface HitsDao {

    /**
     * Gathers all the hits that match the job's filter. Then cuts the ones
     * between startIndex and endIndex, and sends as a DTO.
     * 
     * @param job
     *            The job to take hits from
     * @param startIndex
     *            The 0-index to start from, including this index.
     * @param endIndex
     *            The 0-index to end at, excluding this index.
     * @return HitsDto object
     * 
     * @throws IOException
     *             if failed to read from hits repository
     * @throws NoCurrentJobException
     *             if there is no current job
     */
    public HitsDto getHits(Job currentJob, int startIndex, int endIndex) throws IOException, NoCurrentJobException;

    
    /**
     * Gathers all the hits that match the job's filter. 
     * 
     * @param job
     *            The job to take hits from
     * @return HitsDto object
     * 
     * @throws IOException
     *             if failed to read from hits repository
     * @throws NoCurrentJobException
     *             if there is no current job
     */
    public HitsDto getHits(Job currentJob) throws IOException, NoCurrentJobException;

}
