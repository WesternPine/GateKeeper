package dev.westernpine.gatekeeper.listener.reaction;

import dev.westernpine.gatekeeper.backend.Backend;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GuildListener extends ListenerAdapter {

	// for being added and removed from guilds

	@Override
	public void onGuildJoin(GuildJoinEvent event) {
		Backend.get(event.getGuild().getId()).createTable();
	}
	
	@Override
	public void onGuildLeave(GuildLeaveEvent event) {
		Backend.get(event.getGuild().getId()).dropTable();
	}

}
