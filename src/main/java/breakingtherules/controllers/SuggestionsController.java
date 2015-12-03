package breakingtherules.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import breakingtherules.dao.HitsDao;
import breakingtherules.services.algorithms.Suggestion;
import breakingtherules.services.algorithms.SuggestionsAlgorithm;

@RestController
public class SuggestionsController {

    @Autowired
    private SuggestionsAlgorithm m_algorithm;

    @Autowired
    private HitsDao m_hitsDao;

    @RequestMapping(value = "/suggestions", method = RequestMethod.GET)
    public List<Suggestion> getSuggestions() {
	return new ArrayList<Suggestion>();
    }

}
