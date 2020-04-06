package dev.westernpine.gatekeeper.command.commands;

import java.util.List;
import java.util.Set;

import dev.westernpine.gatekeeper.command.Command;
import dev.westernpine.gatekeeper.command.CommandExecutor;
import dev.westernpine.gatekeeper.management.autoroles.AutoRoleManager;
import dev.westernpine.gatekeeper.object.Messages;
import dev.westernpine.gatekeeper.object.UserType;
import dev.westernpine.gatekeeper.util.Messenger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
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
        if(args.length == 0) {
            Messenger.sendEmbed(ch, Messages.autoRoles(guild.getId()).build());
        } else if(args.length >= 2) {
            UserType userType = UserType.of(args[0]);
            if(userType == null) {
                CommandExecutor.INVALID_COMMAND.execute(guild, user, ch, msg, command, args);
                return;
            }
            List<Role> rolesToModify = msg.getMentionedRoles();
            if(rolesToModify == null || rolesToModify.isEmpty()) {
                CommandExecutor.INVALID_COMMAND.execute(guild, user, ch, msg, command, args);
                return;
            }
            Set<String> roleIds = AutoRoleManager.getAutoRoles(userType, guild.getId());
            for(Role role : rolesToModify) {
                if(roleIds.contains(role.getId())) {
                    roleIds.remove(role.getId());
                } else {
                    roleIds.add(role.getId());
                }
            }
            AutoRoleManager.setAutoRoles(userType, guild.getId(), roleIds);
            Messenger.sendEmbed(ch, Messages.rolesApplied(guild.getId(), userType, roleIds).build());
        } else {
            CommandExecutor.INVALID_COMMAND.execute(guild, user, ch, msg, command, args);
        }
    }

}
