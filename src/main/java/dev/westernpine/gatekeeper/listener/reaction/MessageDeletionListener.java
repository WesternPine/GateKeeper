package dev.westernpine.gatekeeper.listener.reaction;

import dev.westernpine.gatekeeper.management.GuildManager;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageDeletionListener extends ListenerAdapter {
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		String guild = event.getGuild().getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		GuildManager.get(guild).getReactionRoleManager().removeMessage(channel, message);
		
	}
	
}
