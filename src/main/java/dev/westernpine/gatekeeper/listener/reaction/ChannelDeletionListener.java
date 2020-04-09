package dev.westernpine.gatekeeper.listener.reaction;

import dev.westernpine.gatekeeper.management.GuildManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelDeletionListener extends ListenerAdapter {
	
	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent event) {
		String guild = event.getGuild().getId();
		GuildManager.get(guild).getReactionRoleManager().remove(event.getChannel().getId(), TextChannel.class);
	}

}
