package breakingtherules.session;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.firewall.Hit;
import breakingtherules.services.algorithms.Suggestion;
import breakingtherules.services.algorithms.SuggestionsAlgorithm;


public class AttributeSuggestions {

    @JsonProperty("type")
    private String m_type;

    @JsonProperty("suggestions")
    private List<Suggestion> m_suggestions;

    public AttributeSuggestions(String type) {
	m_type = type;
	m_suggestions = new ArrayList<Suggestion>();
    }

    /**
     * Uses class Algorithm to update
     */
    public void update(SuggestionsAlgorithm alg, List<Hit> hits) {
	m_suggestions = alg.getSuggestions(hits, m_type);
    }

}
