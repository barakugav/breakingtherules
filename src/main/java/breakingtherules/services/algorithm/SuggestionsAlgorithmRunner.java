package breakingtherules.services.algorithm;

import java.util.List;

interface SuggestionsAlgorithmRunner extends Runnable {

    public List<Suggestion> getResults();
    
}
