package controller;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import util.Config;

public class NodePurgeTask extends TimerTask {

	private Config config;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private long timeoutPeriod;

	public NodePurgeTask(ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes, Config config) {
		this.config = config;
		this.activeNodes = activeNodes;
		this.timeoutPeriod = config.getInt("node.timeout");
	}
	
	@Override
	public void run() {
		purgeInactiveNodes();
	}

	private void purgeInactiveNodes() {
		for(ConcurrentSkipListSet<Node> singleOperatorList : activeNodes.values())
			for(Node node : singleOperatorList)
				if(!node.isOnline())
					synchronized(node) {
						removeFromActiveNodes(node);
					}		
	}

	private void removeFromActiveNodes(Node node) {
		for(Character operator : activeNodes.keySet()) {
			ConcurrentSkipListSet<Node> set = activeNodes.get(operator);
			set.remove(node);
			if(set.isEmpty())
				removeFromAllowedOperators(operator);
		}
	}

	private void removeFromAllowedOperators(Character operator) {
		config.setProperty("allowedOperators", config.getString("allowedOperators").replace(operator.toString(), ""));
	}

}
