package breakingtherules.dao.elastic;

class ElasticDaoConfig {

    static final String CLUSTER_NAME = "breakingtherules";
    static final String INDEX_NAME = "btr";
    static final String TYPE_HIT = "hit";

    static final String FIELD_ATTRIBUTES = "attributes";
    static final String FIELD_JOB_ID = "jobId";
    static final String FIELD_ATTR_TYPEID = "typeId";
    static final String FIELD_ATTR_VALUE = "value";

    static final int DELETION_THRESHOLD = 5000;

    static final int TIME_PER_SCROLL = 60000; // in milliseconds
    static final int HITS_PER_SCROLL = 5000;

}
