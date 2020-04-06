package dev.westernpine.gatekeeper.object;

import java.util.Collection;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.management.autoroles.AutoRoleManager;
import dev.westernpine.gatekeeper.util.Messenger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import proj.api.marble.lib.emoji.Emoji;
import proj.api.marble.lib.string.Strings;

public class Messages {
	
	public static EmbedBuilder autoRoles(String guild) {
		Guild g = GateKeeper.getInstance().getManager().getGuildById(guild);
		StringBuilder sb = new StringBuilder();
		sb.append(Emoji.Scroll.getValue() + " All Auto-Roles\n\n");
		for(UserType userType : UserType.values()) {
			Set<String> roleIds = AutoRoleManager.getAutoRoles(userType, guild);
			sb.append("Roles auto-applied to " + Strings.capitalizeFirst(userType.toString().toLowerCase()) + " accounts:");
			for(String roleId : roleIds) {
	            sb.append("\n - " + g.getRoleById(roleId).getAsMention());
	        }
			sb.append("\n\n");
		}
		EmbedBuilder embed = Messenger.getEmbedFrame(guild);
        embed.setDescription(sb);
        return embed;
	}
	
	public static EmbedBuilder rolesApplied(String guild, UserType userType, Collection<String> roleIds) {
		StringBuilder sb = new StringBuilder();
        sb.append(Emoji.Scroll.getValue() + "Roles auto-applied to " + Strings.capitalizeFirst(userType.toString().toLowerCase()) + " accounts are:");
        Guild g = GateKeeper.getInstance().getManager().getGuildById(guild);
        for(String roleId : roleIds) {
            sb.append("\n     - " + g.getRoleById(roleId).getAsMention());
        }
        EmbedBuilder embed = Messenger.getEmbedFrame(guild);
        embed.setDescription(sb);
        return embed;
	}

}
