package dev.westernpine.gatekeeper.management;

import java.util.Collection;
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
import dev.westernpine.gatekeeper.util.RoleUtils;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
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

	//              channel         message        reaction        action   role
	private HashMap<String, HashMap<String, HashMap<String, HashMap<Action, String>>>> map;

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
				HashMap<String, HashMap<String, HashMap<Action, String>>> messageMap = new HashMap<>();
				for (Object msg : (JSONArray) jsonChannel.get("messages")) {
					JSONObject jsonMessage = (JSONObject) msg;
					String message = (String) jsonMessage.get("message");
					HashMap<String, HashMap<Action, String>> reactionMap = new HashMap<>();
					for (Object re : (JSONArray) jsonMessage.get("reactions")) {
						JSONObject jsonReaction = (JSONObject) re;
						String reaction = (String) jsonReaction.get("reaction");
						
						HashMap<Action, String> actionMap = new HashMap<>();
						for(Action action : Action.values()) {
							String roleString = (String) jsonReaction.get(action.getJsonLabel());
							actionMap.put(action, roleString);
						}
						reactionMap.put(reaction, actionMap);
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
		HashMap<String, HashMap<String, HashMap<String, HashMap<Action, String>>>> channelMap = new HashMap<>(map);
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
							if (re == null) {
								/*
								 * As we are looping through a duplicate map anyways, AND the #set method is
								 * looping through duplicate maps, it's faster if we just remove the role
								 * straight from our cached map here. Additionally, the roles are cached in JDA,
								 * so there wont be any connections or rate limiters to worry about, and will be
								 * just as fast.
								 */
								map.get(channel).get(message).remove(reaction);
								continue;
							}
							
							/*
							 * Does not need to be updated after every change, as changes take place
							 * outside of loop, and any value-setting applies for the whole action, and not
							 * a single role value of the string.
							 */
							for(Action action : new HashSet<>(channelMap.get(channel).get(message).get(reaction).keySet())) {
								Set<String> filteredMissingRoles = RoleUtils.filterMissingRoles(guild, map.get(channel).get(message).get(reaction).get(action));
								if(filteredMissingRoles.isEmpty()) {
									map.get(channel).get(message).get(reaction).remove(action);
								} else {
									map.get(channel).get(message).get(reaction).put(action, RoleUtils.toRoleString(filteredMissingRoles));
								}
							}
							
							if(map.get(channel).get(message).get(reaction).isEmpty()) {
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
				Message msg = g.getTextChannelById(channel).retrieveMessageById(message).complete();
				for (MessageReaction reaction : msg.getReactions()) {
					String re = ReactionUtil.getId(reaction.getReactionEmote());
					Set<Member> reactors = new HashSet<>();
					reaction.retrieveUsers().complete().forEach(user -> reactors.add(g.getMember(user)));
					map.get(channel).get(message).get(re).keySet().forEach(action -> 
						RoleUtils.applyRoleString(map.get(channel).get(message).get(re).get(action), action, reactors.toArray(new Member[reactors.size()])));
				}
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
			HashMap<String, HashMap<String, HashMap<Action, String>>> messageMap = map.get(channel);
			JSONObject jsonChannel = new JSONObject();
			jsonChannel.put("channel", channel);
			JSONArray channelMessages = new JSONArray();
			for (String message : messageMap.keySet()) {
				HashMap<String, HashMap<Action, String>> reactionMap = messageMap.get(message);
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("message", message);
				JSONArray messageReactions = new JSONArray();
				for(String reaction : reactionMap.keySet()) {
					HashMap<Action, String> actionMap = reactionMap.get(reaction);
					JSONObject jsonReaction = new JSONObject();
					jsonReaction.put("reaction", reaction);
					for(Action action : Action.values()) {
						try {
							jsonReaction.put(action.getJsonLabel(), actionMap.get(action));
						} catch (Exception e) {
							jsonReaction.put(action.getJsonLabel(), "");
						}
					}
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
			String messageId, Collection<String> taggedRoles) {
		if (reactionTask != null)
			reactionTask.end(false);
		reactionTask = new NewReactionTask(guild, action, currentChannel, currentMessage, creator, taggedChannel, messageId,
				taggedRoles);
		Thread reactionTaskThread = new Thread(reactionTask);
		reactionTask.setThread(reactionTaskThread);
		reactionTaskThread.start();
	}

	// not functioning
	public void cleanEmptyMaps() {
		for (String channel : getChannels()) {
			for (String message : getMessages(channel)) {
				for(String reaction : getReactions(channel, message)) {
					for(Action action : getActions(channel, message, reaction)) {
						Set<String> roles = RoleUtils.filterMissingRoles(guild, getRoleString(channel, message, reaction, action));
						map.get(channel).get(message).get(reaction).put(action, RoleUtils.toRoleString(roles));
						if(roles.isEmpty()) {
							map.get(channel).get(message).get(reaction).remove(action);
						}
					}
					if(map.get(channel).get(message).get(reaction).isEmpty()) {
						map.get(channel).get(message).remove(reaction);
					}
				}
				if (map.get(channel).get(message).isEmpty()) {
					map.get(channel).remove(message);
				}
			}
			if (map.get(channel).isEmpty()) {
				map.remove(channel);
			}
		}
	}

	public String getRoleString(String channel, String message, String reaction, Action action) {
		try {
			return map.get(channel).get(message).get(reaction).get(action);
		} catch (Exception e) {
		}
		return null;
	}
	
	public Set<Action> getActions(String channel, String message, String reaction) {
		try {
			return new HashSet<>(map.get(channel).get(message).get(reaction).keySet());
		} catch(Exception e) {
		}
		return null;
	}

	public Set<String> getReactions(String channel, String message) {
		try {
			return new HashSet<>(map.get(channel).get(message).keySet());
		} catch (Exception e) {
		}
		return null;
	}

	public Set<String> getMessages(String channel) {
		try {
			return new HashSet<>(map.get(channel).keySet());
		} catch (Exception e) {
		}
		return null;
	}

	public Set<String> getChannels() {
		try {
			return new HashSet<>(map.keySet());
		} catch (Exception e) {
		}
		return null;
	}

	public void remove(String idToRemove, Class<?> classTypeToRemove) {
		if (classTypeToRemove.isAssignableFrom(TextChannel.class)) {
			map.remove(idToRemove);
		} else if (classTypeToRemove.isAssignableFrom(Role.class)) {
			HashMap<String, HashMap<String, HashMap<String, HashMap<Action, String>>>> channelMap = new HashMap<>(map);
			for (String ch : channelMap.keySet()) {
				HashMap<String, HashMap<String, HashMap<Action, String>>> messageMap = new HashMap<>(map.get(ch));
				for (String msg : messageMap.keySet()) {
					HashMap<String, HashMap<Action, String>> reactionMap = new HashMap<>(map.get(ch).get(msg));
					for (String re : reactionMap.keySet()) {
						HashMap<Action, String> actionMap = new HashMap<>(map.get(ch).get(msg).get(re));
						for(Action a : actionMap.keySet()) {
							Set<String> roles = RoleUtils.toRoleSet(actionMap.get(a));
							if(roles.contains(idToRemove)) {
								roles.remove(idToRemove);
								map.get(ch).get(msg).get(re).put(a, RoleUtils.toRoleString(roles));
							}
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

	public void set(String channel, String message, String reaction, Action action, String roleString) {
		HashMap<String, HashMap<String, HashMap<Action, String>>> messageMap = map.get(channel);
		if (messageMap == null)
			messageMap = new HashMap<>();
		HashMap<String, HashMap<Action, String>> reactionMap = messageMap.get(message);
		if (reactionMap == null)
			reactionMap = new HashMap<>();
		HashMap<Action, String> actionMap = reactionMap.get(reaction);
		if(actionMap == null)
			actionMap = new HashMap<>();
		actionMap.put(action, roleString);
		reactionMap.put(reaction, actionMap);
		messageMap.put(message, reactionMap);
		map.put(channel, messageMap);
		cleanEmptyMaps();
		offload();
	}

}
