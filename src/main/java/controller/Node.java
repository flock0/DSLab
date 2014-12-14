package controller;

import java.net.InetAddress;

/**
 * A node that can be used for computation
 */
public class Node implements Comparable<Node>{
	
	public static long TimeoutPeriod;
	
	private InetAddress address;
	private int port;
	private String allowedOperators;
	private int usage;
	private long lastAliveTimestamp;
	
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
		return lastAliveTimestamp;
	}
	public void setLastAliveMessage(long lastAliveMessage) {
		this.lastAliveTimestamp = lastAliveMessage;
	}
	public InetAddress getIPAddress() {
		return address;
	}
	public int getTCPPort() {
		return port;
	}
	public boolean isOnline() {
		return System.currentTimeMillis() - lastAliveTimestamp < TimeoutPeriod;
	}
	
	private String isOnlineString() {
		if(isOnline())
			return "online";
		else
			return "offline";
	}
	public String getNetworkID() {
		return createNetworkID(address, port);
	}
	public static String createNetworkID(InetAddress address, int port) {
		return String.format("%s:%d", address.getHostAddress(), port);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("IP: ");
		builder.append(address.getHostAddress());
		builder.append(" Port: ");
		builder.append(port);
		builder.append(" ");
		builder.append(isOnlineString());
		builder.append(" Usage: ");
		builder.append(usage);
		builder.append('\n');
		return builder.toString();
	}
	@Override
	public boolean equals(Object other) {
		if(other != null && other instanceof Node)
			return getNetworkID().equals(((Node) other).getNetworkID());
		return false;
	}
	@Override
	public int hashCode() {
		return getNetworkID().hashCode();
	}
	@Override
	public int compareTo(Node other) {
		if(getUsage() < other.getUsage())
			return -1;
		else if(getUsage() > other.getUsage())
			return 1;
		else {
			return getNetworkID().compareTo(other.getNetworkID()); // Avoid returning 0 by comparing the NetworkIDs
		}
	}
}
