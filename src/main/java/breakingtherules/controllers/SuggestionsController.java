package breakingtherules.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.algorithms.Suggestion;
import breakingtherules.algorithms.SuggestionsAlgorithm;
import breakingtherules.dao.HitsDao;

@RestController
public class SuggestionsController {

    @Autowired
    private SuggestionsAlgorithm algorithm;

    @Autowired
    private HitsDao hitsDao;

    @RequestMapping(value = "/suggestions", method = RequestMethod.GET)
    public List<Suggestion> getSuggestions() {
	return new ArrayList<Suggestion>();
    }

}
