package dev.westernpine.gatekeeper.object;

import java.awt.Color;
import java.util.Collection;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.management.GuildManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import proj.api.marble.lib.emoji.Emoji;
import proj.api.marble.lib.string.Strings;

public class Messages {

	public static EmbedBuilder getEmbedFrame() {
		return new EmbedBuilder().setColor(Color.BLACK);
	}

	/*
	 * Error messages
	 */

	public static EmbedBuilder noPermission() {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark.getValue() + " **I was unable to perform that action for you.**");
	}

	public static EmbedBuilder invalidUserType() {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark.getValue() + " **Invalid user type. (\"bot\" or \"client\")**");
	}

	public static EmbedBuilder unableToFindMentionedMessage() {
		return getEmbedFrame().setColor(Color.RED).setDescription(
				Emoji.CrossMark.getValue() + " **Unable to find mentioned message in the mentioned channel.**");
	}

	public static EmbedBuilder noMentionedMessage() {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark.getValue() + " **No message mentioned.**");
	}

	public static EmbedBuilder noMentionedChannel() {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark.getValue() + " **No channel mentioned.**");
	}

	public static EmbedBuilder noMentionedRole() {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark.getValue() + " **No role mentioned.**");
	}

	public static EmbedBuilder noMentionedRoles() {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark.getValue() + " **No roles mentioned.**");
	}

	public static EmbedBuilder reactionSetupExpired() {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark.getValue() + " **Reaction role setup expired.**");
	}

	public static EmbedBuilder failedToApplyReactionRole(String reason) {
		return getEmbedFrame().setColor(Color.RED)
				.setDescription(Emoji.CrossMark + " **Failed to apply reaction role to message!**\n`" + reason + "`");
	}

	public static EmbedBuilder invalidReactionCommandFormat() {
		return getEmbedFrame().setColor(Color.RED).setDescription(
				Emoji.CrossMark + " **Invalid command format. (\"" + GateKeeper.getInstance().getPrefix()
						+ "reactionrole #TaggedChannel NumericMessageID @MentionedRole\")");
	}

	/*
	 * Success messages
	 */

	public static EmbedBuilder reactionRoleApplied() {
		return getEmbedFrame().setColor(Color.GREEN).setDescription(Emoji.GreenCheck + " **Reaction role applied!**");
	}

	public static EmbedBuilder reactionRoleReactionRequiredMessage() {
		return getEmbedFrame().setColor(Color.GREEN)
				.setDescription(Emoji.GreenCheck + " **Reaction role message credentials verified!** "
						+ "\nPlease react to **THIS** message, with the reaction role."
						+ "\nYou have 2 minutes, before this action is cancelled.");
	}

	/*
	 * General Messages
	 */

	public static EmbedBuilder autoRoles(Guild guild) {
		StringBuilder sb = new StringBuilder();
		sb.append(Emoji.Scroll.getValue() + " All Auto-Roles\n\n");
		for (UserType userType : UserType.values()) {
			Set<String> roleIds = GuildManager.get(guild.getId()).getAutoRoleManager().getAutoRoles(userType);
			sb.append("Roles auto-applied to " + Strings.capitalizeFirst(userType.toString().toLowerCase())
					+ " accounts:");
			for (String roleId : roleIds) {
				sb.append("\n - " + guild.getRoleById(roleId).getAsMention());
			}
			sb.append("\n\n");
		}
		EmbedBuilder embed = getEmbedFrame();
		embed.setDescription(sb);
		return embed;
	}

	public static EmbedBuilder rolesApplied(Guild guild, UserType userType, Collection<String> roleIds) {
		StringBuilder sb = new StringBuilder();
		sb.append(Emoji.Scroll.getValue() + "Roles auto-applied to "
				+ Strings.capitalizeFirst(userType.toString().toLowerCase()) + " accounts are:");
		for (String roleId : roleIds) {
			sb.append("\n     - " + guild.getRoleById(roleId).getAsMention());
		}
		EmbedBuilder embed = getEmbedFrame();
		embed.setDescription(sb);
		return embed;
	}

}
