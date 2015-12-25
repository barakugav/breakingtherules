package breakingtherules.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dto.SuggestionsDto;
import breakingtherules.session.Job;
import breakingtherules.session.NoCurrentJobException;

@RestController
public class SuggestionsController {

    @Autowired
    private Job job;

    @RequestMapping(value = "/suggestions", method = RequestMethod.GET)
    public List<SuggestionsDto> getSuggestions(@RequestParam(value = "amount", defaultValue = "10") int amount)
	    throws NoCurrentJobException, IOException {
	return job.getSuggestions();
    }

}
