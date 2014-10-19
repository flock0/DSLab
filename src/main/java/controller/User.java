package controller;

import util.Config;
import util.FixedParameters;

/**
 * Contains a single user loaded from the config files
 */
public class User {
	
	private String username;
	private int credits;
	private String password;
	private int concurrentOnlineCounter;
	
	public User(String username, Config config) {
		this.username = username;
		credits = config.getInt(username + ".credits");
		password = config.getString(username + ".password");
		concurrentOnlineCounter = 0;
	}
	
	public int getCredits() {
		return credits;
	}
	public void setCredits(int credits) {
		this.credits = credits;
	}
	public String getUsername() {
		return username;
	}

	public boolean isOnline() {
		return concurrentOnlineCounter > 0;
	}
	private String isOnlineAsString() {
		if(isOnline())
			return "online";
		return "offline";
	}

	public void increaseOnlineCounter() {
		concurrentOnlineCounter++;
	}
	public void decreaseOnlineCounter() {
		if(concurrentOnlineCounter > 0)
			concurrentOnlineCounter--;
	}
	public boolean isCorrectPassword(String passwordToCheck) {
		return password.equals(passwordToCheck);
	}
	public boolean hasEnoughCredits(ClientRequest request) {
		return credits >= (request.getOperators().length * FixedParameters.CREDIT_COST_PER_OPERATOR);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(username);
		builder.append(" ");
		builder.append(isOnlineAsString());
		builder.append(" Credits: ");
		builder.append(credits);
		builder.append('\n');
		return builder.toString();
	}
}
