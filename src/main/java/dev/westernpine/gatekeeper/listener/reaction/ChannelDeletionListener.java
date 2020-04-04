package dev.westernpine.gatekeeper.listener.reaction;

import dev.westernpine.gatekeeper.management.reactions.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.GuildReactionMap;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ChannelDeletionListener extends ListenerAdapter {
	
	@Override
	public void onTextChannelDelete(TextChannelDeleteEvent event) {
		String guild = event.getGuild().getId();
		
		GuildReactionMap map = ReactionRoleManager.getGuildReactionMapFromBackend(guild);
		map.remove(event.getChannel().getId());
		ReactionRoleManager.updateGuildReactionMapToBackend(map);
	}

}
