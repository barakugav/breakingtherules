package breakingtherules.dto;

import java.util.List;

import breakingtherules.firewall.Hit;

public class HitsDto {
    

    /**
     * The hits to be passed on
     */
    public List<Hit> hits;
    
    
    /**
     * How many there are of this kind.
     * NOT necessarily the same as hits.length !!
     * Note: endIndex - startIndex + 1 == hits.length;
     */
    public int total;
    
    /**
     * The 0-index of the first hit, out of all the hits of the requested type.
     */
    public int startIndex;
    
    /**
     * The 0-index + 1 of the last hit, out of all the hits of the requested type.
     */
    public int endIndex;
    
    
    public HitsDto(List<Hit> hits, int startIndex, int endIndex, int total) {
	this.total = total;
	this.startIndex = startIndex;
	this.endIndex = endIndex;
	this.hits = hits;
    }
    
    
}
