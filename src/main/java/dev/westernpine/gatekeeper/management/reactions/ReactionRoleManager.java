package dev.westernpine.gatekeeper.management.reactions;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.backend.Backend;
import dev.westernpine.gatekeeper.backend.GuildBackend;
import dev.westernpine.gatekeeper.object.GuildReactionMap;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

public class ReactionRoleManager {
	
	public static void initialize() {
		ShardManager manager = GateKeeper.getInstance().getManager();
		for(Guild guild : manager.getGuilds()) {
			GuildReactionMap map = getGuildReactionMapFromBackend(guild.getId());
			map = verifyAndSynchronize(map);
			updateGuildReactionMapToBackend(map);
			applyRoles(map);
			
		}
	}
	
	public static GuildReactionMap getGuildReactionMapFromBackend(String guild) {
		Optional<String> reactionsJson = Backend.get(guild).getEntryValue("reactions");
		return new GuildReactionMap(guild, reactionsJson.isPresent() ? (String) reactionsJson.get() : "");
	}
	
	public static void updateGuildReactionMapToBackend(GuildReactionMap map) {
		GuildBackend backend = Backend.get(map.getGuild());
		backend.dropEntry("reactions");
		backend.addEntry("reactions", map.toString());
	}
	
	public static GuildReactionMap verifyAndSynchronize(GuildReactionMap map) {
		ShardManager manager = GateKeeper.getInstance().getManager();
		Guild guild = manager.getGuildById(map.getGuild());
		
		Set<String> pathsToRemove = new HashSet<>();
		Set<String> roles = new HashSet<>();
		
		for(String channel : map.getMap().keySet()) {
			TextChannel ch = GateKeeper.getInstance().getManager().getTextChannelById(channel);
			if(ch != null) {
				for(String message : map.getMap().get(channel).keySet()) {
					Message msg = ch.getHistory().getMessageById(message);
					if(msg != null) {
						for(String reaction : map.getMap().get(channel).get(message).keySet()) {
							ReactionEmote re = msg.getReactionById(reaction);
							if(re != null) {
								roles.add(map.getMap().get(channel).get(message).get(reaction));
							} else {
								pathsToRemove.add(channel + "," + message + "," + reaction);
							}
						}
					} else {
						pathsToRemove.add(channel + "," + message);
					}
				}
			} else {
				pathsToRemove.add(channel);
			}
		}
		
		for(String role : new HashSet<>(roles)) {
			Role ro = guild.getRoleById(role);
			if(ro != null) {
				roles.remove(role);
			}
		}
		
		roles.forEach(role -> map.removeRole(role));
		pathsToRemove.forEach(path -> {
			if(path.contains(",")) {
				map.remove(path);
			} else {
				String[] split = path.split(",");
				if(split.length == 2) {
					map.remove(split[0], split[1]);
				} else {
					map.remove(split[0], split[1], split[2]);
				}
			}
		});
		
		return map;
	}
	
	public static void applyRoles(GuildReactionMap map) {
		ShardManager manager = GateKeeper.getInstance().getManager();
		for (String channel : map.getMap().keySet()) {
			TextChannel ch = manager.getGuildById(map.getGuild()).getTextChannelById(channel);
			for (String message : map.getMap().get(channel).keySet()) {
				Message msg = ch.getHistory().getMessageById(message);
				
				for(MessageReaction reaction : msg.getReactions()) {
					Role role = manager.getGuildById(map.getGuild()).getRoleById(map.getMap().get(channel).get(message).get(reaction.getReactionEmote().getId()));
					reaction.retrieveUsers().queue(users -> {
						users.forEach(user -> {
							Member member = manager.getGuildById(map.getGuild()).getMember(user);
							manager.getGuildById(map.getGuild()).addRoleToMember(member, role);
						});
					});
				}
			}
		}
	}
	
	/*
	 * FROM HERE DOWN
	 * All related to setting up a new reaction role
	 */
	
	static Set<NewReactionTask> tasks = Collections.synchronizedSet(new HashSet<NewReactionTask>());
	
	public static NewReactionTask getActiveReactionTask(String guild) {
		for(NewReactionTask task : tasks) {
			if(task.getGuild().equals(guild)) {
				return task;
			}
		}
		return null;
	}
	
	public static boolean removeAvtiveReactionListener(String guild) {
		boolean anyActive = false;
		NewReactionTask task = getActiveReactionTask(guild);
		if(task != null) {
			anyActive = true;
			task.getThread().interrupt();
			tasks.remove(task);
		}
		return anyActive;
	}
	
	public static void listenForNewReaction(String guild, String channel, String message) {
		removeAvtiveReactionListener(guild);
		NewReactionTask task = new NewReactionTask(guild, channel, message);
		tasks.add(task);
		new Thread(task).run();
	}

}

class NewReactionTask implements Runnable {
	
	@Getter
	private String guild;
	
	@Getter
	private String channel;
	
	@Getter
	private String message;
	
	NewReactionTask(String guild, String channel, String message) {
		this.channel = channel;
		this.message = message;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(120000); //2 minutes
			ReactionRoleManager.tasks.remove(this);
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