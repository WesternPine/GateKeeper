package dev.westernpine.gatekeeper.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dev.westernpine.common.strings.Strings;
import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.object.Action;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class RoleUtils {
	
	public static void applyRoleString(String roleString, Action action, String guildId, Member...members) {
		if(members.length == 0)
			return;
		Guild guild = GateKeeper.getInstance().getManager().getGuildById(guildId);
		Set<String> roles = new HashSet<>();
		if(!Strings.resemblesNull(roleString)) {
			if(!roleString.contains(", ")) {
				roles.add(roleString);
			} else {
				for(String role : roleString.split(", ")) {
					roles.add(role);
				}
			}
		}
		
		if(roles.size() == 0)
			return;
		
		roles.forEach(roleId -> {
			Role role = guild.getRoleById(roleId);
			//This way of applying roles is purposely NOT giving the roles to EVERYONE who didnt react.
			//Reasoning is for it's specific use case of only removing romoving roles AFTER
			//they reacted the first time. Meaning they have to double react, and the role isn't
			//distributed to everyone on every statup.
			for(Member member : members)
				if(member != null)
					if(!guild.getSelfMember().getId().equals(member.getId()))
						if(action == Action.ADD)
							guild.addRoleToMember(member, role).queue();
						else
							guild.removeRoleFromMember(member, role).queue();
		});
	}
	
	public static Set<String> filterMissingRoles(String guild, String roleString) {
		Guild g = GateKeeper.getInstance().getManager().getGuildById(guild);
		Set<String> roles = new HashSet<>();
		if(!Strings.resemblesNull(roleString)) {
			if(!roleString.contains(", ")) {
				if(g.getRoleById(roleString) != null) {
					roles.add(roleString);
				}
			} else {
				for(String role : roleString.split(", ")) {
					if(g.getRoleById(role) != null) {
						roles.add(role);
					}
				}
			}
		}
		return roles;
	}
	
	public static Set<String> toRoleSet(String roleString) {
		Set<String> roles = new HashSet<>();
		if(!Strings.resemblesNull(roleString)) {
			if(!roleString.contains(", ")) {
				roles.add(roleString);
			} else {
				for(String role : roleString.split(", ")) {
					roles.add(role);
				}
			}
		}
		return roles;
	}
	
	public static String toRoleString(Collection<String> roles) {
		return String.join(", ", roles.toArray(new String[roles.size()]));
	}

}
