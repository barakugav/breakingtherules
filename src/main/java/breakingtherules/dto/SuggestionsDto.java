package breakingtherules.dto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.services.algorithm.Suggestion;

/**
 * The SuggestionsDto class is a DTO that hold a list of suggestions of a
 * specific type.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class SuggestionsDto {

    /**
     * List of the suggestions.
     */
    private final List<Suggestion> m_suggestions;

    /**
     * The type of the suggestions in the list.
     */
    private final AttributeType m_type;

    /**
     * Construct new SuggestionsDto.
     * 
     * @param suggestions
     *            list of the suggestions this DTO will hold.
     * @param type
     *            the type of the suggestion in the list.
     * @throws NullPointerException
     *             if the suggestion list is null.
     */
    public SuggestionsDto(final List<Suggestion> suggestions, final AttributeType type) {
	m_suggestions = Collections.unmodifiableList(suggestions);
	m_type = Objects.requireNonNull(type);
    }

    /**
     * Get list of suggestions this DTO holds.
     * <p>
     * 
     * @return list of suggestions of this DTO (unmodifiable).
     */
    public List<Suggestion> getSuggestions() {
	return m_suggestions;
    }

    /**
     * Get the type of the suggestions this DTO holds
     * 
     * @return type of suggestions.
     */
    public AttributeType getType() {
	return m_type;
    }

}
