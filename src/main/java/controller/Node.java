package controller;

import java.net.InetAddress;
import java.util.UUID;

public class Node implements Comparable<Node>{
	
	public static long TimeoutPeriod;
	
	private InetAddress address;
	private int port;
	private String allowedOperators;
	private int usage;
	private long lastAliveTimestamp;
	private UUID uuid; // Used as a hack to circumvent the limit on ConcurrentSkipListSets
	
	public Node(InetAddress address, int port) {
		this.address = address;
		this.port = port;
		usage = 0;
		/* Assign a random UUID so that nodes with the same usage count 
		 * can still be within the ConcurrentSkipListSet at the same time
		 */
		uuid = UUID.randomUUID();  
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
	private UUID getUUID() {
		return uuid;
	}
	public boolean isOnline() {
		return System.currentTimeMillis() - lastAliveTimestamp < TimeoutPeriod;
	}
	
	public String getNetworkID() {
		return createNetworkID(address, port);
	}
	public static String createNetworkID(InetAddress address, int port) {
		return String.format("%s:%d", address.getHostAddress(), port);
	}
	@Override
	public int compareTo(Node other) {
		if(getUsage() < other.getUsage())
			return -1;
		else if(getUsage() > other.getUsage())
			return 1;
		else {
			return uuid.compareTo(other.getUUID()); // Avoid returning 0 by comparing the random UUIDs
		}
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
	
	private String isOnlineString() {
		if(isOnline())
			return "online";
		else
			return "offline";
	}
}
