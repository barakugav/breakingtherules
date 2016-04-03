package breakingtherules.dao.postgresql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import breakingtherules.dao.HitsDao;
import breakingtherules.dto.ListDto;
import breakingtherules.firewall.Filter;
import breakingtherules.firewall.Hit;
import breakingtherules.firewall.Rule;

public class HitsPostgresDao implements HitsDao {

    private DataSource m_dataSource;
    private JdbcTemplate m_jdbcTemplateObject;

    public void setDataSource(DataSource dataSource) {
	this.m_dataSource = dataSource;
	this.m_jdbcTemplateObject = new JdbcTemplate(dataSource);
	System.out.println("Success");
    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter) throws IOException {
	return new ListDto<Hit>(new ArrayList<Hit>(), 0, 10, 235);
    }

    @Override
    public ListDto<Hit> getHits(int jobId, List<Rule> rules, Filter filter, int startIndex, int endIndex)
	    throws IOException {
	return new ListDto<Hit>(new ArrayList<Hit>(), 0, 10, 235);
    }

}