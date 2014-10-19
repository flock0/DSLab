package controller;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import util.Config;

/**
 * Routinely purge nodes that appear to be offline
 */
public class NodePurgeTask extends TimerTask {

	private Config config;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private final long timeoutPeriod;

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
						synchronized(activeNodes) {
							removeFromActiveNodes(node);
						}
					}		
	}

	private void removeFromActiveNodes(Node node) {
		for(ConcurrentSkipListSet<Node> set : activeNodes.values())
			synchronized(set) {
				set.remove(node);
			}
	}
}
