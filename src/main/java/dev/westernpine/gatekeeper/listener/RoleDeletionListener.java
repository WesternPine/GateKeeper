package dev.westernpine.gatekeeper.listener;

import java.util.Set;

import dev.westernpine.gatekeeper.management.AutoRoleManager;
import dev.westernpine.gatekeeper.management.GuildManager;
import dev.westernpine.gatekeeper.management.Manager;
import dev.westernpine.gatekeeper.management.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.UserType;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleDeletionListener extends ListenerAdapter {

	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		String guild = event.getGuild().getId();

		Manager manager = GuildManager.get(guild);
		AutoRoleManager arManager = manager.getAutoRoleManager();
		ReactionRoleManager rrManager = manager.getReactionRoleManager();

		// auto roles
		for (UserType type : UserType.values()) {
			Set<String> roles = arManager.getAutoRoles(type);
			roles.remove(event.getRole().getId());
			arManager.setAutoRoles(type, roles);
		}

		// reaction roles
		rrManager.remove(event.getRole().getId(), Role.class);
	}

}
