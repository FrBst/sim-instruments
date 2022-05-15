package model;

import lombok.ToString;

public enum ActivityType {
	HOME("home"),
	WORK("work"),
	LEISURE("leisure"),
	SHOP("shop"),
	STUDY_ADULT("study"),
	STUDY_CHILD("study");
	
	private String string;
	
	private ActivityType(String name) {
		string = name;
	}
	
	@Override
	public String toString() {
		return string;
	}
}
