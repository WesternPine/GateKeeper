package dev.westernpine.gatekeeper.object;

public enum Action {
	
	ADD(),
	REMOVE(),
	;
	
	public static Action of(String string) {
		try {
			return Action.valueOf(string.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}

}
