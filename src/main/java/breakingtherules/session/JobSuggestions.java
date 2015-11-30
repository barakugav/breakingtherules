package breakingtherules.session;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class JobSuggestions {

    /**
     * A suggestion object for each attribute.
     */
    List<AttributeSuggestions> m_attribute_suggestions;

    @Autowired
    Job job;

    public JobSuggestions() {
	m_attribute_suggestions = new ArrayList<AttributeSuggestions>();	
    }

    /**
     * Update the suggestions for each attribute
     */
    public void update() {
	for (AttributeSuggestions attr_sug : m_attribute_suggestions) {
	    attr_sug.update();
	}
    }

}
