package dev.westernpine.gatekeeper.backend;

import java.util.Collection;

import dev.westernpine.common.sql.SQL;
import dev.westernpine.common.sql.SQL.DatabaseType;
import dev.westernpine.common.sql.SQL.SQLBuilder;
import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.configuration.ConfigValue;
import dev.westernpine.gatekeeper.configuration.GateKeeperConfig;
import net.dv8tion.jda.api.entities.Guild;

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

	public static void initializeGuilds(Collection<Guild> guilds) {
		guilds.forEach(guild -> get(guild.getId()).createTable());
		A: for (String table : sql.getTables()) {
			for (Guild guild : guilds) {
				if (guild.getId().equals(table)) {
					continue A;
				}
			}
			get(table).dropTable();
		}
	}

	public static void primeForShutdown() {
		sql.getConnection().open();
	}

	public static void finishShutdown() {
		sql.getConnection().close();
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