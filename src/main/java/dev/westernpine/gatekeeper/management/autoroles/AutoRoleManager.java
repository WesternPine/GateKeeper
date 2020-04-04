package dev.westernpine.gatekeeper.management.autoroles;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.backend.Backend;
import dev.westernpine.gatekeeper.backend.GuildBackend;
import dev.westernpine.gatekeeper.object.UserType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import proj.api.marble.lib.string.Strings;

public class AutoRoleManager {
	
	public static void initialize() {
		for(Guild guild : GateKeeper.getInstance().getManager().getGuilds()) {
			HashMap<UserType, Set<Role>> accountRoles = new HashMap<>();
			for(UserType type : UserType.values()) {
				Set<Role> roles = new HashSet<>();
				Set<String> stringRoles = getAutoRoles(type, guild.getId());
				for(String stringRole : new HashSet<>(stringRoles)) {
					Role role = guild.getRoleById(stringRole);
					if(role == null) {
						stringRoles.remove(stringRole);
					} else {
						roles.add(role);
					}
				}
				accountRoles.put(type, roles);
				setAutoRoles(type, guild.getId(), stringRoles);
			}
			
			//for every member and user type, add roles accordingly
			guild.getMembers().forEach(member -> accountRoles.get(UserType.of(member)).forEach(role -> guild.addRoleToMember(member, role).queue()));
		}
	}
	
	public static Set<String> getAutoRoles(UserType type, String guild) {
		Set<String> roles = new HashSet<>();
		Optional<String> value = Backend.get(guild).getEntryValue(type.toString());
		if(value.isPresent() && !Strings.resemblesNull(value.get())) {
			if(value.get().contains(",")) {
				for(String role : value.get().split(","))
					roles.add(role);
			} else {
				roles.add(value.get());
			}
		}
		return roles;
	}
	
	public static void setAutoRoles(UserType type, String guild, Collection<String> roles) {
		String compiled = "";
		boolean first = true;
		for(String role : roles) {
			compiled = (first ? role : role + "," + compiled);
			first = false;
		}
		GuildBackend backend = Backend.get(guild);
		backend.dropEntry(type.toString());
		backend.addEntry(type.toString(), compiled);
	}
	
}
