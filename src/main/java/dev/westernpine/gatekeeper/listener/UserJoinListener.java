package dev.westernpine.gatekeeper.listener;

import java.util.Set;

import dev.westernpine.gatekeeper.management.autoroles.AutoRoleManager;
import dev.westernpine.gatekeeper.object.UserType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class UserJoinListener extends ListenerAdapter {
	
	@Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        Set<String> roles = AutoRoleManager.getAutoRoles(UserType.of(member), event.getGuild().getId());
        roles.forEach(role -> event.getGuild().addRoleToMember(member, event.getGuild().getRoleById(role)).queue());
    }
	
}
