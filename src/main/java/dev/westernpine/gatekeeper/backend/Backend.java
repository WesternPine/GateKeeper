package dev.westernpine.gatekeeper.backend;

import java.util.Optional;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.configuration.ConfigValue;
import dev.westernpine.gatekeeper.configuration.GateKeeperConfig;
import proj.api.marble.builders.sql.DatabaseType;
import proj.api.marble.builders.sql.SQL;
import proj.api.marble.builders.sql.SQLBuilder;

public class Backend {
	
	private SQL sql;
	
	public Backend(boolean debugMode) {
		GateKeeperConfig config = GateKeeper.getInstance().getConfig();
		
		SQLBuilder builder = SQL.getBuilder();
		builder.setUseSSL(false);
		builder.setUseSSLSuffix(true);
		builder.setDatabaseType(DatabaseType.MYSQL);
		builder.setIp(config.getValue(ConfigValue.SQL_IP));
		builder.setPort(config.getValue(ConfigValue.SQL_PORT));
		builder.setDatabase(config.getValue(ConfigValue.SQL_DATABASE));
		builder.setUsername(config.getValue(ConfigValue.SQL_USERNAME));
		builder.setPassword(config.getValue(ConfigValue.SQL_PASSWORD));
		sql = builder.build();
		sql.setDebugging(debugMode);
	}
	
	public GuildBackend get(String guild) {
		return new GuildBackend(sql, guild);
	}
	
}

class GuildBackend {
	
	private SQL sql;
	private String guild;
	
	GuildBackend(SQL sql, String guild) {
		this.sql = sql;
		this.guild = guild;
	}
	
	public boolean exists() {
		return sql.tableExists(guild);
	}
	
	public void createTable() {
        sql.update("CREATE TABLE IF NOT EXISTS `" + guild + "` (`type` varchar(255) NOT NULL, `id` varchar(255) NOT NULL, PRIMARY KEY (type)) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
	}
	
	public void destroyTable() {
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