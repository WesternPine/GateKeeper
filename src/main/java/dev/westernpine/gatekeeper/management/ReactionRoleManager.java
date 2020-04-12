package dev.westernpine.gatekeeper.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import dev.westernpine.gatekeeper.GateKeeper;
import dev.westernpine.gatekeeper.backend.Backend;
import dev.westernpine.gatekeeper.backend.GuildBackend;
import dev.westernpine.gatekeeper.object.Action;
import dev.westernpine.gatekeeper.object.NewReactionTask;
import dev.westernpine.gatekeeper.util.ReactionUtil;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import proj.api.marble.lib.string.Strings;

public class ReactionRoleManager {

	@Getter
	private String guild;

	public NewReactionTask reactionTask;

	private HashMap<String, HashMap<String, HashMap<String, String>>> map;

	//for effeciency reasons, only apply roles on creation.
	ReactionRoleManager(String guild) {
		this.guild = guild;
		refresh();
		applyRoles();
	}

	public void refresh() {
		map = new HashMap<>();
		Optional<String> optionalJson = Backend.get(guild).getEntryValue("reactions");
		String jsonMap = optionalJson.isPresent() ? optionalJson.get() : "";
		unpack(jsonMap);
		synchronize();
		String postSynchronizedJsonMap = toString();
		if (!postSynchronizedJsonMap.equals(jsonMap))
			offload(postSynchronizedJsonMap);
	}

	// convert the json to the current map
	private void unpack(String jsonMap) {
		if (Strings.resemblesNull(jsonMap))
			return;
		try {
			JSONParser parser = new JSONParser();
			for (Object ch : (JSONArray) parser.parse(jsonMap)) {
				JSONObject jsonChannel = (JSONObject) ch;
				String channel = (String) jsonChannel.get("channel");
				HashMap<String, HashMap<String, String>> messageMap = new HashMap<>();
				for (Object msg : (JSONArray) jsonChannel.get("messages")) {
					JSONObject jsonMessage = (JSONObject) msg;
					String message = (String) jsonMessage.get("message");
					HashMap<String, String> reactionMap = new HashMap<>();
					for (Object re : (JSONArray) jsonMessage.get("reactions")) {
						JSONObject jsonReaction = (JSONObject) re;
						String reaction = (String) jsonReaction.get("reaction");
						String role = (String) jsonReaction.get("role");
						reactionMap.put(reaction, role);
					}
					messageMap.put(message, reactionMap);
				}
				map.put(channel, messageMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// synchronize current map with whats in the server
	private void synchronize() {
		Guild g = GateKeeper.getInstance().getManager().getGuildById(guild);
		HashMap<String, HashMap<String, HashMap<String, String>>> channelMap = new HashMap<>(map);
		for (String channel : channelMap.keySet()) {
			TextChannel ch = g.getTextChannelById(channel);
			if (ch != null) {
				for (String message : channelMap.get(channel).keySet()) {
					Message msg = null;
					try {
						msg = ch.retrieveMessageById(message).complete();
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (msg != null) {
						for (String reaction : channelMap.get(channel).get(message).keySet()) {
							ReactionEmote re = ReactionUtil.getReaction(msg, reaction);
							if (re == null
									|| g.getRoleById(channelMap.get(channel).get(message).get(reaction)) == null) {
								/*
								 * As we are looping through a duplicate map anyways, AND the #set method is
								 * looping through duplicate maps, it's faster if we just remove the role
								 * straight from our cached map here. Additionally, the roles are cached in JDA,
								 * so there wont be any connections or rate limiters to worry about, and will be
								 * just as fast.
								 */
								map.get(channel).get(message).remove(reaction);
							}
						}
					} else {
						map.get(channel).remove(message);
					}
				}
			} else {
				map.remove(channel);
			}
		}
		cleanEmptyMaps();
	}

	// apply roles from current map
	private void applyRoles() {
		Guild g = GateKeeper.getInstance().getManager().getGuildById(guild);
		for (String channel : map.keySet()) {
			for (String message : map.get(channel).keySet()) {
				g.getTextChannelById(channel).retrieveMessageById(message).queue(msg -> {
					for (MessageReaction reaction : msg.getReactions()) {
						String re = ReactionUtil.getId(reaction.getReactionEmote());
						Role r = g.getRoleById(map.get(channel).get(message).get(re));
						reaction.retrieveUsers().queue(users -> users.forEach(user -> {
							if (!g.getSelfMember().getUser().getId().equals(user.getId())) {
								g.addRoleToMember(user.getId(), r).queue();
							}
						}));
					}
				});
			}
		}
	}

	// offload map to database
	void shutdown() {
		offload();
	}

	// offload map converted to json to the database
	public void offload() {
		offload(toString());
	}

	// offload json as the current map to the database
	public void offload(String jsonMap) {
		GuildBackend backend = Backend.get(guild);
		backend.dropEntry("reactions");
		backend.addEntry("reactions", jsonMap);
	}

	// convert map to json
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONArray channels = new JSONArray();
		for (String channel : map.keySet()) {
			HashMap<String, HashMap<String, String>> messageMap = map.get(channel);
			JSONObject jsonChannel = new JSONObject();
			jsonChannel.put("channel", channel);
			JSONArray channelMessages = new JSONArray();
			for (String message : messageMap.keySet()) {
				HashMap<String, String> reactionMap = messageMap.get(message);
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("message", message);
				JSONArray messageReactions = new JSONArray();
				for (String reaction : reactionMap.keySet()) {
					String role = reactionMap.get(reaction);
					JSONObject jsonReaction = new JSONObject();
					jsonReaction.put("reaction", reaction);
					jsonReaction.put("role", role);
					messageReactions.add(jsonReaction);
				}
				jsonMessage.put("reactions", messageReactions);
				channelMessages.add(jsonMessage);
			}
			jsonChannel.put("messages", channelMessages);
			channels.add(jsonChannel);
		}

		return channels.toJSONString();
	}

	public void listenForNewReaction(Action action, String currentChannel, String currentMessage, String creator, String taggedChannel,
			String messageId, String taggedRole) {
		if (reactionTask != null)
			reactionTask.end(false);
		reactionTask = new NewReactionTask(guild, action, currentChannel, currentMessage, creator, taggedChannel, messageId,
				taggedRole);
		Thread reactionTaskThread = new Thread(reactionTask);
		reactionTask.setThread(reactionTaskThread);
		reactionTaskThread.start();
	}

	// not functioning
	public void cleanEmptyMaps() {
		for (String channel : new HashSet<>(getChannels())) {
			for (String message : new HashSet<>(getMessages(channel))) {
				if (map.get(channel).get(message).isEmpty()) {
					map.get(channel).remove(message);
				}
			}
			if (map.get(channel).isEmpty()) {
				map.remove(channel);
			}
		}
	}

	public String getRole(String channel, String message, String reaction) {
		try {
			return map.get(channel).get(message).get(reaction);
		} catch (Exception e) {
		}
		return null;
	}

	public Set<String> getReactions(String channel, String message) {
		try {
			return map.get(channel).get(message).keySet();
		} catch (Exception e) {
		}
		return null;
	}

	public Set<String> getMessages(String channel) {
		try {
			return map.get(channel).keySet();
		} catch (Exception e) {
		}
		return null;
	}

	public Set<String> getChannels() {
		try {
			return map.keySet();
		} catch (Exception e) {
		}
		return null;
	}

	public void remove(String idToRemove, Class<?> classTypeToRemove) {
		if (classTypeToRemove.isAssignableFrom(TextChannel.class)) {
			map.remove(idToRemove);
		} else if (classTypeToRemove.isAssignableFrom(Role.class)) {
			HashMap<String, HashMap<String, HashMap<String, String>>> channelMap = new HashMap<>(map);
			for (String ch : channelMap.keySet()) {
				HashMap<String, HashMap<String, String>> messageMap = new HashMap<>(map.get(ch));
				for (String msg : messageMap.keySet()) {
					HashMap<String, String> reactionMap = new HashMap<>(map.get(ch).get(msg));
					for (String re : reactionMap.keySet()) {
						if (reactionMap.get(re).contains(idToRemove)) {
							map.get(ch).get(msg).remove(re);
						}
					}
				}
			}
		} else {
			return;
		}
		cleanEmptyMaps();
		offload();
	}

	public void removeMessage(String channel, String message) {
		if (map.get(channel) != null)
			map.get(channel).remove(message);
		cleanEmptyMaps();
		offload();
	}

	public void removeReaction(String channel, String message, String reaction) {
		if (map.get(channel) != null)
			if (map.get(channel).get(message) != null)
				map.get(channel).get(message).remove(reaction);
		cleanEmptyMaps();
		offload();
	}

	public void set(String channel, String message, String reaction, String role) {
		HashMap<String, HashMap<String, String>> messageMap = map.get(channel);
		if (messageMap == null)
			messageMap = new HashMap<>();
		HashMap<String, String> reactionMap = messageMap.get(message);
		if (reactionMap == null)
			reactionMap = new HashMap<>();
		reactionMap.put(reaction, role);
		messageMap.put(message, reactionMap);
		map.put(channel, messageMap);
		cleanEmptyMaps();
		offload();
	}

}
