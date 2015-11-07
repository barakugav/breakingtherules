package dao;

import java.util.ArrayList;
import java.util.List;

import dao.Attribute.AttType;

public class ServerImp implements ServerAPI {

    private List<Rule> currentRules;

    private Filter currentFilter;

    private List<Hit> hits;

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

    private boolean isCaughtByRules(Hit hit) {
	for (Rule rule : currentRules)
	    if (rule.isMatch(hit))
		return true;
	return false;
    }


    public List<Suggestion> getSuggestions(AttType attType, int startIndex, int endIndex) {
	return new SimpleAlgorithm().getSuggestions(hits, currentRules, currentFilter, attType, startIndex, endIndex);
    }


    public void setFilter(Filter filter) {
	currentFilter = filter;
    }


    public void clearFilter() {
	currentFilter = new Filter();
    }

}
