package dev.westernpine.gatekeeper.authenticator;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class Authenticator {
	
	public static boolean hasPermission(Member member, Permission permission) {
        if(member.isOwner()) return true;
        for (Role role : member.getRoles()) {
            if (role.getPermissions().contains(permission) || role.getPermissions().contains(Permission.ADMINISTRATOR)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean hasRole(Member member, String roleName) {
        for (Role role : member.getRoles()) {
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

}
