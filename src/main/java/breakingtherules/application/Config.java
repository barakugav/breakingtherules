package breakingtherules.application;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.csv.HitsCSVDao;
import breakingtherules.dao.elastic.HitsElasticDao;
import breakingtherules.dao.xml.HitsXmlDao;
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

    @Bean
    public HitsDao hitsXmlDao() {
	return new HitsXmlDao();
    }

    // @Bean
    public HitsDao hitsCSVDao() {
	return new HitsCSVDao();
    }

    // @Bean(destroyMethod = "cleanup")
    public HitsDao hitsElasticDao() {
	return new HitsElasticDao();
    }

    @Bean
    public SuggestionsAlgorithm infoAlgorithm() {
	return new InformationAlgorithm();
    }

    // @Bean
    public SuggestionsAlgorithm simpleAlgorithm() {
	return new SimpleAlgorithm();
    }

}
