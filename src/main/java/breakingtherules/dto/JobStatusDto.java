package breakingtherules.dto;

import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Rule;

/**
 * Holds the status of a certain job - information about the original rule and
 * the progress that has already been made.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 * 
 */
public class JobStatusDto {

    /**
     * The original rule of the job.
     */
    private final Rule m_originalRule;

    /**
     * Number of created rules.
     */
    private final int m_createdRulesCount;

    /**
     * Number of total hits in the job.
     */
    private final int m_totalHitsCount;

    /**
     * Number of covered hits by the created rules.
     */
    private final int m_coveredHitsCount;

    /**
     * Number of filtered hits by the current filter of the uncovered hits.
     */
    private final int m_filteredHitsCount;

    /**
     * The current filter of the job
     */
    private final Filter m_filter;

    /**
     * Construct new JobStatusDto.
     * 
     * @param original
     *            the original rule of the job.
     * @param createdRules
     *            the number of rules created in the job.
     * @param totalHits
     *            the number of all the hits in the job (original input).
     * @param coveredHits
     *            the number of all the hits covered by new rules.
     * @param filteredHits
     *            the number of hits that pass the current filter.
     * @param filter
     *            Current filter of the job
     */
    public JobStatusDto(final Rule original, final int createdRules, final int totalHits, final int coveredHits,
	    final int filteredHits, Filter filter) {
	m_originalRule = original;
	m_createdRulesCount = createdRules;
	m_totalHitsCount = totalHits;
	m_coveredHitsCount = coveredHits;
	m_filteredHitsCount = filteredHits;
	m_filter = filter;
    }

    /**
     * Get the original rule.
     * 
     * @return the original rule of the job.
     */
    public Rule getOriginalRule() {
	return m_originalRule;
    }

    /**
     * Get the number of created rules.
     * 
     * @return the number of rules created in the job.
     */
    public int getCreatedRulesCount() {
	return m_createdRulesCount;
    }

    /**
     * Get the total number of hits in the job.
     * 
     * @return the number of all the hits in the job (original input).
     */
    public int getTotalHitsCount() {
	return m_totalHitsCount;
    }

    /**
     * Get the number of covered hits by the created rules out of the total
     * number of hits.
     * 
     * @return the number of all the hits covered by new rules.
     */
    public int getCoveredHitsCount() {
	return m_coveredHitsCount;
    }

    /**
     * Get the number of filtered hits by the current filter out of the
     * uncovered hits.
     * 
     * @return the number of hits that pass the current filter.
     */
    public int getFilteredHitsCount() {
	return m_filteredHitsCount;
    }

    /**
     * @return The current filter of the job
     */
    public Filter getFilter() {
	return m_filter;
    }

}
