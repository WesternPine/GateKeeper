package dev.westernpine.gatekeeper.configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum ConfigValue {
	
	DISCORD_TOKEN(),
	COMMAND_PREFIX(),
	SQL_IP(),
	SQL_PORT(),
	SQL_DATABASE(),
	SQL_USERNAME(),
	SQL_PASSWORD(),
	;
	
	public static Collection<String> getStrings(){
		Set<String> values = new HashSet<>();
		Arrays.asList(ConfigValue.values()).forEach(cv -> values.add(cv.toString()));
		return values;
	}

}
