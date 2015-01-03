package controller;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Routinely purge nodes that appear to be offline
 */
public class NodePurgeTask extends TimerTask {

	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;

	public NodePurgeTask(ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes) {
		this.activeNodes = activeNodes;
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
			set.remove(node);
	}
}
