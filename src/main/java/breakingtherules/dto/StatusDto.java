package breakingtherules.dto;

import breakingtherules.firewall.Rule;

/**
 * Holds the status of a certain job - information about the original rule and
 * the progress that has already been made
 */
public class StatusDto {

    private Rule m_originalRule;

    private int m_createdRulesCount;

    private int m_totalHitsCount;

    private int m_coveredHitsCount;

    private int m_filteredHitsCount;

    /**
     * 
     * @param orig
     *            the original rule of the job
     * @param createdRules
     *            the number of rules created in the job
     * @param totalHits
     *            the number of all the hits in the job (original input)
     * @param coveredHits
     *            the number of all the hits covered by new rules
     * @param filteredHits
     *            the number of hits that pass the current filter
     */
    public StatusDto(Rule orig, int createdRules, int totalHits, int coveredHits, int filteredHits) {
	m_originalRule = orig;
	m_createdRulesCount = createdRules;
	m_totalHitsCount = totalHits;
	m_coveredHitsCount = coveredHits;
	m_filteredHitsCount = filteredHits;
    }

    /**
     * @return the original rule of the job
     */
    public Rule getOriginalRule() {
	return m_originalRule;
    }

    /**
     * @return the number of rules created in the job
     */
    public int getCreatedRulesCount() {
	return m_createdRulesCount;
    }

    /**
     * @return the number of all the hits in the job (original input)
     */
    public int getTotalHitsCount() {
	return m_totalHitsCount;
    }

    /**
     * @return the number of all the hits covered by new rules
     */
    public int getCoveredHitsCount() {
	return m_coveredHitsCount;
    }

    /**
     * @return the number of hits that pass the current filter
     */
    public int getFilteredHitsCount() {
	return m_filteredHitsCount;
    }

}
