package dev.westernpine.gatekeeper.backend;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.configuration.ConfigValue;
import dev.westernpine.gatekeeper.configuration.GateKeeperConfig;
import proj.api.marble.builders.sql.DatabaseType;
import proj.api.marble.builders.sql.SQL;
import proj.api.marble.builders.sql.SQLBuilder;

public class Backend {
	
	private static SQL sql;
	
	public static void initialize(boolean debugMode) {
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
	
	public static boolean canConnect() {
		boolean canConnect = false;
		sql.getConnection().open();
		canConnect = sql.getConnection().isOpen();
		sql.getConnection().close();
		return canConnect;
	}
	
	public static GuildBackend get(String guild) {
		return new GuildBackend(sql, guild);
	}
	
	
	
}