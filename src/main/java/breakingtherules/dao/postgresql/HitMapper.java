package breakingtherules.dao.postgresql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.jdbc.core.RowMapper;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;

public class HitMapper implements RowMapper<Hit> {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet,
     * int)
     */
    public Hit mapRow(ResultSet rs, int rowNum) throws SQLException {
	return new Hit(rs.getInt(PostgresConfig.ID), new ArrayList<Attribute>());
    }

}
