package dev.westernpine.gatekeeper.management;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.backend.Backend;
import dev.westernpine.gatekeeper.backend.GuildBackend;
import dev.westernpine.gatekeeper.object.UserType;
import dev.westernpine.gatekeeper.util.RoleUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import proj.api.marble.lib.string.Strings;

public class AutoRoleManager {
	
	@Getter
	private String guild;
	
	HashMap<UserType, Set<String>> autoRoles = new HashMap<>();
	
	AutoRoleManager(String guild) {
		this.guild = guild;
		Guild g = GateKeeper.getInstance().getManager().getGuildById(guild);
		for(UserType userType : UserType.values()) {
			Set<String> roles = new HashSet<>();
			Optional<String> value = Backend.get(guild).getEntryValue(userType.toString());
			if(value.isPresent() && !Strings.resemblesNull(value.get())) {
				if(value.get().contains(",")) {
					for(String role : value.get().split(", "))
						roles.add(role);
				} else {
					roles.add(value.get());
				}
			}
			autoRoles.put(userType, roles);
		}
		
		HashMap<UserType, Set<Member>> memberTypes = new HashMap<>();
		Arrays.asList(UserType.values()).forEach(userType -> memberTypes.put(userType, new HashSet<>()));
		g.getMembers().forEach(member -> memberTypes.get(UserType.of(member)).add(member));
		autoRoles.keySet().forEach(userType -> RoleUtils.applyRoles(memberTypes.get(userType), autoRoles.get(userType)));
	}
	
	void shutdown() {
		autoRoles.keySet().forEach(userType -> offload(userType));
	}
	
	public Set<String> getAutoRoles(UserType userType) {
		return autoRoles.get(userType);
	}
	
	public void setAutoRoles(UserType userType, Set<String> roles) {
		autoRoles.put(userType, roles);
		offload(userType);
	}
	
	private void offload(UserType userType) {
		String compiled = "";
		boolean first = true;
		for(String role : autoRoles.get(userType)) {
			compiled = (first ? role : role + ", " + compiled);
			first = false;
		}
		GuildBackend backend = Backend.get(guild);
		backend.dropEntry(userType.toString());
		backend.addEntry(userType.toString(), compiled);
	}

}
