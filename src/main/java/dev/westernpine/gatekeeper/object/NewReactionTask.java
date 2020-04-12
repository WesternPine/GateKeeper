package dev.westernpine.gatekeeper.object;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.management.GuildManager;
import dev.westernpine.gatekeeper.util.Messenger;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.TextChannel;

public class NewReactionTask implements Runnable {

	@Getter
	private String guild;
	
	@Getter
	private Action action;

	@Getter
	private String currentChannel;

	@Getter
	private String currentMessage;

	@Getter
	private String creator;

	@Getter
	private String taggedChannel;

	@Getter
	private String messageId;

	@Getter
	private Set<String> taggedRoles;

	@Setter
	private Thread thread;

	public NewReactionTask(String guild, Action action, String currentChannel, String currentMessage, String creator,
			String taggedChannel, String messageId, Collection<String> taggedRoles) {
		this.guild = guild;
		this.action = action;
		this.currentChannel = currentChannel;
		this.currentMessage = currentMessage;
		this.creator = creator;
		this.taggedChannel = taggedChannel;
		this.messageId = messageId;
		this.taggedRoles = new HashSet<>(taggedRoles);
	}

	@Override
	public void run() {
		try {
			Thread.sleep(120000); // 2 minutes
			end(true);
		} catch (Exception e) {
		}
	}

	public void end(boolean sendExpiredMessage) {
		TextChannel ch = GateKeeper.getInstance().getManager().getGuildById(guild).getTextChannelById(currentChannel);
		try {
			Messenger.delete(ch.retrieveMessageById(currentMessage).complete());
		} catch (Exception e) {
		}
		if (sendExpiredMessage) {
			GuildManager.get(guild).getReactionRoleManager().reactionTask = null;
			Messenger.sendEmbed(ch, Messages.reactionSetupExpired().build());
		}

		// Call after deletion, otherwise message wont delete since same thread
		thread.interrupt();
	}
}