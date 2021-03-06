package dev.westernpine.gatekeeper.util;

import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.User;

public class Messenger {

	private static boolean debug = true;

	public static void clearDM(User user) {
		User self = user.getJDA().getSelfUser();
		PrivateChannel channel = user.openPrivateChannel().complete();
		List<Message> messages = channel.getIterableHistory().complete();
		for (Message message : messages) {
			if (message.getAuthor().getId().equals(self.getId())) {
				message.delete().complete();
			}
		}
	}

	public static void sendMessage(MessageChannel ch, String message) {
		try {
			ch.sendMessage(message).queue();
		} catch (Exception e) {
			if (debug)
				e.printStackTrace();
		}
	}

	public static void sendEmbed(MessageChannel ch, MessageEmbed embed) {
		try {
			ch.sendMessage(embed).queue();
		} catch (Exception e) {
			if (debug)
				e.printStackTrace();
		}
	}

	public static void sendMessage(User user, String message) {
		try {
			user.openPrivateChannel().queue(ch -> ch.sendMessage(message).queue());
		} catch (Exception e) {
			if (debug)
				e.printStackTrace();
		}
	}

	public static void sendEmbed(User user, MessageEmbed embed) {
		try {
			user.openPrivateChannel().queue(ch -> ch.sendMessage(embed).queue());
		} catch (Exception e) {
			if (debug)
				e.printStackTrace();
		}
	}

	public static void delete(Message message) {
		try {
			if (!isDeleted(message)) {
				message.delete().complete();
			}
		} catch (Exception e) {
		}
	}

	public static boolean isDeleted(Message message) {
		if (message == null)
			return true;
		try {
			return isDeleted(message.getChannel(), message.getId());
		} catch (Exception e) {
			return true;
		}
	}

	public static boolean isDeleted(MessageChannel channel, String messageId) {
		if (messageId == null || channel == null)
			return true;
		try {
			channel.retrieveMessageById(messageId).complete().getAuthor();
			return false;
		} catch (Exception e) {
			return true;
		}
	}

}
