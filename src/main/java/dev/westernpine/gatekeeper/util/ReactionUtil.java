package dev.westernpine.gatekeeper.util;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.internal.utils.EncodingUtil;
import proj.api.marble.lib.string.Strings;

public class ReactionUtil {
	
	//PROBLEM AREA -> look in role manager #apply roles for detials
	public static String getId(ReactionEmote reaction) {
		return reaction.isEmote() ? reaction.getId() : reaction.getAsCodepoints();
	}
	
	public static ReactionEmote getReaction(Message message, String fromId) {
		return Strings.isNumeric(fromId) ? message.getReactionById(fromId) : message.getReactionByUnicode(EncodingUtil.decodeCodepoint(fromId));
	}

}
