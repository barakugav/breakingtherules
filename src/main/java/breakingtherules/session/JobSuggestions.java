package breakingtherules.session;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import breakingtherules.firewall.Attribute.AttType;

public class JobSuggestions {

    /**
     * A suggestion object for each attribute.
     */
    @JsonProperty("suggestions")
    private List<AttributeSuggestions> m_attributeSuggestions;

    @JsonIgnore
    private Job m_job;
    

    public JobSuggestions(Job job) {
	m_job = job;
	System.out.println(m_job.getAllAttributeTypes());
	m_attributeSuggestions = new ArrayList<AttributeSuggestions>();
	for (AttType att : m_job.getAllAttributeTypes()) {
	    m_attributeSuggestions.add(new AttributeSuggestions(m_job, att));
	}
    }

    /**
     * Update the suggestions for each attribute
     */
    public void update() {
	for (AttributeSuggestions attr_sug : m_attributeSuggestions) {
	    attr_sug.update();
	}
    }

}
