package dev.westernpine.gatekeeper.object;

import lombok.Getter;

public enum Action {
	
	ADD("add"),
	REMOVE("remove"),
	;
	
	@Getter
	private String jsonLabel;
	
	Action(String jsonLabel) {
		this.jsonLabel = jsonLabel;
	}
	
	public static Action of(String string) {
		try {
			return Action.valueOf(string.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Action fromJsonLabel(String label) {
		try {
			for(Action action : Action.values()) {
				if(action.getJsonLabel().contentEquals(label)) {
					return action;
				}
			}
		} catch (Exception e) {}
		return null;
	}
	
	public Action getOpposite() {
		if(this == ADD)
			return REMOVE;
		else
			return ADD;
	}

}
