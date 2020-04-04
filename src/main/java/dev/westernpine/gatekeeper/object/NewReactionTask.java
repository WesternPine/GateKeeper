package dev.westernpine.gatekeeper.object;

import java.awt.Color;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.management.reactions.ReactionRoleManager;
import dev.westernpine.gatekeeper.util.Messenger;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import proj.api.marble.lib.emoji.Emoji;

public class NewReactionTask implements Runnable {
	
	@Getter
	private String guild;
	
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
	private String taggedRole;
	
	public NewReactionTask(String guild, String currentChannel, String currentMessage, String creator, String taggedChannel, String messageId, String taggedRole) {
		this.guild = guild;
		this.currentChannel = currentChannel;
		this.currentMessage = currentMessage;
		this.creator = creator;
		this.taggedChannel = taggedChannel;
		this.messageId = messageId;
		this.taggedRole = taggedRole;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(120000); //2 minutes
			ReactionRoleManager.tasks.remove(this);
		
			TextChannel ch = GateKeeper.getInstance().getManager().getGuildById(guild).getTextChannelById(currentChannel);
			Messenger.delete(ch.getHistory().getMessageById(currentMessage));
			EmbedBuilder builder = Messenger.getEmbedFrame();
			builder.setDescription(Emoji.CrossMark + " **Message reaction setup expired!**");
			builder.setColor(Color.RED);
			ch.sendMessage(builder.build()).queue();
			
		} catch (Exception e) {}
	}
	
	public Thread getThread() {
		return Thread.currentThread();
	}
	
	public void destroy() {
		getThread().interrupt();
		ReactionRoleManager.tasks.remove(this);
	}
}