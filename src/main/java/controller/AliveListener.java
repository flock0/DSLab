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

	private Config config;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private ConcurrentHashMap<String, Node> allNodes;
	private DatagramSocket datagramSocket = null;
	private DatagramPacket packet = null;
	private long lastAliveTimestamp;
	private String[] splitMessage = null;
	private long timeoutPeriod;

	public AliveListener(
			ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes,
			ConcurrentHashMap<String, Node> allNodes, Config config) {
		this.activeNodes = activeNodes;
		this.allNodes = allNodes;
		this.config = config;
		
		timeoutPeriod = config.getInt("node.timeout");
		openUDPSocket();
	}

	private void openUDPSocket() {
		try {
			datagramSocket = new DatagramSocket(config.getInt("udp.port"));
		} catch (SocketException e) {
			System.out.println("Couldn't create UDP socket: " + e.getMessage());
		}
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
		return splitMessage.length == 3 && splitMessage[0].equals("!alive")
				&& isInteger(splitMessage[1]) && !splitMessage[2].isEmpty();
	}

	private void updateActiveNodes() {
		String uniqueNodeID = Node.createNetworkID(packet.getAddress(),
				Integer.parseInt(splitMessage[1]));

		if (haveSeenNode(uniqueNodeID)) {
			 refreshNode(uniqueNodeID);
		} else {
			addToNodesAndSetActive(uniqueNodeID);
		}
	}

	private boolean haveSeenNode(String uniqueNodeID) {
		return allNodes.containsKey(uniqueNodeID);
	}

	private void refreshNode(String uniqueNodeID) {
		Node node = allNodes.get(uniqueNodeID);
		
		if(!node.isOnline()) {
			
			node.setLastAliveMessage(lastAliveTimestamp);
			addToActiveNodes(node);
			
		} else {
			node.setLastAliveMessage(lastAliveTimestamp);
			
			if(operatorsChanged(node.getAllowedOperators(), splitMessage[2])) {
				synchronized(node) {
					node.setAllowedOperators(splitMessage[2]);
					removeFromActiveNodes(node);
					addToActiveNodes(node);
				}
			}
		}
		
	}

	private boolean operatorsChanged(String oldOperators, String newOperators) {
		char[] oldArray = oldOperators.toCharArray();
		char[] newArray = newOperators.toCharArray();
		Arrays.sort(oldArray);
		Arrays.sort(newArray);
		
		return !Arrays.equals(oldArray, newArray);
	}
	
	private void removeFromActiveNodes(Node node) {
		for(ConcurrentSkipListSet<Node> singleOperatorList : activeNodes.values()) {
			singleOperatorList.remove(node);
		}
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
		String nodeOperators = node.getAllowedOperators();
		updateAllowedOperators(nodeOperators);
		for (char operator : nodeOperators.toCharArray()) {
			if (activeNodes.containsKey(operator)) {
				activeNodes.get(operator).add(node);
			} else {
				activeNodes.put(operator, new ConcurrentSkipListSet<Node>());
				activeNodes.get(operator).add(node);
			}
		}

	}

	private void updateAllowedOperators(String nodeOperators) {
		config.setProperty("allowedProperties", removeDuplicates(config.getString("allowedProperties") + nodeOperators));
	}

	static String removeDuplicates(String s) {
	    StringBuilder noDupes = new StringBuilder();
	    for (int i = 0; i < s.length(); i++) {
	        String si = s.substring(i, i + 1);
	        if (noDupes.indexOf(si) == -1) {
	            noDupes.append(si);
	        }
	    }
	    return noDupes.toString();
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
