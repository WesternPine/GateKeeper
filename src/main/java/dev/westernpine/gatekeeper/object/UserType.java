package dev.westernpine.gatekeeper.object;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public enum UserType {
	
	BOT(true),
	CLIENT(false);
	
	private boolean bot;
	
	UserType(boolean bot) {
		this.bot = bot;
	}
	
	public boolean isBot() {
		return bot;
	}
	
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
	
	public static UserType of(String string) {
		return UserType.valueOf(string.toUpperCase());
	}
	
	public static UserType of(Member member) {
		return of(member.getUser());
	}
	
	public static UserType of(User user) {
		return fromIsBot(user.isBot());
	}
	
	public static UserType fromIsBot(boolean isBot) {
		for(UserType type : UserType.values()) {
			if(type.isBot() == isBot) {
				return type;
			}
		}
		return UserType.CLIENT;
	}

}
