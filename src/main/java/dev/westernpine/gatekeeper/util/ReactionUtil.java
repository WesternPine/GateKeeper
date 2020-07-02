package dev.westernpine.gatekeeper.util;

import dev.westernpine.common.strings.Strings;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.internal.utils.EncodingUtil;

public class ReactionUtil {

	// PROBLEM AREA -> look in role manager #apply roles for detials
	public static String getId(ReactionEmote reaction) {
		return reaction.isEmote() ? reaction.getEmote().getId() : reaction.getAsCodepoints();
	}

	public static ReactionEmote getReaction(Message message, String fromId) {
		return Strings.isNumeric(fromId) ? getReactionEmoteIdFromEmoteId(message, fromId)
				: message.getReactionByUnicode(EncodingUtil.decodeCodepoint(fromId));
	}

	private static ReactionEmote getReactionEmoteIdFromEmoteId(Message message, String emoteId) {
		for (MessageReaction mr : message.getReactions()) {
			ReactionEmote reactionEmote = mr.getReactionEmote();
			if (reactionEmote.isEmote()) {
				if (reactionEmote.getId().equals(emoteId)) {
					return reactionEmote;
				}
			}
		}
		return null;
	}

}
