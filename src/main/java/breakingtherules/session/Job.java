package breakingtherules.session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.RulesDao;
import breakingtherules.dto.ListDto;
import breakingtherules.dto.SuggestionsDto;
import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.services.algorithm.Suggestion;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;
import breakingtherules.utilities.Utility;

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
     * All the rules of the current job
     * 
     * The first is the original base rule and is not modified. The others are
     * created with the user, constantly modified.
     */
    private List<Rule> m_rules;

    /**
     * A list of all the attributes that this job holds for each hit/rule. To be
     * used ONLY by getAllAttributeTypes()
     */
    private List<String> m_allAttributeTypes;

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
	m_rules = m_rulesDao.getRules(id).getData();
	m_filter = Filter.ANY_FILTER; // TODO Change to: previously used
				      // filter
    }

    /**
     * Get the job's Id
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
     * Get a list of all the rules that are active in this job
     * 
     * @return All the rules for the current job
     */
    public List<Rule> getRules() {
	checkJobState();
	return m_rules;
    }

    /**
     * Delete a rule from the job, by its id
     */
    public void deleteRule(int ruleId) {
	// TODO update DAO
	if (m_rules.get(0).getId() == ruleId)
	    return;
	m_rules.removeIf(r -> r.getId() == ruleId);
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
	return m_hitsDao.getHits(m_id, m_rules, m_filter, startIndex, endIndex);
    }

    /**
     * Get all hits that match the filter and the rules
     * 
     * @return DTO object that hold list of all hits
     * @throws IOException
     *             if DAO failed to load data
     */
    public ListDto<Hit> getHitsAll() throws IOException {
	checkJobState();
	return m_hitsDao.getHits(m_id, m_rules, m_filter);
    }

    /**
     * Get all the suggestions computed by the algorithm.
     * 
     * @return Current job's suggestions.
     * @throws IOException
     *             if DAO failed to load the job's hits
     */
    public List<SuggestionsDto> getSuggestions(int amount) throws IOException {
	checkJobState();

	List<SuggestionsDto> suggestionsDtos = new ArrayList<>();
	List<String> allAttributesType = getAllAttributeTypes();
	List<Hit> hits = getHitsAll().getData();
	for (String attType : allAttributesType) {
	    List<Suggestion> suggestions = m_algorithm.getSuggestions(hits, attType);
	    SuggestionsDto attSuggestions = new SuggestionsDto(suggestions, attType);
	    suggestionsDtos.add(attSuggestions);
	}
	return Utility.subList(suggestionsDtos, 0, amount);
    }

    /**
     * Set the filter of this job to a new filter
     * 
     * @param filter
     *            new filter of this job
     */
    public void setFilter(Filter filter) {
	checkJobState();
	m_filter = filter;
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
	    Rule demoRule = m_rules.get(0);
	    for (Attribute att : demoRule) {
		m_allAttributeTypes.add(att.getType());
	    }
	}
	return m_allAttributeTypes;
    }

    /**
     * Takes the current filter and adds it as a new rule
     */
    public void addCurrentFilterToRules() {
	checkJobState();

	int lastIndex = m_rules.get(m_rules.size() - 1).getId();
	Rule newRule = new Rule(lastIndex + 1, m_filter);
	m_rules.add(newRule);
	// TODO update DAO
    }

    private void checkJobState() {
	if (m_id == NO_CURRENT_JOB) {
	    throw new NoCurrentJobException("Job wasn't set yet");
	}
    }

}
