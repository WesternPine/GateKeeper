package dev.westernpine.gatekeeper.backend;

import java.util.HashMap;
import java.util.Optional;

import proj.api.marble.builders.sql.SQL;

public class GuildBackend {
	
	private SQL sql;
	private String guild;
	
	protected GuildBackend(SQL sql, String guild) {
		this.sql = sql;
		this.guild = guild;
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getEntries() {
		Optional<Object> returned = sql.query(rs -> {
			HashMap<String, String> entries = new HashMap<>();
			try {
				while(rs.next()) {
					entries.put(rs.getString("type"), rs.getString("id"));
				}
			} catch (Exception e) {}
			return entries;
		}, "SELECT * FROM `" + guild + "`;");
		return (HashMap<String, String>) returned.get(); //will always return a hash map
	}
	
	public boolean exists() {
		return sql.getTables().contains(guild);
	}
	
	public void createTable() {
        sql.update("CREATE TABLE IF NOT EXISTS `" + guild + "` (`type` varchar(255) NOT NULL, `id` text NOT NULL, PRIMARY KEY (type)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
	}
	
	public void dropTable() {
		sql.update("DROP TABLE " + guild + ";");
	}
	
	public void addEntry(String key, String value) {
		sql.update("INSERT INTO `" + guild + "` VALUES(?,?);", key, value);
	}
	
	public Optional<String> getEntryValue(String key) {
		Optional<Object> returned = sql.query(rs -> {
			try {
				if(rs.next())
					return rs.getString("id");
			} catch (Exception e) {
				if(sql.isDebugging())
					e.printStackTrace();
			}
			return null;
		}, "SELECT * FROM `" + guild + "` WHERE type=?;", key);
		return returned.isPresent() ? Optional.of((String) returned.get()) : Optional.empty();
	}
	
	public void dropEntry(String key) {
		sql.update("DELETE FROM `" + guild + "` WHERE type=?;", key);
	}
	
}
