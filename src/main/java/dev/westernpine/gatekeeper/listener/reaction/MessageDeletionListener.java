package dev.westernpine.gatekeeper.listener.reaction;

import dev.westernpine.gatekeeper.management.reactions.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.GuildReactionMap;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageDeletionListener extends ListenerAdapter {
	
	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		String guild = event.getGuild().getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		
		GuildReactionMap map = ReactionRoleManager.getGuildReactionMapFromBackend(guild);
		map.remove(channel, message);
		ReactionRoleManager.updateGuildReactionMapToBackend(map);
		
	}
	
}
