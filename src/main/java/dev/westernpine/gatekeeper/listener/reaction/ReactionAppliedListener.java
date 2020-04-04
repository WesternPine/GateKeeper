package dev.westernpine.gatekeeper.listener.reaction;

import java.awt.Color;
import java.util.List;

import javax.annotation.Nonnull;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.management.reactions.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.GuildReactionMap;
import dev.westernpine.gatekeeper.object.NewReactionTask;
import dev.westernpine.gatekeeper.util.Messenger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import proj.api.marble.lib.emoji.Emoji;

public class ReactionAppliedListener extends ListenerAdapter {
	
	//listen for active creation of reaction roles + valid reaction roles
	
	@Override
	public void onGuildMessageReactionAdd(@Nonnull GuildMessageReactionAddEvent event) {
		Guild g = event.getGuild();
		String guild = g.getId();
		Member member = event.getMember();
		String userApplied = member.getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		String reaction = event.getReactionEmote().isEmoji() ? event.getReactionEmote().getEmoji() : event.getReactionEmote().getId();
		
		
		//test to see if is a set up reaction
		NewReactionTask task = ReactionRoleManager.getActiveReactionTask(guild);
		if(task != null && task.getCurrentChannel().equals(channel) && task.getCurrentMessage().equals(message) && task.getCreator().equals(userApplied)) {//.equals
			boolean added = false;
			String reason = "Unknown.";
			try {
				reason = "Role no longer exists.";
				Role role = g.getRoleById(task.getTaggedRole());
				role.getIdLong();
				
				reason = "Unable to find message.";
				Message msg = g.getTextChannelById(task.getTaggedChannel()).getHistory().getMessageById(task.getMessageId());
				msg.getIdLong();
				
				reason = "My reaction already exists on the message.";
				List<User> reactors = event.getReactionEmote().isEmoji()
						? msg.retrieveReactionUsers(event.getReactionEmote().getEmoji()).complete()
						: msg.retrieveReactionUsers(event.getReactionEmote().getEmote()).complete();
				if(reactors.contains(g.getSelfMember().getUser()))
					throw new Exception();
				
				reason = "Unable to apply reaction to the message.";
				if(event.getReactionEmote().isEmoji()) {
					msg.addReaction(event.getReactionEmote().getEmote()).complete();
				} else {
					msg.addReaction(event.getReactionEmote().getEmoji()).complete();
				}
				
				GuildReactionMap map = ReactionRoleManager.getGuildReactionMapFromBackend(guild);
				map.modify(task.getTaggedChannel(), task.getMessageId(), reaction, task.getTaggedRole());
				ReactionRoleManager.updateGuildReactionMapToBackend(map);
				
				added = true;
			} catch (Exception e) {}
			
			TextChannel ch = GateKeeper.getInstance().getManager().getGuildById(guild).getTextChannelById(channel);
			Messenger.delete(ch.getHistory().getMessageById(message));
			
			EmbedBuilder failed = Messenger.getEmbedFrame();
			failed.setDescription(Emoji.CrossMark + " **Failed to apply reaction role to message!**\n`" + reason + "`");
			failed.setColor(Color.RED);
			
			EmbedBuilder success = Messenger.getEmbedFrame();
			success.setDescription(Emoji.GreenCheck + " **Reaction role applied!**");
			success.setColor(Color.GREEN);
			
			ch.sendMessage((added ? success : failed).build()).queue();
			
		} else {
			if(!g.getSelfMember().getId().equals(userApplied)) {
				try {
					String roleId = ReactionRoleManager.getGuildReactionMapFromBackend(guild).getMap().get(channel).get(message).get(reaction);
					Role role = g.getRoleById(roleId);
					g.addRoleToMember(member, role);
				} catch (Exception e) {} 
				//Dont worry about no value existing in map, or if role doesnt exist, or if you cant modify the users roles
				//Thats why this trycatch is here ^
			}
		}
	}
	
}
