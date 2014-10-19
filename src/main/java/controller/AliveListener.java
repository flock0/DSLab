package controller;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import util.Config;
import util.TerminableThread;

public class AliveListener extends TerminableThread {

	private final Config config;
	private final ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private final ConcurrentHashMap<String, Node> allNodes;
	private DatagramSocket datagramSocket = null;;
	private DatagramPacket packet = null;
	private long lastAliveTimestamp;
	private String[] splitMessage = null;
	private final long timeoutPeriod;

	public AliveListener(
			ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes,
			ConcurrentHashMap<String, Node> allNodes, Config config) throws SocketException {
		this.activeNodes = activeNodes;
		this.allNodes = allNodes;
		this.config = config;
		
		timeoutPeriod = config.getInt("node.timeout");
		openUDPSocket();
	}

	private void openUDPSocket() throws SocketException {
			datagramSocket = new DatagramSocket(config.getInt("udp.port"));
	}

	@Override
	public void run() {
		if (datagramSocket != null) {
			try {
				while (true) {
					packet = receiveMessage();

					String aliveMessage = new String(packet.getData());
					splitMessage = aliveMessage.trim().split("\\s");

					if (messageIsValid())
						updateActiveNodes();
				}
			} catch (SocketException e) {
				System.out.println("AliveSocket shutdown: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IOException occured: " + e.getMessage());
			}
		}

	}

	private DatagramPacket receiveMessage() throws IOException {
		byte[] buffer = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

		datagramSocket.receive(packet);
		lastAliveTimestamp = System.currentTimeMillis();

		return packet;
	}

	private boolean messageIsValid() {
		// "!alive <portnumber> <allowed_operators>"
		return splitMessage.length == 3 
				&& splitMessage[0].equals("!alive")
				&& isInteger(splitMessage[1]) 
				&& !splitMessage[2].isEmpty();
	}

	private boolean haveSeenNode(String uniqueNodeID) {
		return allNodes.containsKey(uniqueNodeID);
	}

	private void refreshNode(String uniqueNodeID) {
		Node node = allNodes.get(uniqueNodeID);
		
		if(!node.isOnline()) {
			
			node.setLastAliveMessage(lastAliveTimestamp);
			synchronized(activeNodes) {
				addToActiveNodes(node);
			}
		} else {
			node.setLastAliveMessage(lastAliveTimestamp);
			
			String newOperators = splitMessage[2];
			if(operatorsChanged(node.getAllowedOperators(), newOperators)) {
				synchronized(node) {
					synchronized(activeNodes) {
						updateActiveNodes(node, newOperators);
						node.setAllowedOperators(newOperators);
					}
				}
			}
		}
		
	}

	private static boolean operatorsChanged(String oldOperators, String newOperators) {
		char[] oldArray = oldOperators.toCharArray();
		char[] newArray = newOperators.toCharArray();
		Arrays.sort(oldArray);
		Arrays.sort(newArray);
		
		return !Arrays.equals(oldArray, newArray);
	}

	private void updateActiveNodes() {
		String uniqueNodeID = Node.createNetworkID(packet.getAddress(),
				Integer.parseInt(splitMessage[1]));
	
		if (haveSeenNode(uniqueNodeID))
			refreshNode(uniqueNodeID);
		else
			addToNodesAndSetActive(uniqueNodeID);
	}

	private void addToNodesAndSetActive(String uniqueNodeID) {
		Node node = new Node(packet.getAddress(),
				Integer.parseInt(splitMessage[1]));
		node.setAllowedOperators(splitMessage[2]);
		node.setLastAliveMessage(lastAliveTimestamp);
		
		allNodes.put(node.getNetworkID(), node);
		addToActiveNodes(node);
	}

	private void addToActiveNodes(Node node) {
		for (Character operator : node.getAllowedOperators().toCharArray()) {
			// If a respective set already exists, add it. Otherwise create a new list
			if (activeNodes.containsKey(operator)) {
				ConcurrentSkipListSet<Node> set = activeNodes.get(operator);
				synchronized(set) {
					set.add(node);
				}
			} else {
				ConcurrentSkipListSet<Node> set = new ConcurrentSkipListSet<Node>();
				synchronized(set) {
					activeNodes.put(operator, set);
					set.add(node);
				}
			}
		}

	}

	private void updateActiveNodes(Node node, String newOperators) {
		// Remove node from unavailable operators lists
		for(char operator : node.getAllowedOperators().toCharArray()) {
			if(newOperators.indexOf(operator) == -1)
				synchronized(activeNodes.get(operator)) {
					activeNodes.get(operator).remove(node);
				}
		}
		// Add node to now available operators lists
		for (char operator : newOperators.toCharArray()) {
			if(node.getAllowedOperators().indexOf(operator) == -1)
				synchronized(activeNodes.get(operator)) {
					activeNodes.get(operator).add(node);
				}
		}
	}

	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public void shutdown() {
		datagramSocket.close();
	}
}
