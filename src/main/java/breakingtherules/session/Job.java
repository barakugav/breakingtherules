package breakingtherules.session;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
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
import breakingtherules.dao.xml.XMLRulesDao;
import breakingtherules.dto.ListDto;
import breakingtherules.dto.SuggestionsDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Job {

    /**
     * DAO of the job's hits
     */
    @Autowired
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
    private String[] m_allAttributeTypes;

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
     * Id constant that represent that the id wasn't set yet
     */
    private static final String NO_CURRENT_JOB = null;

    /************************************************/

    /**
     * Construct new rule.
     * <p>
     * Set name to {@link Job#NO_CURRENT_JOB}.
     */
    public Job() {
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
     * @throws IOException
     * @throws ParseException
     */
    public void createJob(String jobName, MultipartFile hitsFile, List<Integer> columnTypes)
	    throws IOException, ParseException {
	DaoConfig.initRepository(jobName);

	File fileDestination = new File(new File(CSVDaoConfig.getHitsFile(jobName)).getAbsolutePath());
	hitsFile.transferTo(fileDestination);
	List<Hit> hits = new CSVHitsDao().getHitsList(jobName, new ArrayList<Rule>(), Filter.ANY_FILTER).getData();
	m_hitsDao.initJob(jobName, hits);

	Job job = new Job();
	job.m_name = jobName;
	job.m_originalRule = new Rule(Filter.ANY_FILTER);
	job.m_rules = new ArrayList<>();
	job.updateRulesFile();
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
    public void setJob(final String name) throws IOException, ParseException {
	synchronized (this) {
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
    }

    /**
     * Get the name of the job
     * 
     * @return the job's name
     */
    public String getName() {
	checkJobState();
	return m_name;
    }

    /**
     * Get the current filter of this job
     * 
     * @return current filter of this job
     */
    public Filter getFilter() {
	checkJobState();
	return m_filter;
    }

    /**
     * Get the original rule of this job
     * 
     * @return The original rule of the job
     */
    public Rule getOriginalRule() {
	checkJobState();
	return m_originalRule;
    }

    /**
     * Get a list of all the rules that are active in this job
     * 
     * @return All the rules for the current job
     */
    public List<Rule> getRules() {
	checkJobState();
	final List<Rule> rules = new ArrayList<>(m_rules.size());
	for (final StatisticedRule rule : m_rules) {
	    rules.add(rule.m_rule);
	}
	return rules;
    }

    /**
     * Delete a rule from the job, by its id
     * 
     * @param ruleIndex
     *            the index of the rule to delete, counting from 0, out of all
     *            the created rules
     * @throws IndexOutOfBoundsException
     *             if index of rule is out of bounds
     * @throws IOException
     *             if any I/O error occurs
     * @throws ParseException
     *             if any parse errors occurs in the data.
     */
    public void deleteRule(final int ruleIndex) throws IOException, ParseException {
	synchronized (this) {
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
     */
    public List<SuggestionsDto> getSuggestions(final int amount) throws IOException, ParseException {
	checkJobState();

	final String[] allAttributesType = getAllAttributeTypes();
	final List<Suggestion>[] suggestions = m_algorithm.getSuggestions(m_hitsDao, m_name, getRules(), m_filter,
		amount, allAttributesType);
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
     */
    public void setFilter(final Filter filter) throws IOException, ParseException {
	checkJobState();
	synchronized (this) {
	    m_filter = Objects.requireNonNull(filter);
	    m_filteredHitsCount = m_hitsDao.getHitsNumber(m_name, getRules(), m_filter);
	}
    }

    /**
     * Uses one of the job's rules to decipher the different attributes that
     * this job has for each hit/rule. Is calculated once and kept in
     * allAttributeTypes
     * 
     * @return E.x. ["Source", "Destination", "Service"]
     */
    private String[] getAllAttributeTypes() {
	if (m_allAttributeTypes == null) {
	    List<String> allAttributeTypes = new ArrayList<>();
	    for (final Attribute att : m_originalRule) {
		allAttributeTypes.add(att.getType());
	    }
	    m_allAttributeTypes = allAttributeTypes.toArray(new String[allAttributeTypes.size()]);
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
     */
    public void addCurrentFilterToRules() throws IOException, ParseException {
	checkJobState();

	final Rule newRule = new Rule(m_filter);
	addRule(new StatisticedRule(newRule, m_filteredHitsCount));
    }

    private void addRule(final Rule newRule) throws IOException, ParseException {
	synchronized (this) {
	    List<Rule> newRules = getRules();
	    newRules.add(newRule);

	    final int newUncoveredHitsCount = m_hitsDao.getHitsNumber(m_name, newRules, Filter.ANY_FILTER);
	    final int oldUncoveredHitsCount = m_totalHitsCount - m_coveredHitsCount;
	    final int ruleCoveredHits = oldUncoveredHitsCount - newUncoveredHitsCount;
	    addRule(new StatisticedRule(newRule, ruleCoveredHits));
	}
    }

    private void addRule(final StatisticedRule newRule) throws IOException, ParseException {
	synchronized (this) {
	    m_coveredHitsCount += newRule.m_coveredHits;
	    m_rules.add(newRule);
	    m_filteredHitsCount = m_hitsDao.getHitsNumber(m_name, getRules(), m_filter);
	    updateRulesFile();
	}
    }

    /**
     * Get the total number of hits that was given as input for this job
     * 
     * @return The number of hits given as input for the job
     */
    public int getTotalHitsCount() {
	checkJobState();
	return m_totalHitsCount;
    }

    /**
     * Get the number of hits covered by the created rules
     * 
     * @return The number of hits covered by the created rules
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
     *         any created rule)
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
     */
    public FileSystemResource getRulesFile() {
	checkJobState();
	return new FileSystemResource(DaoConfig.getRepoRoot(m_name) + XMLRulesDao.REPOSITORY_NAME);
    }

    private void updateRulesFile() throws IOException {
	String repositoryPath = DaoConfig.getRepoRoot(m_name) + XMLRulesDao.REPOSITORY_NAME;
	final List<Rule> rules = getRules();
	XMLRulesDao.writeRules(repositoryPath, rules, m_originalRule);
    }

    private void checkJobState() {
	if (Objects.equals(m_name, NO_CURRENT_JOB)) {
	    throw new NoCurrentJobException("Job wasn't set yet");
	}
    }

    private static class StatisticedRule {

	private final Rule m_rule;
	private final int m_coveredHits;

	public StatisticedRule(final Rule rule, final int coveredHits) {
	    if (coveredHits < 0) {
		throw new IllegalArgumentException("coveredHits < 0: " + coveredHits);
	    }
	    m_rule = Objects.requireNonNull(rule);
	    m_coveredHits = coveredHits;
	}

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

	@Override
	public int hashCode() {
	    return m_rule.hashCode();
	}

	@Override
	public String toString() {
	    return m_rule.toString() + " (covered hits=" + m_coveredHits + ")";
	}

    }

}
