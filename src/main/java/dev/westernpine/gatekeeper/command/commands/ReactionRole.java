package dev.westernpine.gatekeeper.command.commands;

import java.util.List;

import dev.westernpine.gatekeeper.command.Command;
import dev.westernpine.gatekeeper.management.GuildManager;
import dev.westernpine.gatekeeper.object.Messages;
import dev.westernpine.gatekeeper.util.Messenger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import proj.api.marble.lib.string.Strings;

public class ReactionRole implements Command {

	@Override
	public boolean permissible() {
		return true;
	}

	@Override
	public boolean useRole() {
		return false;
	}

	@Override
	public String getRole() {
		return null;
	}

	@Override
	public Permission getPermission() {
		return Permission.MANAGE_ROLES;
	}

	// command format
	// !rr #taggedChannel <messageID> @TaggedRole

	@Override
	public void execute(Guild guild, User user, MessageChannel ch, Message msg, String command, String[] args) {
		if (args.length != 3) {
			Messenger.sendEmbed(ch, Messages.invalidReactionCommandFormat().build());
		} else {
			String channel = args[0];
			String message = args[1];
			String role = args[2];

			TextChannel mentionedChannel = null;
			Message mentionedMessage = null;
			Role mentionedRole = null;

			if (channel.startsWith("<#") && channel.endsWith(">")) {
				List<TextChannel> mentionedChannels = msg.getMentionedChannels();
				if (mentionedChannels.isEmpty()) {
					Messenger.sendEmbed(ch, Messages.noMentionedChannel().build());
					return;
				}
				mentionedChannel = mentionedChannels.get(0);
			} else {
				Messenger.sendEmbed(ch, Messages.invalidReactionCommandFormat().build());
				return;
			}

			if (Strings.isNumeric(message)) {
				try {
					mentionedMessage = mentionedChannel.retrieveMessageById(message).complete();
					mentionedMessage.getId();
				} catch (Exception e) {
					Messenger.sendEmbed(ch, Messages.unableToFindMentionedMessage().build());
					return;
				}
			} else {
				Messenger.sendEmbed(ch, Messages.invalidReactionCommandFormat().build());
				return;
			}

			if (role.startsWith("<@&") && role.endsWith(">")) {
				List<Role> mentionedRoles = msg.getMentionedRoles();
				if (mentionedRoles.isEmpty()) {
					Messenger.sendEmbed(ch, Messages.noMentionedRole().build());
					return;
				}
				mentionedRole = mentionedRoles.get(0);
			} else {
				Messenger.sendEmbed(ch, Messages.invalidReactionCommandFormat().build());
				return;
			}

			Message sentMessage = ch.sendMessage(Messages.reactionRoleReactionRequiredMessage().build()).complete();
			GuildManager.get(guild.getId()).getReactionRoleManager().listenForNewReaction(ch.getId(),
					sentMessage.getId(), user.getId(), mentionedChannel.getId(), mentionedMessage.getId(),
					mentionedRole.getId());
		}
	}

}
