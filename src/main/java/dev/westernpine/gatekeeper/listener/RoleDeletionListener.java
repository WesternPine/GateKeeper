package dev.westernpine.gatekeeper.listener;

import java.util.Set;

import dev.westernpine.gatekeeper.management.autoroles.AutoRoleManager;
import dev.westernpine.gatekeeper.management.reactions.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.GuildReactionMap;
import dev.westernpine.gatekeeper.object.UserType;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RoleDeletionListener extends ListenerAdapter {
	
	@Override
	public void onRoleDelete(RoleDeleteEvent event) {
		String guild = event.getGuild().getId();
		
		//auto roles
		for(UserType type : UserType.values()) {
			Set<String> roles = AutoRoleManager.getAutoRoles(type, guild);
			roles.remove(event.getRole().getId());
			AutoRoleManager.setAutoRoles(type, guild, roles);
		}
		
		//reaction roles
		GuildReactionMap map = ReactionRoleManager.getGuildReactionMapFromBackend(guild);
		map.removeRole(event.getRole().getId());
		ReactionRoleManager.updateGuildReactionMapToBackend(map);
	}
	
}
