package dao;

import java.util.List;

import dao.Attribute.AttType;

public interface ServerAPI {

    public void init(String job);

    public void addRule(Rule rule);

    public void removeRule(Rule rule);

    public List<Hit> getHits(int startIndex, int endIndex);

    public List<Suggestion> getSuggestions(AttType attType, int startIndex, int endIndex);

    public void setFilter(Filter filter);
    
    public void clearFilter();

}
