package dev.westernpine.gatekeeper.util;

import dev.westernpine.gatekeeper.GateKeeper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

public class Messenger {

	private static boolean debug = true;

	public static EmbedBuilder getEmbedFrame(String guild) {
		return new EmbedBuilder().setColor(GateKeeper.defColor(GateKeeper.getInstance().getManager().getGuildById(guild)));
	}

	public static EmbedBuilder getEmbedFrame() {
		return new EmbedBuilder();
	}

	public static void clearDM(User user) {
		User self = user.getJDA().getSelfUser();

		user.openPrivateChannel().queue(channel -> {
			channel.getIterableHistory().queue(message -> {
				message.forEach(msg -> {
					if (msg.getAuthor().getId().equals(self.getId())) {
						msg.delete().queue();
					}
				});
			});
		});
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
