package dev.westernpine.gatekeeper.management;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import dev.westernpine.common.strings.Strings;
import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.backend.Backend;
import dev.westernpine.gatekeeper.backend.GuildBackend;
import dev.westernpine.gatekeeper.object.Action;
import dev.westernpine.gatekeeper.object.UserType;
import dev.westernpine.gatekeeper.util.RoleUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class AutoRoleManager {

	@Getter
	private String guild;

	HashMap<UserType, String> autoRoles = new HashMap<>();

	AutoRoleManager(String guild) {
		this.guild = guild;
		Guild g = GateKeeper.getInstance().getManager().getGuildById(guild);
		for (UserType userType : UserType.values()) {
			Optional<String> value = Backend.get(guild).getEntryValue(userType.toString());
			if (value.isPresent() && !Strings.resemblesNull(value.get())) {
				autoRoles.put(userType, value.get());
			} else {
				autoRoles.put(userType, "");
			}
		}

		HashMap<UserType, Set<Member>> memberTypes = new HashMap<>();
		Arrays.asList(UserType.values()).forEach(userType -> memberTypes.put(userType, new HashSet<>()));
		g.getMembers().forEach(member -> memberTypes.get(UserType.of(member)).add(member));
		autoRoles.keySet().forEach(userType -> {
			Set<Member> members = memberTypes.get(userType);
			RoleUtils.applyRoleString(autoRoles.get(userType), Action.ADD, members.toArray(new Member[members.size()]));
		});
	}

	void shutdown() {
		autoRoles.keySet().forEach(userType -> offload(userType));
	}

	public Set<String> getAutoRoles(UserType userType) {
		return RoleUtils.toRoleSet(autoRoles.get(userType));
	}

	public void setAutoRoles(UserType userType, Set<String> roles) {
		autoRoles.put(userType, RoleUtils.toRoleString(roles));
		offload(userType);
	}

	private void offload(UserType userType) {
		GuildBackend backend = Backend.get(guild);
		backend.dropEntry(userType.toString());
		backend.addEntry(userType.toString(), autoRoles.get(userType));
	}

}
