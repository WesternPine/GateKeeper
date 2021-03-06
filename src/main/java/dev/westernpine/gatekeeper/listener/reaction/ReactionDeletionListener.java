package dev.westernpine.gatekeeper.listener.reaction;

import java.util.Arrays;

import dev.westernpine.gatekeeper.management.GuildManager;
import dev.westernpine.gatekeeper.management.ReactionRoleManager;
import dev.westernpine.gatekeeper.object.Action;
import dev.westernpine.gatekeeper.util.ReactionUtil;
import dev.westernpine.gatekeeper.util.RoleUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveAllEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEmoteEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionDeletionListener extends ListenerAdapter {

	// remove role from person that reacted

	// Single emote removed
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		Guild g = event.getGuild();
		String guild = g.getId();
		Member member = event.getMember();
		String userRemoved = member.getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		String reaction = ReactionUtil.getId(event.getReactionEmote());

		ReactionRoleManager rrManager = GuildManager.get(guild).getReactionRoleManager();

		if (!g.getSelfMember().getId().equals(userRemoved)) {
			if(rrManager.getMap().containsKey(channel))
				if(rrManager.getMap().get(channel).containsKey(message))
					if(rrManager.getMap().get(channel).get(message).containsKey(reaction))
						Arrays.asList(Action.values()).forEach(action -> {
							if(rrManager.getMap().get(channel).get(message).get(reaction).containsKey(action))
								RoleUtils.applyRoleString(rrManager.getMap().get(channel).get(message).get(reaction).get(action), action.getOpposite(), guild, member);
						});
		} else {
			rrManager.removeReaction(channel, message, reaction);
		}

	}

	// All of EVERY emote was removed
	@Override
	public void onGuildMessageReactionRemoveAll(GuildMessageReactionRemoveAllEvent event) {
		String guild = event.getGuild().getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		GuildManager.get(guild).getReactionRoleManager().removeMessage(channel, message);
	}

	// All of a SINGLE emote was removed
	@Override
	public void onGuildMessageReactionRemoveEmote(GuildMessageReactionRemoveEmoteEvent event) {
		String guild = event.getGuild().getId();
		String channel = event.getChannel().getId();
		String message = event.getMessageId();
		String reaction = ReactionUtil.getId(event.getReactionEmote());
		GuildManager.get(guild).getReactionRoleManager().removeReaction(channel, message, reaction);
	}

}
