package breakingtherules.serverapi;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import breakingtherules.algorithms.Suggestion;
import breakingtherules.algorithms.SuggestionsAlgorithm;
import breakingtherules.firewall.Attribute.AttType;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;
import breakingtherules.session.Job;

public class ServerImp implements ServerAPI {

    private List<Rule> currentRules;

    private Filter currentFilter;

    private List<Hit> hits;

    @Autowired
    private Job job;

    @Autowired
    private SuggestionsAlgorithm algorithm;

    /*---------------------------------------------*/

    public ServerImp() {

    }

    public void init(String job) {
	currentRules = new ArrayList<Rule>();
	currentFilter = new Filter();
	// hits = job.getHits();
    }

    public void addRule(Rule rule) {
	currentRules.add(rule);
    }

    public void removeRule(Rule rule) {
	currentRules.remove(rule);
    }

    public List<Hit> getHits(int startIndex, int endIndex) {
	List<Hit> matchedHits = new ArrayList<Hit>();
	for (Hit hit : hits) {
	    if (currentFilter.isMatch(hit) && !isCaughtByRules(hit))
		matchedHits.add(hit);
	}
	return matchedHits;
    }

    public List<Rule> getRules() {
	return job.getRules();
    }

    private boolean isCaughtByRules(Hit hit) {
	for (Rule rule : currentRules)
	    if (rule.isMatch(hit))
		return true;
	return false;
    }

    public List<Suggestion> getSuggestions(AttType attType, int startIndex, int endIndex) {
	return algorithm.getSuggestions(job, attType);
    }

    public void setFilter(Filter filter) {
	currentFilter = filter;
    }

    public void clearFilter() {
	currentFilter = new Filter();
    }

}
