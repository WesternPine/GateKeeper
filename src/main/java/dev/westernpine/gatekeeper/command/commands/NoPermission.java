package dev.westernpine.gatekeeper.command.commands;

import dev.westernpine.gatekeeper.command.Command;
import dev.westernpine.gatekeeper.object.Messages;
import dev.westernpine.gatekeeper.util.Messenger;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class NoPermission implements Command {

	@Override
	public boolean permissible() {
		return false;
	}

	@Override
	public boolean useRole() {
		return false;
	}

	@Override
	public String getRole() {
		return null;
	}

	@Override
	public Permission getPermission() {
		return null;
	}

	@Override
	public void execute(Guild guild, User user, MessageChannel ch, Message msg, String command, String[] args) {
		try {
			msg.delete().queue();
		} catch (Exception e) {
		}
		Messenger.clearDM(user);
		Messenger.sendEmbed(user, Messages.noPermission().build());
	}

}
