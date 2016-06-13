package breakingtherules.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import javax.xml.parsers.ParserConfigurationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.RulesDao;
import breakingtherules.dao.xml.RulesXmlDao;
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
     * Id of the job
     * 
     * Every job has a unique id. In the start of the session, there is no
     * active job, so the id is set to NO_CURRENT_JOB in the constructor.
     */
    private int m_id;

    /**
     * The current hit filter
     * 
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
    private List<String> m_allAttributeTypes;

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
    private static final int NO_CURRENT_JOB = -1;

    /************************************************/

    /**
     * Constructor
     * 
     * set id to {@link Job#NO_CURRENT_JOB}
     */
    public Job() {
	m_id = NO_CURRENT_JOB;
    }

    /**
     * Restore all relevant parameters about the job to be worked on
     * 
     * @param id
     *            The id of the job that needs to be worked on.
     * @throws IOException
     *             if DAO failed to load data
     */
    public void setJob(int id) throws IOException {
	m_id = id;
	m_originalRule = m_rulesDao.getOriginalRule(id);
	m_rules = new ArrayList<>();
	m_filter = Filter.ANY_FILTER;
	m_totalHitsCount = m_hitsDao.getHitsNumber(id, new ArrayList<Rule>(), Filter.ANY_FILTER);
	m_coveredHitsCount = 0;
	m_filteredHitsCount = m_totalHitsCount;

	List<Rule> rules = m_rulesDao.getRules(id).getData();
	for (Rule rule : rules) {
	    addRule(rule);
	}
    }

    /**
     * Get the id of the job
     * 
     * @return the job's Id
     */
    public int getId() {
	return m_id;
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
	List<Rule> rules = new ArrayList<>(m_rules.size());
	for (StatisticedRule rule : m_rules)
	    rules.add(rule.m_rule);
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
     */
    public void deleteRule(int ruleIndex) throws IOException {
	if (ruleIndex < 0 || ruleIndex >= m_rules.size())
	    throw new IndexOutOfBoundsException("rule index=" + ruleIndex + ", numer of rules=" + m_rules.size());

	// Remove all rules up to searched rule
	List<Rule> removedRules = new ArrayList<>(m_rules.size() - ruleIndex - 1);
	for (ListIterator<StatisticedRule> it = m_rules.listIterator(m_rules.size()); it.previousIndex() < ruleIndex;) {
	    StatisticedRule removedRule = it.previous();
	    m_coveredHitsCount -= removedRule.m_coveredHits;
	    removedRules.add(removedRule.m_rule);
	    it.remove();
	}
	// Reverse so order is as insertion order
	Collections.reverse(removedRules);

	// Remove searched rule
	StatisticedRule searchedRule = m_rules.get(ruleIndex);
	m_rules.remove(ruleIndex);

	// Update filtered hits count
	m_coveredHitsCount -= searchedRule.m_coveredHits;
	m_filteredHitsCount = m_hitsDao.getHitsNumber(m_id, getRules(), m_filter);

	// Add again removed rules
	for (Rule removedRule : removedRules) {
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
     */
    public ListDto<Hit> getHits(int startIndex, int endIndex) throws IOException {
	checkJobState();
	return m_hitsDao.getHits(m_id, getRules(), m_filter, startIndex, endIndex);
    }

    /**
     * Get all the suggestions computed by the algorithm.
     * 
     * @param amount
     *            Maximum number of suggestions to return for each attribute
     *            type
     * 
     * @return Current job's suggestions.
     * @throws Exception
     *             if any error occurs
     */
    public List<SuggestionsDto> getSuggestions(final int amount) throws Exception {
	checkJobState();

	List<SuggestionsDto> suggestionsDtos = new ArrayList<>();
	List<String> allAttributesType = getAllAttributeTypes();
	for (String attType : allAttributesType) {
	    List<Suggestion> suggestions = m_algorithm.getSuggestions(m_hitsDao, m_id, getRules(), m_filter, amount,
		    attType);
	    SuggestionsDto attSuggestions = new SuggestionsDto(suggestions, attType);
	    suggestionsDtos.add(attSuggestions);
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
     */
    public void setFilter(Filter filter) throws IOException {
	checkJobState();
	m_filter = filter;
	m_filteredHitsCount = m_hitsDao.getHitsNumber(m_id, getRules(), m_filter);
    }

    /**
     * Uses one of the job's rules to decipher the different attributes that
     * this job has for each hit/rule. Is calculated once and kept in
     * allAttributeTypes
     * 
     * @return E.x. ["Source", "Destination", "Service"]
     */
    private List<String> getAllAttributeTypes() {
	checkJobState();
	if (m_allAttributeTypes == null) {
	    m_allAttributeTypes = new ArrayList<>();
	    Rule demoRule = m_originalRule;
	    for (Attribute att : demoRule) {
		m_allAttributeTypes.add(att.getType());
	    }
	}
	return m_allAttributeTypes;
    }

    /**
     * Takes the current filter and adds it as a new rule
     * 
     * @throws IOException
     */
    public void addCurrentFilterToRules() throws IOException {
	checkJobState();

	Rule newRule = new Rule(m_filter);
	addRule(new StatisticedRule(newRule, m_filteredHitsCount));
    }

    private void addRule(Rule newRule) throws IOException {
	List<Rule> newRules = getRules();
	newRules.add(newRule);

	int newUncoveredHitsCount = m_hitsDao.getHitsNumber(m_id, newRules, Filter.ANY_FILTER);
	int oldUncoveredHitsCount = m_totalHitsCount - m_coveredHitsCount;
	int ruleCoveredHits = oldUncoveredHitsCount - newUncoveredHitsCount;
	addRule(new StatisticedRule(newRule, ruleCoveredHits));
    }

    private void addRule(StatisticedRule newRule) throws IOException {
	m_coveredHitsCount += newRule.m_coveredHits;
	m_rules.add(newRule);
	m_filteredHitsCount = m_hitsDao.getHitsNumber(m_id, getRules(), m_filter);
	updateRulesFile();
    }

    /**
     * Get the total number of hits that was given as input for this job
     * 
     * @return The number of hits given as input for the job
     */
    public int getTotalHitsCount() {
	return m_totalHitsCount;
    }

    /**
     * Get the number of hits covered by the created rules
     * 
     * @return The number of hits covered by the created rules
     */
    public int getCoveredHitsCount() {
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
	return m_filteredHitsCount;
    }
    
    public String getRulesFilePath() {
	return "./repository/" + m_id + "/" + RulesXmlDao.REPOSITORY_NAME;
    }

    private void updateRulesFile() throws IOException {
	String repositoryPath = getRulesFilePath();
	List<Rule> rawRules = new ArrayList<Rule>();
	for (StatisticedRule statRule : m_rules)
	    rawRules.add(statRule.getRule());
	try {
	    RulesXmlDao.writeRules(repositoryPath, rawRules, m_originalRule);
	} catch (ParserConfigurationException e) {
	    throw new IOException("Parser configuration problems.");
	}
    }

    private void checkJobState() {
	if (m_id == NO_CURRENT_JOB) {
	    throw new NoCurrentJobException("Job wasn't set yet");
	}
    }

    private static class StatisticedRule {

	private final Rule m_rule;
	private final int m_coveredHits;

	public StatisticedRule(final Rule rule, final int coveredHits) {
	    if (coveredHits < 0)
		throw new IllegalArgumentException("coveredHits < 0: " + coveredHits);
	    m_rule = Objects.requireNonNull(rule);
	    m_coveredHits = coveredHits;
	}

	public Rule getRule() {
	    return m_rule;
	}

	@Override
	public boolean equals(Object o) {
	    if (o == this) {
		return true;
	    }
	    return o instanceof StatisticedRule && m_rule.equals(((StatisticedRule) o).m_rule);
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
