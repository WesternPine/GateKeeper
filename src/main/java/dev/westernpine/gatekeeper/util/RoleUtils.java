package dev.westernpine.gatekeeper.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class RoleUtils {
	
	public static void applyRoles(Member member, Collection<String> roleIds) {
		if(roleIds.isEmpty()) return;
		Guild guild = member.getGuild();
		Set<Role> roles = new HashSet<>();
		roleIds.forEach(roleId -> roles.add(guild.getRoleById(roleId)));
		guild.modifyMemberRoles(member, roles, new HashSet<>()).queue();
	}
	
	public static void applyRoles(Collection<Member> members, Collection<String> roleIds) {
		if(members.isEmpty()) return;
		if(roleIds.isEmpty()) return;
		Guild guild = members.iterator().next().getGuild();
		Set<Role> roles = new HashSet<>();
		roleIds.forEach(roleId -> roles.add(guild.getRoleById(roleId)));
		members.forEach(member -> guild.modifyMemberRoles(member, roles, new HashSet<>()).queue());
		
	}
	
	public static void applyRole(Member member, String roleId) {
		Guild guild = member.getGuild();
		Role role = guild.getRoleById(roleId);
		guild.addRoleToMember(member, role).queue();
	}
	
	public static void applyRole(Collection<Member> members, String roleId) {
		if(members.isEmpty()) return;
		Guild guild = members.iterator().next().getGuild();
		Role role = guild.getRoleById(roleId);
		members.forEach(member -> guild.addRoleToMember(member, role).queue());
	}
	
}
