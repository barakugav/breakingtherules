package breakingtherules.session;

import java.util.List;

import breakingtherules.firewall.Attribute.AttType;
import breakingtherules.services.algorithms.Suggestion;

public class AttributeSuggestions {

    private AttType m_type;

    private List<Suggestion> m_suggestions;

    public AttributeSuggestions(AttType type) {
	m_type = type;
    }

    /**
     * Uses class Algorithm to update
     */
    public void update() {

    }

}
