package dev.westernpine.gatekeeper.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.backend.Backend;
import dev.westernpine.gatekeeper.backend.GuildBackend;
import dev.westernpine.gatekeeper.object.UserType;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
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
					for(String role : value.get().split(","))
						roles.add(role);
				} else {
					roles.add(value.get());
				}
			}
			autoRoles.put(userType, roles);
		}
		g.getMembers().forEach(member -> 
			autoRoles.get(UserType.of(member)).forEach(role -> 
				g.addRoleToMember(member, g.getRoleById(role)).queue()));
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
			compiled = (first ? role : role + "," + compiled);
			first = false;
		}
		GuildBackend backend = Backend.get(guild);
		backend.dropEntry(userType.toString());
		backend.addEntry(userType.toString(), compiled);
	}

}
