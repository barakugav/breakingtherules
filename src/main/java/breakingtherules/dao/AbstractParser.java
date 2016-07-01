package breakingtherules.dao;

import breakingtherules.firewall.Destination;
import breakingtherules.firewall.Service;
import breakingtherules.firewall.Source;

/**
 * Abstract parser contains caches of possible attributes that can be used by
 * subclasses.
 * <p>
 * Caches can be set to reduce object creations and memory use.
 * <p>
 * 
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
public class AbstractParser {

    /**
     * Cache of source objects. Can be used by the parser to reduce objects
     * creation and memory use.
     */
    protected Source.Cache sourceCache;

    /**
     * Cache of destination objects. Can be used by the parser to reduce objects
     * creation and memory use.
     */
    protected Destination.Cache destinationCache;

    /**
     * Cache of service objects. Can be used by the parser to reduce objects
     * creation and memory use.
     */
    protected Service.Cache serviceCache;

    /**
     * Construct new parser.
     */
    protected AbstractParser() {
    }

    /**
     * Get the current used source objects cache by this parser.
     * 
     * @return the parser current source objects cache.
     */
    public Source.Cache getSourceCache() {
	return sourceCache;
    }

    /**
     * Get the current used destination objects cache by this parser.
     * 
     * @return the parser current destination objects cache.
     */
    public Destination.Cache getDestinationCache() {
	return destinationCache;
    }

    /**
     * Get the current used service objects cache by this parser.
     * 
     * @return the parser current service objects cache.
     */
    public Service.Cache getServiceCache() {
	return serviceCache;
    }

    /**
     * Set the parser's source object cache.
     * <p>
     * Can be used by the parser to reduce objects creation and memory use.
     * 
     * @param cache
     *            the source objects cache. (can be null).
     */
    public void setSourceCache(final Source.Cache cache) {
	sourceCache = cache;
    }

    /**
     * Set the parser's destination object cache.
     * <p>
     * Can be used by the parser to reduce objects creation and memory use.
     * 
     * @param cache
     *            the destination objects cache. (can be null).
     */
    public void setDestinationCache(final Destination.Cache cache) {
	destinationCache = cache;
    }

    /**
     * Set the parser's service object cache.
     * <p>
     * Can be used by the parser to reduce objects creation and memory use.
     * 
     * @param cache
     *            the service objects cache. (can be null).
     */
    public void setServiceCache(final Service.Cache cache) {
	serviceCache = cache;
    }

}
