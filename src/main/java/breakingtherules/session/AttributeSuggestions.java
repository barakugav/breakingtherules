package breakingtherules.session;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.firewall.Attribute.AttType;
import breakingtherules.services.algorithms.Suggestion;
import breakingtherules.services.algorithms.SuggestionsAlgorithm;

public class AttributeSuggestions {

    @JsonProperty("type")
    private AttType m_type;

    @JsonProperty("suggestions")
    private List<Suggestion> m_suggestions;
    
    private Job job;
    

    public AttributeSuggestions(Job job, AttType type) {
	this.job = job;
	m_type = type;
	m_suggestions = new ArrayList<Suggestion>();
    }

    /**
     * Uses class Algorithm to update
     */
    public void update() {
	m_suggestions = job.getAlgorithm().getSuggestions(job, m_type);
    }

}
