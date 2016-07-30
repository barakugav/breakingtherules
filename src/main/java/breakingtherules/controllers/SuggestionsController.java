package breakingtherules.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.ParseException;
import breakingtherules.dto.SuggestionsDto;
import breakingtherules.service.Suggestion;
import breakingtherules.service.SuggestionsAlgorithm;
import breakingtherules.session.JobManager;
import breakingtherules.session.NoCurrentJobException;

/**
 * Controller that provides requests for suggestions.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Suggestion
 * @see JobManager
 */
@RestController
public class SuggestionsController {

    /**
     * The session job manager
     */
    @Autowired
    private JobManager m_jobManager;

    /**
     * Get suggestion for the current uncovered hits in the current job.
     *
     * @param amount
     *            the number of requested suggestions.
     * @return list of suggestion, one {@link SuggestionsDto} for each
     *         suggestion type.
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws ParseException
     *             if any parse errors occurs.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     * @throws IllegalArgumentException
     *             if {@code amount} is negative.
     */
    @RequestMapping(value = "/suggestions", method = RequestMethod.GET)
    public List<SuggestionsDto> getSuggestions(@RequestParam(value = "amount", defaultValue = "10") final int amount)
	    throws IOException, ParseException {
	return m_jobManager.getSuggestions(amount);
    }

    /**
     * Change the permissiveness of the suggestion-creating algorithm
     *
     * @param permissiveness
     *            the new permissiveness value. Should be in range [
     *            {@link SuggestionsAlgorithm#MIN_PERMISSIVENESS min},
     *            {@link SuggestionsAlgorithm#MAX_PERMISSIVENESS max}].
     * @throws IllegalArgumentException
     *             if the permissiveness is not in range [
     *             {@link SuggestionsAlgorithm#MIN_PERMISSIVENESS min},
     *             {@link SuggestionsAlgorithm#MAX_PERMISSIVENESS max}].
     */
    @RequestMapping(value = "/permissiveness", method = RequestMethod.PUT)
    public void setPermissiveness(final double permissiveness) {
	m_jobManager.setAlgorithmPermissiveness(permissiveness);
	System.out.println("The permissiveness changed to be " + permissiveness);
    }

}
