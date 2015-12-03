package breakingtherules.session;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class JobSuggestions {

    /**
     * A suggestion object for each attribute.
     */
    private List<AttributeSuggestions> m_attributeSuggestions;

    @Autowired
    private Job m_job;

    public JobSuggestions() {
	m_attributeSuggestions = new ArrayList<AttributeSuggestions>();
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
