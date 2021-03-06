package dev.westernpine.gatekeeper.management;

import java.util.HashMap;

import dev.westernpine.gatekeeper.GateKeeper;

public class GuildManager {

	private static HashMap<String, Manager> guildManagers = new HashMap<>();

	public static void initialize() {
		GateKeeper.getInstance().getManager().getGuilds().forEach(guild -> {
			// This acts as a catch-all for startup errors.
			try {
				guildManagers.put(guild.getId(), new Manager(guild.getId()));
			} catch (Exception e) {
			}
		});
	}

	public static void shutdown() {
		guildManagers.values().forEach(manager -> manager.shutdown());
	}

	public static Manager get(String guild) {
		Manager manager = guildManagers.get(guild);
		if (manager == null) {
			manager = new Manager(guild);
			guildManagers.put(guild, manager);
		}
		return manager;
	}

}
