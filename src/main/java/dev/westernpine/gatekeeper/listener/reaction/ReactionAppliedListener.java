package dev.westernpine.gatekeeper.listener.reaction;

import java.util.List;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.management.GuildManager;
import dev.westernpine.gatekeeper.management.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.Messages;
import dev.westernpine.gatekeeper.object.NewReactionTask;
import dev.westernpine.gatekeeper.util.Messenger;
import dev.westernpine.gatekeeper.util.ReactionUtil;
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

		// test to see if is a set up reaction
		NewReactionTask task = rrManager.reactionTask;
		if (task != null && task.getCurrentChannel().equals(channel) && task.getCurrentMessage().equals(message)
				&& task.getCreator().equals(userApplied)) {
			boolean added = false;
			String reason = "Unknown.";
			try {
				reason = "Role no longer exists.";
				Role role = g.getRoleById(task.getTaggedRole());
				role.getIdLong();

				reason = "Unable to find message.";
				Message msg = g.getTextChannelById(task.getTaggedChannel()).retrieveMessageById(task.getMessageId())
						.complete();
				msg.getIdLong();

				reason = "My reaction already exists on the message.";
				List<User> reactors = event.getReactionEmote().isEmote()
						? msg.retrieveReactionUsers(event.getReactionEmote().getEmote()).complete()
						: msg.retrieveReactionUsers(event.getReactionEmote().getEmoji()).complete();
				if (reactors.contains(g.getSelfMember().getUser()))
					throw new Exception();

				if (event.getReactionEmote().isEmote()) {

				} else {

				}

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

				rrManager.set(task.getTaggedChannel(), msg.getId(), reaction, role.getId());
				task.end(false);

				added = true;
			} catch (Exception e) {}

			TextChannel ch = GateKeeper.getInstance().getManager().getGuildById(guild).getTextChannelById(channel);
			Messenger.delete(ch.retrieveMessageById(message).complete());
			Messenger.sendEmbed(ch, (added ? Messages.reactionRoleApplied().build()
					: Messages.failedToApplyReactionRole(reason).build()));

		} else {
			if (!g.getSelfMember().getId().equals(userApplied)) {
				String roleId = rrManager.getRole(channel, message, reaction);
				Role role = g.getRoleById(roleId);
				g.addRoleToMember(member, role).queue();
			}
		}
	}

}
