package breakingtherules.session;

import java.util.List;

import breakingtherules.algorithms.Suggestion;
import breakingtherules.firewall.Attribute;

public class AttributeSuggestions {

    
    Attribute.AttType m_type;
    
    List<Suggestion> m_suggestions;
    
    
    public AttributeSuggestions(Attribute.AttType type) {
	m_type = type;
    }
    
    
    /** 
     * Uses class Algorithm to update
     */
    public void update(){
	
    }
    
    
}
