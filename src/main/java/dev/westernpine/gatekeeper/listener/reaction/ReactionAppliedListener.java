package dev.westernpine.gatekeeper.listener.reaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.management.GuildManager;
import dev.westernpine.gatekeeper.management.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.Action;
import dev.westernpine.gatekeeper.object.Messages;
import dev.westernpine.gatekeeper.object.NewReactionTask;
import dev.westernpine.gatekeeper.util.Messenger;
import dev.westernpine.gatekeeper.util.ReactionUtil;
import dev.westernpine.gatekeeper.util.RoleUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionAppliedListener extends ListenerAdapter {

	// listen for active creation of reaction roles + valid reaction roles

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		Guild g = event.getGuild();
		String guild = g.getId();
		Member member = event.getMember();
		String userApplied = member.getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		String reaction = ReactionUtil.getId(event.getReactionEmote());

		ReactionRoleManager rrManager = GuildManager.get(guild).getReactionRoleManager();
		
		if (!g.getSelfMember().getId().equals(userApplied)) {
			for(Action action : Action.values()) {
				String roleString = rrManager.getRoleString(channel, message, reaction, action);
				RoleUtils.applyRoleString(roleString, action, member);
			}
			// dont return in case it is a setup message reaction
		}

		Set<String> activeRoleIds = new HashSet<>();
		Set<Member> reactors = new HashSet<>();
		NewReactionTask task = rrManager.reactionTask;
		
		if (task != null && task.getCurrentChannel().equals(channel) && task.getCurrentMessage().equals(message)
				&& task.getCreator().equals(userApplied)) {
			boolean added = false;
			String reason = "Unknown reason.";
			try {
				reason = "One or more roles no longer exists.";
				Set<String> roleIds = task.getTaggedRoles();
				roleIds.forEach(roleId -> g.getRoleById(roleId).getIdLong());
				
				reason = "Unable to find message.";
				Message msg = g.getTextChannelById(task.getTaggedChannel()).retrieveMessageById(task.getMessageId()).complete();
				msg.getIdLong();
				
				reason = "A reaction role already exists with one of the roles.";
				List<User> userReactors = event.getReactionEmote().isEmote()
						? msg.retrieveReactionUsers(event.getReactionEmote().getEmote()).complete()
						: msg.retrieveReactionUsers(event.getReactionEmote().getEmoji()).complete();
				if (userReactors.contains(g.getSelfMember().getUser())) {
					for(Action action : Action.values()) {
						String roleString = rrManager.getRoleString(task.getTaggedChannel(), task.getMessageId(), ReactionUtil.getId(event.getReactionEmote()), action);
						activeRoleIds = RoleUtils.toRoleSet(roleString);
						for(String roleId : activeRoleIds) {
							if(roleIds.contains(roleId)) {
								Role role = g.getRoleById(roleId);
								reason = reason + "\nCommon Role Name: " + role.getName();
								reason = reason + "\nCommon Role ID: " + roleId;
								throw new Exception();
							}
						}
					}
				}
					

				userReactors.forEach(user -> reactors.add(g.getMember(user)));

				reason = "Unable to apply reaction to the message.";
				if (event.getReactionEmote().isEmoji()) {
					msg.addReaction(event.getReactionEmote().getEmoji()).complete();
				} else {
					if (g.getEmoteById(reaction) == null) {
						reason = "That custom emote is not in this server.";
						throw new Exception();
					} else {
						msg.addReaction(event.getReactionEmote().getEmote()).complete();
					}
				}
				
				activeRoleIds.addAll(roleIds);
				rrManager.set(task.getTaggedChannel(), msg.getId(), reaction, task.getAction(), RoleUtils.toRoleString(activeRoleIds));
				added = true;
			} catch (Exception e) {
			} finally {
				task.end(false);
			}

			TextChannel ch = GateKeeper.getInstance().getManager().getGuildById(guild).getTextChannelById(channel);
			Messenger.sendEmbed(ch, (added ? Messages.reactionRoleApplied().build()
					: Messages.failedToApplyReactionRole(reason).build()));
			if (added) {
				reactors.remove(g.getSelfMember());
				RoleUtils.applyRoleString(RoleUtils.toRoleString(activeRoleIds), task.getAction(), reactors.toArray(new Member[reactors.size()]));
			}
		}
	}

}
