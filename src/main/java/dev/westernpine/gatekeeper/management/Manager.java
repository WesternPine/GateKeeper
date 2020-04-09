package dev.westernpine.gatekeeper.management;

import lombok.Getter;

public class Manager {
	
	@Getter
	private String guild;
	
	@Getter
	private AutoRoleManager autoRoleManager;
	
	@Getter
	private ReactionRoleManager reactionRoleManager;
	
	Manager(String guild) {
		this.guild = guild;
		this.autoRoleManager = new AutoRoleManager(guild);
		this.reactionRoleManager = new ReactionRoleManager(guild);
	}
	
	public void shutdown() {
		autoRoleManager.shutdown();
		reactionRoleManager.shutdown();
	}

}
