package breakingtherules.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.csv.CSVHitsDao;
import breakingtherules.dao.elastic.ElasticHitsDao;
import breakingtherules.dao.xml.XMLHitsDao;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.SimpleAlgorithm;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;

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

    // -------------- Algorithm ---------------

    @Bean
    public SuggestionsAlgorithm infoAlgorithm() {
	return new InformationAlgorithm();
    }

    // @Bean
    public SuggestionsAlgorithm simpleAlgorithm() {
	return new SimpleAlgorithm();
    }

    // ------------ Upload Settings ---------------

    @Bean
    public MultipartResolver fileResolver() {
	return new StandardServletMultipartResolver();
    }

    // ---------- Data Access Objects -------------

    // Choose one of the following three options
    @Bean
    public HitsDao hitsDao() {
	return csvHitsDao();
    }

    // @Bean(destroyMethod = "cleanup")
    public ElasticHitsDao esHitsDao() {
	return new ElasticHitsDao();
    }

    @Bean
    public CSVHitsDao csvHitsDao() {
	return new CSVHitsDao();
    }

    @Bean
    public XMLHitsDao hitsXmlDao() {
	return new XMLHitsDao();
    }

}
