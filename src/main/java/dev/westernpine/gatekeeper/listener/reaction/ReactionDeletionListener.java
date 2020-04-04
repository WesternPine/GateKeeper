package dev.westernpine.gatekeeper.listener.reaction;

import dev.westernpine.gatekeeper.management.reactions.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.GuildReactionMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionDeletionListener extends ListenerAdapter {
	
	//Single emote removed
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		Guild g = event.getGuild();
		String guild = g.getId();
		Member member = event.getMember();
		String userRemoved = member.getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		String reaction = event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : event.getReactionEmote().getId();
		
		if(!g.getSelfMember().getId().equals(userRemoved)) {
			try {
				String roleId = ReactionRoleManager.getGuildReactionMapFromBackend(guild).getMap().get(channel).get(message).get(reaction);
				Role role = g.getRoleById(roleId);
				g.removeRoleFromMember(member, role);
			} catch (Exception e) {} 
			//Dont worry about no value existing in map, or if role doesnt exist, or if you cant modify the users roles
			//Thats why this trycatch is here ^
		} else {
			GuildReactionMap map = ReactionRoleManager.getGuildReactionMapFromBackend(guild);
			map.remove(channel, message, reaction);
			ReactionRoleManager.updateGuildReactionMapToBackend(map);
		}
		
	}
	
	//All of EVERY emote was removed
	@Override
    public void onGuildMessageReactionRemoveAll(GuildMessageReactionRemoveAllEvent event) {
		String guild = event.getGuild().getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		
		GuildReactionMap map = ReactionRoleManager.getGuildReactionMapFromBackend(guild);
		map.remove(channel, message);
		ReactionRoleManager.updateGuildReactionMapToBackend(map);
	}
	
	//All of a SINGLE emote was removed
	@Override
    public void onGuildMessageReactionRemoveEmote(GuildMessageReactionRemoveEmoteEvent event) {
		String guild = event.getGuild().getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		String reaction = event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : event.getReactionEmote().getId();
		
		GuildReactionMap map = ReactionRoleManager.getGuildReactionMapFromBackend(guild);
		map.remove(channel, message, reaction);
		ReactionRoleManager.updateGuildReactionMapToBackend(map);
	}

}
