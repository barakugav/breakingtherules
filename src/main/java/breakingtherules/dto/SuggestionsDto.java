package breakingtherules.dto;

import java.util.List;

import breakingtherules.services.algorithm.Suggestion;

/**
 * The SuggestionsDto class is a DTO that hold a list of suggestions of a
 * specific type
 */
public class SuggestionsDto {

    /**
     * List of suggestions
     */
    private final List<Suggestion> m_suggestions;

    /**
     * The type of the suggestions in the list
     */
    private final String m_type;

    /**
     * Constructor
     * 
     * @param suggestions
     *            list of the suggestions this DTO will hold
     * @param type
     *            the type of the suggestion in the list
     */
    public SuggestionsDto(final List<Suggestion> suggestions, final String type) {
	m_suggestions = suggestions;
	m_type = type;
    }

    /**
     * Get list of suggestions this DTO holds
     * 
     * @return list of suggestions of this DTO
     */
    public List<Suggestion> getSuggestions() {
	return m_suggestions;
    }

    /**
     * Get the type of the suggestions this DTO holds
     * 
     * @return type of suggestions
     */
    public String getType() {
	return m_type;
    }

}
