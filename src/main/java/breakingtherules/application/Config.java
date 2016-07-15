package breakingtherules.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.RulesDao;
import breakingtherules.dao.csv.CSVHitsDao;
import breakingtherules.dao.elastic.ElasticHitsDao;
import breakingtherules.dao.xml.XMLHitsDao;
import breakingtherules.dao.xml.XMLRulesDao;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.SimpleAlgorithm;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;
import breakingtherules.session.JobManager;

/**
 * Configuring Of the Spring Application. Specifically, what algorithm to use to
 * get suggestions for rules, what DAO to use to get hits and rules, and how to
 * handle file uploads
 *
 * @author Barak Ugav
 * @author Yishai Gronich
 *
 */
@SuppressWarnings("javadoc")
@Configuration
@ComponentScan({ "breakingtherules" })
public class Config {

    /* ------- Algorithm ------- */

    @Bean
    public SuggestionsAlgorithm algorithm() {
	return infoAlgorithm();
    }

    @Bean
    public SuggestionsAlgorithm simpleAlgorithm() {
	return new SimpleAlgorithm(hitsDao());
    }

    @Bean
    public SuggestionsAlgorithm infoAlgorithm() {
	return new InformationAlgorithm(hitsDao());
    }

    /* ------- DAO ------- */

    @Bean
    public HitsDao hitsDao() {
	return csvHitsDao();
    }

    @Bean
    public RulesDao rulesDao() {
	return xmlRulesDao();
    }

    @Bean
    public CSVHitsDao csvHitsDao() {
	return new CSVHitsDao();
    }

    @Bean
    public XMLHitsDao hitsXmlDao() {
	return new XMLHitsDao();
    }

    // @Bean(destroyMethod = "cleanup")
    public ElasticHitsDao esHitsDao() {
	return new ElasticHitsDao();
    }

    @Bean
    public XMLRulesDao xmlRulesDao() {
	return new XMLRulesDao();
    }

    /* ------- Other ------- */

    @Bean
    public MultipartResolver fileResolver() {
	return new StandardServletMultipartResolver();
    }

    @Bean
    @Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public JobManager jobManager() {
	return new JobManager(hitsDao(), rulesDao(), algorithm());
    }

}
