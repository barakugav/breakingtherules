package breakingtherules.application;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.csv.CSVHitsDao;
import breakingtherules.dao.elastic.ElasticHitsDao;
import breakingtherules.dao.xml.XMLHitsDao;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.SimpleAlgorithm;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;

@Configuration
@ComponentScan({ "breakingtherules" })
public class Config {

    @Bean
    public DataSource jdbcDataSource() {
	DriverManagerDataSource ds = new DriverManagerDataSource();
	ds.setDriverClassName("org.postgresql.Driver");
	ds.setUrl("jdbc:posgresql://localhost:3306/TEST");
	ds.setUsername("root");
	ds.setPassword("admin");
	return ds;
    }

    // @Bean
    public HitsDao hitsXmlDao() {
	return new XMLHitsDao();
    }

    @Bean
    public HitsDao hitsCSVDao() {
	return new CSVHitsDao();
    }

    // @Bean(destroyMethod = "cleanup")
    public HitsDao hitsElasticDao() {
	return new ElasticHitsDao();
    }

    @Bean
    public SuggestionsAlgorithm infoAlgorithm() {
	return new InformationAlgorithm();
    }

    // @Bean
    public SuggestionsAlgorithm simpleAlgorithm() {
	return new SimpleAlgorithm();
    }

    @Bean
    public MultipartResolver uploadSettings() {
	MultipartResolver resolver = new StandardServletMultipartResolver();
	return resolver;
    }

}
