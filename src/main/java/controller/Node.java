package controller;

import java.net.InetAddress;

public class Node {
	private InetAddress address;
	private int port;
	private String allowedOperators;
	private int usage;
	private long lastAliveMessage;
	
	public Node(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		usage = 0;
	}
	public String getAllowedOperators() {
		return allowedOperators;
	}
	public void setAllowedOperators(String allowedOperators) {
		this.allowedOperators = allowedOperators;
	}
	public int getUsage() {
		return usage;
	}
	public void setUsage(int usage) {
		this.usage = usage;
	}
	public long getLastAliveMessage() {
		return lastAliveMessage;
	}
	public void setLastAliveMessage(long lastAliveMessage) {
		this.lastAliveMessage = lastAliveMessage;
	}
	public InetAddress getIPAddress() {
		return address;
	}
	public int getTCPPort() {
		return port;
	}
	public String getUniqueID() {
		return createUniqueID(address, port);
	}
	public boolean wasActiveIn(long timeoutPeriod) {
		return System.currentTimeMillis() - timeoutPeriod > lastAliveMessage; // TODO: Umbenennen isOffline
	}
	
	public static String createUniqueID(InetAddress address, int port) {
		return String.format("%s:%d", address.getHostAddress(), port);
	}
	
	// TODO: Equals, Comparator
}
