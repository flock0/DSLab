package controller;

import util.Config;
import util.FixedParameters;

public class User {
	
	private String username;
	private int credits;
	private String password;
	private boolean isOnline;
	
	public User(String username, Config config) {
		this.username = username;
		credits = config.getInt(username + ".credits");
		password = config.getString(username + ".password");
		isOnline = false;
	}
	public int getCredits() {
		return credits;
	}
	public void setCredits(int credits) {
		this.credits = credits;
	}
	public boolean isOnline() {
		return isOnline;
	}
	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
	public String getUsername() {
		return username;
	}
	public boolean isCorrectPassword(String passwordToCheck) {
		return password.equals(passwordToCheck);
	}
	public boolean hasEnoughCredits(ClientRequest request) {
		return credits > request.getOperators().length * FixedParameters.CREDIT_COST_PER_OPERATOR;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(username);
		builder.append(" ");
		builder.append(isOnlineString());
		builder.append(" Credits: ");
		builder.append(credits);
		builder.append('\n');
		return builder.toString();
	}
	
	private String isOnlineString() {
		if(isOnline())
			return "online";
		else
			return "offline";
	}
}
