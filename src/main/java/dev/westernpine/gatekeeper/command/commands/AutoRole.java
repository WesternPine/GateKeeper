package dev.westernpine.gatekeeper.command.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.westernpine.gatekeeper.command.Command;
import dev.westernpine.gatekeeper.management.AutoRoleManager;
import dev.westernpine.gatekeeper.management.GuildManager;
import dev.westernpine.gatekeeper.object.Action;
import dev.westernpine.gatekeeper.object.Messages;
import dev.westernpine.gatekeeper.object.UserType;
import dev.westernpine.gatekeeper.util.Messenger;
import dev.westernpine.gatekeeper.util.RoleUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public class AutoRole implements Command {

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

	@Override
	public void execute(Guild guild, User user, MessageChannel ch, Message msg, String command, String[] args) {
		AutoRoleManager arManager = GuildManager.get(guild.getId()).getAutoRoleManager();
		if (args.length == 0) {
			Messenger.sendEmbed(ch, Messages.autoRoles(guild).build());
		} else if (args.length == 1) {
			UserType userType = null;
			try {
				userType = UserType.of(args[0]);
			} catch (Exception e) {
			}
			if (userType == null) {
				Messenger.sendEmbed(ch, Messages.invalidUserType().build());
				return;
			}
			Messenger.sendEmbed(ch, Messages.rolesApplied(guild, userType, arManager.getAutoRoles(userType)).build());
		} else if (args.length >= 2) {
			UserType userType = null;
			try {
				userType = UserType.of(args[0]);
			} catch (Exception e) {
			}
			if (userType == null) {
				Messenger.sendEmbed(ch, Messages.invalidUserType().build());
				return;
			}

			List<Role> rolesToModify = msg.getMentionedRoles();
			if (rolesToModify == null || rolesToModify.isEmpty()) {
				Messenger.sendEmbed(ch, Messages.noMentionedRoles().build());
				return;
			}

			Set<String> roleIds = arManager.getAutoRoles(userType);
			for (Role role : rolesToModify) {
				if (roleIds.contains(role.getId())) {
					roleIds.remove(role.getId());
				} else {
					roleIds.add(role.getId());
				}
			}
			arManager.setAutoRoles(userType, roleIds);
			Messenger.sendEmbed(ch, Messages.rolesApplied(guild, userType, roleIds).build());

			Set<Member> membersToModify = new HashSet<>();
			for (Member member : guild.getMembers()) {
				if (UserType.of(member) == userType) {
					membersToModify.add(member);
				}
			}
			RoleUtils.applyRoleString(RoleUtils.toRoleString(roleIds), Action.ADD, membersToModify.toArray(new Member[membersToModify.size()]));
		}
	}

}
