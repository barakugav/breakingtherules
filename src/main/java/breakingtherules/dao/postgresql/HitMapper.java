package breakingtherules.dao.postgresql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.springframework.jdbc.core.RowMapper;

import breakingtherules.firewall.Attribute;
import breakingtherules.firewall.Hit;

public class HitMapper implements RowMapper<Hit> {
    public Hit mapRow(ResultSet rs, int rowNum) throws SQLException {
	Hit hit = new Hit(rs.getInt(PostgresConfig.ID), new ArrayList<Attribute>());
	return hit;
    }
}
