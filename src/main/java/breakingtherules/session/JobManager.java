package breakingtherules.session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import breakingtherules.dao.DaoConfig;
import breakingtherules.dao.HitsDao;
import breakingtherules.dao.ParseException;
import breakingtherules.dao.RulesDao;
import breakingtherules.dao.csv.CSVDaoConfig;
import breakingtherules.dao.csv.CSVHitsDao;
import breakingtherules.dao.xml.XMLDaoConfig;
import breakingtherules.dao.xml.XMLRulesDao;
import breakingtherules.dto.ListDto;
import breakingtherules.dto.SuggestionsDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Attribute.AttributeType;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;

/**
 * This class manages the current job and allows to create new jobs. One of
 * these objects exists for each active session, so that each session can have
 * one job.
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class JobManager {

    /**
     * DAO of the job's hits
     */
    @Autowired
    @Qualifier("hitsDao")
    private HitsDao m_hitsDao;

    /**
     * DAO of the job's rules
     */
    @Autowired
    private RulesDao m_rulesDao;

    /**
     * Algorithm for suggesting rules suggestions
     */
    @Autowired
    private SuggestionsAlgorithm m_algorithm;

    /**
     * Name of the job.
     * <p>
     * Every job has a unique name. In the start of the session, there is no
     * active job, so the name is set to NO_CURRENT_JOB in the constructor.
     */
    private String m_name;

    /**
     * The current hit filter.
     * <p>
     * This is the filter that the user inputs, and receives only the hits that
     * match this filter.
     */
    private Filter m_filter;

    /**
     * The original rule that needs to be broken down to smaller rules
     */
    private Rule m_originalRule;

    /**
     * The rules that were created by the user throughout the program usage.
     */
    private List<StatisticedRule> m_rules;

    /**
     * A list of all the attributes that this job holds for each hit/rule. To be
     * used ONLY by getAllAttributeTypes()
     */
    private AttributeType[] m_allAttributeTypes;

    /**
     * The number of hits given as input for the job
     */
    private int m_totalHitsCount;

    /**
     * The number of hits covered by created rules
     */
    private int m_coveredHitsCount;

    /**
     * The number of hits that pass the current filter (and don't match any
     * created rule)
     */
    private int m_filteredHitsCount;

    /**
     * Name constant that represent that the name wasn't set yet
     */
    private static final String NO_CURRENT_JOB = null;

    /**
     * Construct new rule.
     * <p>
     * Set name to {@link JobManager#NO_CURRENT_JOB}.
     */
    public JobManager() {
	m_name = NO_CURRENT_JOB;
    }

    /**
     * Create a new job, with the given job name and the given hits
     * 
     * @param jobName
     *            The name of the new job. Must be different from existing names
     * @param hitsFile
     *            A CSV file with all of the hits that should be processed in
     *            this job
     * @param columnTypes
     *            The order of the columns in the CSV file
     * @param originalRule
     *            The original rule of the job
     * @throws IOException
     *             if any I/O errors occurs.
     * @throws ParseException
     *             if failed to parse file.
     * @throws NullPointerException
     *             if the file is null.
     */
    public void createJob(final String jobName, final MultipartFile hitsFile, final List<Integer> columnTypes,
	    Rule originalRule) throws IOException, ParseException {

	// TODO - treat return value from initRepository.
	DaoConfig.initRepository(jobName);

	final File fileDestination = new File(new File(CSVDaoConfig.getHitsFile(jobName)).getAbsolutePath());
	hitsFile.transferTo(fileDestination);
	final CSVHitsDao csvDao = new CSVHitsDao();
	csvDao.setColumnTypes(columnTypes);
	final Iterable<Hit> hits = csvDao.getHits(jobName, Collections.emptyList(), Filter.ANY_FILTER);
	m_hitsDao.initJob(jobName, hits);

	final JobManager newJobManager = new JobManager();
	newJobManager.m_name = jobName;
	newJobManager.m_originalRule = originalRule;
	newJobManager.m_rules = new ArrayList<>();
	newJobManager.updateRulesFile();
	throw new RuntimeException("WHY");
    }

    /**
     * Restore all relevant parameters about the job to be worked on
     * 
     * @param name
     *            The name of the job that needs to be worked on.
     * @throws IOException
     *             if DAO failed to load data
     * @throws ParseException
     *             if any parse errors occurs in the data.
     */
    public synchronized void setJob(final String name) throws IOException, ParseException {
	m_name = Objects.requireNonNull(name);
	m_originalRule = m_rulesDao.getOriginalRule(name);
	m_rules = new ArrayList<>();
	m_filter = Filter.ANY_FILTER;
	m_totalHitsCount = m_hitsDao.getHitsNumber(name, new ArrayList<Rule>(), Filter.ANY_FILTER);
	m_coveredHitsCount = 0;
	m_filteredHitsCount = m_totalHitsCount;
	m_allAttributeTypes = null;

	final List<Rule> rules = m_rulesDao.getRules(name).getData();
	for (final Rule rule : rules) {
	    addRule(rule);
	}

    }

    /**
     * Get the name of the job
     * 
     * @return the job's name
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public synchronized String getName() {
	checkJobState();
	return m_name;
    }

    /**
     * Get the current filter of this job
     * 
     * @return current filter of this job
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public synchronized Filter getFilter() {
	checkJobState();
	return m_filter;
    }

    /**
     * Get the original rule of this job
     * 
     * @return The original rule of the job
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public Rule getOriginalRule() {
	checkJobState();
	return m_originalRule;
    }

    /**
     * Get a list of all the rules that are active in this job
     * 
     * @return All the rules for the current job.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public synchronized List<Rule> getRules() {
	checkJobState();
	final List<Rule> rules = new ArrayList<>(m_rules.size());
	for (final StatisticedRule rule : m_rules) {
	    rules.add(rule.m_rule);
	}
	return rules;
    }

    /**
     * Delete a rule from the job, by its index.
     * 
     * @param ruleIndex
     *            the index of the rule to delete, counting from 0, out of all
     *            the created rules
     * @throws IndexOutOfBoundsException
     *             if index of rule is out of bounds
     * @throws IOException
     *             if any I/O error occurs.
     * @throws ParseException
     *             if any parse errors occurs in the data.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public synchronized void deleteRule(final int ruleIndex) throws IOException, ParseException {
	checkJobState();
	if (ruleIndex < 0 || ruleIndex >= m_rules.size()) {
	    throw new IndexOutOfBoundsException("rule index=" + ruleIndex + ", numer of rules=" + m_rules.size());
	}

	// Remove all rules up to searched rule
	final List<Rule> removedRules = new ArrayList<>(m_rules.size() - ruleIndex - 1);
	for (final ListIterator<StatisticedRule> it = m_rules.listIterator(m_rules.size()); it
		.previousIndex() < ruleIndex;) {
	    final StatisticedRule removedRule = it.previous();
	    m_coveredHitsCount -= removedRule.m_coveredHits;
	    removedRules.add(removedRule.m_rule);
	    it.remove();
	}
	// Reverse so order is as insertion order
	Collections.reverse(removedRules);

	// Remove searched rule
	final StatisticedRule searchedRule = m_rules.get(ruleIndex);
	m_rules.remove(ruleIndex);

	// Update filtered hits count
	m_coveredHitsCount -= searchedRule.m_coveredHits;
	m_filteredHitsCount = m_hitsDao.getHitsNumber(m_name, getRules(), m_filter);

	// Add again removed rules
	for (final Rule removedRule : removedRules) {
	    addRule(removedRule);
	}

	updateRulesFile();

    }

    /**
     * Get hits of the job in range [startIndex, endIndex] that match the filter
     * and rules
     * 
     * @param startIndex
     *            the start index of the hits list, including this index
     * @param endIndex
     *            the end index of the hits list, including this index
     * @return DTO object that hold list of requested hits
     * @throws IOException
     *             if DAO failed to load data
     * @throws ParseException
     *             if any parse errors occurs in the data.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public ListDto<Hit> getHits(int startIndex, int endIndex) throws IOException, ParseException {
	checkJobState();
	return m_hitsDao.getHitsList(m_name, getRules(), m_filter, startIndex, endIndex);
    }

    /**
     * 
     * Get all the suggestions computed by the algorithm.
     * 
     * @param amount
     *            Maximum number of suggestions to return for each attribute
     *            type
     * 
     * @return Current job's suggestions.
     * @throws IOException
     *             if any I/O errors occurs in DAO.
     * @throws ParseException
     *             if any parse errors occurs in DAO.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public synchronized List<SuggestionsDto> getSuggestions(final int amount) throws IOException, ParseException {
	checkJobState();

	final AttributeType[] allAttributesType = getAllAttributeTypes();
	final List<Suggestion>[] suggestions = m_algorithm.getSuggestions(m_name, getRules(), m_filter, amount,
		allAttributesType);
	final List<SuggestionsDto> suggestionsDtos = new ArrayList<>();
	for (int i = 0; i < allAttributesType.length; i++) {
	    suggestionsDtos.add(new SuggestionsDto(suggestions[i], allAttributesType[i]));
	}

	return suggestionsDtos;
    }

    /**
     * Set the filter of this job to a new filter
     * 
     * @param filter
     *            new filter of this job
     * @throws IOException
     *             if any I/O error occurs
     * @throws ParseException
     *             if any parse errors occurs in the data.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public synchronized void setFilter(final Filter filter) throws IOException, ParseException {
	checkJobState();
	m_filter = Objects.requireNonNull(filter);
	m_filteredHitsCount = m_hitsDao.getHitsNumber(m_name, getRules(), m_filter);
    }

    /**
     * Uses one of the job's rules to decipher the different attributes that
     * this job has for each hit/rule. Is calculated once and kept in
     * allAttributeTypes
     * 
     * @return E.x. ["Source", "Destination", "Service"]
     */
    private AttributeType[] getAllAttributeTypes() {
	if (m_allAttributeTypes == null) {
	    List<AttributeType> allAttributeTypes = new ArrayList<>();
	    for (final Attribute att : m_originalRule.getAttributes()) {
		allAttributeTypes.add(att.getType());
	    }
	    m_allAttributeTypes = allAttributeTypes.toArray(new AttributeType[allAttributeTypes.size()]);
	}
	return m_allAttributeTypes;
    }

    /**
     * Takes the current filter and adds it as a new rule
     * 
     * @throws IOException
     *             if any I/O errors occurs in the data.
     * @throws ParseException
     *             if any parse errors occurs in the data.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public void addCurrentFilterToRules() throws IOException, ParseException {
	checkJobState();

	final Rule newRule = new Rule(m_filter);
	addRule(new StatisticedRule(newRule, m_filteredHitsCount));
    }

    /**
     * Add a rule to the job's rules list.
     * 
     * @param newRule
     *            the added rule.
     * @throws IOException
     *             if any I/O errors occurs when trying to update statistics.
     * @throws ParseException
     *             if any parse errors occurs when trying to update statistics.
     */
    private synchronized void addRule(final Rule newRule) throws IOException, ParseException {
	List<Rule> newRules = getRules();
	newRules.add(newRule);

	final int newUncoveredHitsCount = m_hitsDao.getHitsNumber(m_name, newRules, Filter.ANY_FILTER);
	final int oldUncoveredHitsCount = m_totalHitsCount - m_coveredHitsCount;
	final int ruleCoveredHits = oldUncoveredHitsCount - newUncoveredHitsCount;
	addRule(new StatisticedRule(newRule, ruleCoveredHits));
    }

    /**
     * Add a rule (already with calculated statistics) to the job's rules list.
     * 
     * @param newRule
     *            the added rule (already with calculated statistics).
     * @throws IOException
     *             if any I/O errors occurs when trying to update statistics.
     * @throws ParseException
     *             if any parse errors occurs when trying to update statistics.
     */
    private synchronized void addRule(final StatisticedRule newRule) throws IOException, ParseException {
	m_coveredHitsCount += newRule.m_coveredHits;
	m_rules.add(newRule);
	m_filteredHitsCount = m_hitsDao.getHitsNumber(m_name, getRules(), m_filter);
	updateRulesFile();
    }

    /**
     * Get the total number of hits that was given as input for this job
     * 
     * @return The number of hits given as input for the job.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public int getTotalHitsCount() {
	checkJobState();
	return m_totalHitsCount;
    }

    /**
     * Get the number of hits covered by the created rules
     * 
     * @return The number of hits covered by the created rules.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public int getCoveredHitsCount() {
	checkJobState();
	return m_coveredHitsCount;
    }

    /**
     * Get the number of hits that pass the current filter (and don't match any
     * created rule
     * 
     * @return The number of hits that pass the current filter (and don't match
     *         any created rule).
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public int getFilteredHitsCount() {
	checkJobState();
	return m_filteredHitsCount;
    }

    /**
     * Creates (or returns an existing) XML file with all of the job's rules.
     * Can be downloaded by the user.
     * 
     * @return A file of XML format with this job's rules.
     * @throws NoCurrentJobException
     *             if the job wasn't set yet.
     */
    public FileSystemResource getRulesFile() {
	checkJobState();
	// TODO - this is not generic
	return new FileSystemResource(XMLDaoConfig.getRulesFile(m_name));
    }

    /**
     * Update the rules file to match the current rules list in the job.
     * 
     * @throws IOException
     *             if any I/O errors occurs during the file update.
     */
    private void updateRulesFile() throws IOException {
	// TODO - this is not generic
	final String fileName = XMLDaoConfig.getRulesFile(m_name);
	final List<Rule> rules = getRules();
	XMLRulesDao.writeRules(fileName, rules, m_originalRule);
    }

    /**
     * Check that the current state of the job is ready to be used.
     * 
     * @throws NoCurrentJobException
     *             if the state is invalid.
     */
    private void checkJobState() {
	if (Objects.equals(m_name, NO_CURRENT_JOB)) {
	    throw new NoCurrentJobException("Job wasn't set yet");
	}
    }

    /**
     * Wrapper to a rule, save some statistics about the rule.
     * 
     * @author Barak Ugav
     * @author Yishai Gronich
     *
     * @see Rule
     */
    private static class StatisticedRule {

	/**
	 * The rule itself.
	 */
	final Rule m_rule;

	/**
	 * Number of covered hits by the rule.
	 */
	final int m_coveredHits;

	/**
	 * Construct new StatisticedRule.
	 * 
	 * @param rule
	 *            a rule.
	 * @param coveredHits
	 *            number of covered hits by the rule.
	 * @throws NullPointerException
	 *             if the rule is null.
	 * @throws IllegalArgumentException
	 *             if the {@code coveredHits} is negative.
	 */
	StatisticedRule(final Rule rule, final int coveredHits) {
	    if (coveredHits < 0) {
		throw new IllegalArgumentException("coveredHits < 0: " + coveredHits);
	    }
	    m_rule = Objects.requireNonNull(rule);
	    m_coveredHits = coveredHits;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object o) {
	    if (o == this) {
		return true;
	    }
	    if (!(o instanceof StatisticedRule)) {
		return false;
	    }

	    final StatisticedRule other = (StatisticedRule) o;
	    return m_rule.equals(other.m_rule);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
	    return m_rule.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
	    return m_rule.toString() + " (covered hits=" + m_coveredHits + ")";
	}

    }

}
