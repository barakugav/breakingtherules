package breakingtherules.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.ParseException;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Hit;
import breakingtherules.session.JobManager;
import breakingtherules.session.NoCurrentJobException;

/**
 * Controller that allows requests of hits.
 * <p>
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Hit
 * @see JobManager
 */
@RestController
public class HitsController {

    /**
     * The session job manager
     */
    @Autowired
    private JobManager m_jobManager;

    /**
     * Answers the GET hits query
     *
     * @param startIndex
     *            Index of the first hit wanted, inclusive
     * @param endIndex
     *            Index of the last hit wanted, exclusive
     * @return List of all the requested hits.
     * @throws IOException
     *             if any I/O errors occurs when trying to update the
     *             statistics.
     * @throws ParseException
     *             if any parse errors occurs when trying to update the
     *             statistics.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    @RequestMapping(value = "/hits", method = RequestMethod.GET)
    public ListDto<Hit> getHits(final int startIndex, final int endIndex) throws IOException, ParseException {
	return m_jobManager.getHits(startIndex, endIndex);
    }

}
