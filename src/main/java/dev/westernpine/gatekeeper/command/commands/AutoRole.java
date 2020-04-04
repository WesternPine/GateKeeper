package dev.westernpine.gatekeeper.command.commands;

import java.util.List;
import java.util.Set;

import dev.westernpine.gatekeeper.command.Command;
import dev.westernpine.gatekeeper.command.CommandExecutor;
import dev.westernpine.gatekeeper.management.autoroles.AutoRoleManager;
import dev.westernpine.gatekeeper.object.UserType;
import dev.westernpine.gatekeeper.util.Messenger;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import proj.api.marble.lib.emoji.Emoji;
import proj.api.marble.lib.string.Strings;

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
            CommandExecutor.INVALID_COMMAND.execute(guild, user, ch, msg, command, args);
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
            
            StringBuilder sb = new StringBuilder();
            sb.append(Emoji.Scroll.getValue() + "Roles auto-applied to " + Strings.capitalizeFirst(userType.toString().toLowerCase()) + " accounts are:");
            for(String roleId : roleIds) {
                sb.append("\n     - " + guild.getRoleById(roleId).getAsMention());
            }
            EmbedBuilder embed = Messenger.getEmbedFrame(guild);
            embed.setDescription(sb);
            Messenger.sendEmbed(ch, embed.build());
        } else {
            CommandExecutor.INVALID_COMMAND.execute(guild, user, ch, msg, command, args);
        }
    }

}
