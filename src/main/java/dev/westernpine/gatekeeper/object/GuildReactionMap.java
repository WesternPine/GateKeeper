package dev.westernpine.gatekeeper.object;

import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import lombok.Getter;
import proj.api.marble.lib.string.Strings;

public class GuildReactionMap {
	
	@Getter
	private String guild;
	
	@Getter
	private HashMap<String, HashMap<String, HashMap<String, String>>> map = new HashMap<>();
	
	public GuildReactionMap(String guild, String jsonString) {
		this.guild = guild;
		if(Strings.resemblesNull(jsonString))
			return;
		JSONParser parser = new JSONParser();
		try {
			for(Object ch : (JSONArray)parser.parse(jsonString)) {
				JSONObject jsonChannel = (JSONObject)ch;
				String channel = (String) jsonChannel.get("channel");
				HashMap<String, HashMap<String, String>> messageMap = new HashMap<>();
				for(Object msg : ((JSONArray)jsonChannel.get("messages"))) {
					JSONObject jsonMessage = (JSONObject)msg;
					String message = (String) jsonMessage.get("message");
					HashMap<String, String> reactionMap = new HashMap<>();
					for (Object re : ((JSONArray)jsonChannel.get("reactions"))) {
						JSONObject jsonReaction = (JSONObject)re;
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
	
	public boolean reactionExistsForMessage(String channel, String message, String reaction) {
		HashMap<String, HashMap<String, String>> messageMap = map.get(channel);
		if(messageMap != null) {
			HashMap<String, String> reactionMap = messageMap.get(message);
			if(reactionMap != null) {
				return reactionMap.get(reaction) != null;
			}
		}
		return false;
	}
	
	public boolean roleExistsForMessage(String channel, String message, String role) {
		HashMap<String, HashMap<String, String>> messageMap = map.get(channel);
		if(messageMap != null) {
			HashMap<String, String> reactionMap = messageMap.get(message);
			if(reactionMap != null) {
				for(String reaction : reactionMap.keySet()) {
					if(reactionMap.get(reaction).equals(role)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void modify(String channel, String message, String reaction, String role) {
		HashMap<String, HashMap<String, String>> messageMap = map.get(channel);
		if(messageMap == null)
			messageMap = new HashMap<>();
		HashMap<String, String> reactionMap = messageMap.get(message);
		if(reactionMap == null)
			reactionMap = new HashMap<>();
			reactionMap.put(reaction, role);
		messageMap.put(message, reactionMap);
		map.put(channel, messageMap);
	}
	
	public void remove(String channel) {
		map.remove(channel);
		cleanup();
	}
	
	public void remove(String channel, String message) {
		HashMap<String, HashMap<String, String>> messageMap = map.get(channel);
		if(messageMap != null)
			messageMap.remove(message);
		cleanup();
	}
	
	public void remove(String channel, String message, String reaction) {
		HashMap<String, HashMap<String, String>> messageMap = map.get(channel);
		if(messageMap != null) {
			HashMap<String, String> reactionMap = messageMap.get(message);
			if(reactionMap != null)
				reactionMap.remove(reaction);
		}
		cleanup();
	}
	
	public void removeRole(String role) {
		HashMap<String, HashMap<String, HashMap<String, String>>> channelMap = new HashMap<>(map);
		for(String channel : channelMap.keySet()) {
			HashMap<String, HashMap<String, String>> messageMap = new HashMap<>(map.get(channel));
			for(String message : messageMap.keySet()) {
				HashMap<String, String> reactionMap = new HashMap<>(map.get(channel).get(message));
				for(String reaction : reactionMap.keySet()) {
					if(reactionMap.get(reaction).contains(role)) {
						map.get(channel).get(message).remove(reaction);
					}
				}
			}
		}
		cleanup();
	}
	
	
	
	public void cleanup() {
		HashMap<String, HashMap<String, HashMap<String, String>>> channelMap = new HashMap<>(map);
		for(String channel : channelMap.keySet()) {
			HashMap<String, HashMap<String, String>> messageMap = new HashMap<>(map.get(channel));
			for(String message : messageMap.keySet()) {
				HashMap<String, String> reactionMap = new HashMap<>(map.get(channel).get(message));
				for(String reaction : reactionMap.keySet()) {
					if(reactionMap.get(reaction).isEmpty()) {
						map.get(channel).remove(message);
					}
				}
				if(map.get(channel).isEmpty()) {
					map.remove(channel);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		cleanup();
		JSONArray jsonChannelMap = new JSONArray();
		for(String channel : map.keySet()) {
			JSONObject jsonChannel = new JSONObject();
			jsonChannel.put("channel", channel);
			JSONArray jsonMessageMap = new JSONArray();
			for(String message : map.get(channel).keySet()) {
				JSONObject jsonMessage = new JSONObject();
				jsonMessage.put("message", message);
				JSONArray jsonReactionMap = new JSONArray();
				for(String reaction : map.get(channel).get(message).keySet()) {
					JSONObject reactionObject = new JSONObject();
					reactionObject.put("reaction", reaction);
					reactionObject.put("role", map.get(channel).get(message).get(reaction));
					jsonReactionMap.add(reactionObject);
				}
				jsonMessage.put("reactions", jsonReactionMap);
				jsonMessageMap.add(jsonMessage);
			}
			jsonChannel.put("messages", jsonMessageMap);
			jsonChannelMap.add(jsonChannel);
		}
		return jsonChannelMap.toJSONString();
	}

}
