package breakingtherules.application;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import breakingtherules.dao.HitsDao;
import breakingtherules.dao.es.HitsElasticDao;
import breakingtherules.dao.postgresql.HitsPostgresDao;
import breakingtherules.dao.xml.HitsXmlDao;
import breakingtherules.services.algorithm.InformationAlgorithm;
import breakingtherules.services.algorithm.SuggestionsAlgorithm;

@Configuration
@ComponentScan({ "breakingtherules" })
public class Config {

    @Bean
    public DataSource dataSource() {
	DriverManagerDataSource ds = new DriverManagerDataSource();
	ds.setDriverClassName("org.postgresql.Driver");
	ds.setUrl("jdbc:posgresql://localhost:3306/TEST");
	ds.setUsername("root");
	ds.setPassword("admin");
	return ds;
    }

    // @Bean
    public HitsDao hitsPostgresDao() {
	HitsPostgresDao dao = new HitsPostgresDao();
	dao.setDataSource(dataSource());
	return dao;
    }

    @Bean
    public HitsDao hitsXmlDao() {
	return new HitsXmlDao();
    }

    // @Bean(destroyMethod = "cleanup")
    public HitsDao hitsElasticDao() {
	return new HitsElasticDao();
    }

    @Bean
    public SuggestionsAlgorithm infoAlgorithm() {
	return new InformationAlgorithm();
    }

}
