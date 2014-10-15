package controller;

import util.Config;

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
	public String getPassword() {
		return password;
	}
}
