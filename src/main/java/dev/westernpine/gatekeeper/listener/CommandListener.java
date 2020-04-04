package dev.westernpine.gatekeeper.listener;

import java.util.Arrays;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.command.CommandExecutor;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	
	@Override
    public void onMessageReceived(MessageReceivedEvent event) {
		String prefix = GateKeeper.getInstance().getPrefix();
        Message msg = event.getMessage();
        MessageChannel ch = event.getChannel();
        if (msg.getContentRaw().startsWith(prefix)) {
            User user = event.getAuthor();
            String command = msg.getContentRaw().split(" ")[0].replace(prefix, "");
            String[] args = Arrays.copyOfRange(msg.getContentRaw().split(" "), 1, msg.getContentRaw().split(" ").length);
            CommandExecutor.getCommand(command, msg.getGuild(), user).execute(event.getGuild(), user, ch, msg, command, args);
        }
    }

}
