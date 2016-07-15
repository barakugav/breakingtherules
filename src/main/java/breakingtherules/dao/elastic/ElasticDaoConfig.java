package breakingtherules.dao.elastic;

/**
 * Configuration information for the ElasticSearch DAO.
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
class ElasticDaoConfig {

    /**
     * The name of the ElasticSearch cluster to connect to
     */
    static final String CLUSTER_NAME = "breakingtherules";

    /**
     * The name of the ElasticSearch index that all the information is saved in
     * and read from
     */
    static final String INDEX_NAME = "btr";

    /**
     * The name of the type Hit in the database
     */
    static final String TYPE_HIT = "hit";

    /**
     * The name of the field that holds the hit's attributes
     */
    static final String FIELD_ATTRIBUTES = "attributes";

    /**
     * The name of the field that holds the hit's job name
     */
    static final String FIELD_JOB_NAME = "jobName";

    /**
     * The name of the field that each Hit Attribute has, that says the ID of
     * the attribute type
     */
    static final String FIELD_ATTR_TYPEID = "typeId";

    /**
     * The name of the field that each Hit Attribute has, that says the value of
     * the attribute
     */
    static final String FIELD_ATTR_VALUE = "value";

    /**
     * Jobs that have more hits that this threshold cannot be deleted by code,
     * and have to be deleted manually
     */
    static final int DELETION_THRESHOLD = 5000;

    /**
     * This is the maximum time that each scroll request is given before a
     * timeout happens
     */
    static final int TIME_PER_SCROLL = 60000; // in milliseconds

    /**
     * The number of hits that are requested in every scroll request. When this
     * parameter is high, more hits are requested at the same time
     */
    static final int HITS_PER_SCROLL = 5000;

    /**
     * Suppresses default constructor, ensuring non-instantiability.
     */
    private ElasticDaoConfig() {
    }

}
