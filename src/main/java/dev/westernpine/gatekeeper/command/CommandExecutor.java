package dev.westernpine.gatekeeper.command;

import dev.westernpine.gatekeeper.authenticator.Authenticator;
import dev.westernpine.gatekeeper.command.commands.AutoRole;
import dev.westernpine.gatekeeper.command.commands.Help;
import dev.westernpine.gatekeeper.command.commands.NA;
import dev.westernpine.gatekeeper.command.commands.NoPermission;
import dev.westernpine.gatekeeper.command.commands.ReactionRole;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public enum CommandExecutor {
//    HELP("HELP", new Help()),
	AutoRole("AutoRole", new AutoRole()), AR("AR", new AutoRole()),

	ReactionRole("ReactionRole", new ReactionRole()), RR("RR", new ReactionRole()),

	HELP("HELP", new Help()),

	NA("NA", new NA()), NP("NP", new NoPermission()),;

	@Getter
	private String identifier;

	@Getter
	private Command command;

	CommandExecutor(String identifier, Command command) {
		this.identifier = identifier;
		this.command = command;
	}

	public static CommandExecutor getCommand(String cmd, Guild guild, User user) {
		for (CommandExecutor ce : CommandExecutor.values()) {
			if (ce.getIdentifier().equalsIgnoreCase(cmd)) {
				if (ce.getCommand().permissible()) {
					if (ce.getCommand().useRole()) {
						if (Authenticator.hasRole(guild.getMember(user), ce.getCommand().getRole())) {
							return ce;
						} else {
							return NP;
						}
					} else {
						if (Authenticator.hasPermission(guild.getMember(user), ce.getCommand().getPermission())) {
							return ce;
						} else {
							return NP;
						}
					}
				} else {
					return ce;
				}
			}
		}
		return NA;
	}

	public void execute(Guild guild, User user, MessageChannel ch, Message msg, String command, String[] args) {
		getCommand().execute(guild, user, ch, msg, command, args);
	}
}
