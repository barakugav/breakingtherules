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
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

/**
 * Controller that provides requests for suggestions.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 * @see Suggestion
 * @see Job
 */
@RestController
public class SuggestionsController {

    /**
     * The session job.
     */
    @Autowired
    private Job job;

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
	return job.getSuggestions(amount);
    }

}
