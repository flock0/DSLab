package util;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Keeps information about all open Channels and closes them if asked.
 *
 */
public class ChannelSet {
	private Set<Channel> set = Collections.newSetFromMap(new ConcurrentHashMap<Channel, Boolean>());
	
	public void add(Channel s) {
		set.add(s);
	}
	
	public void cleanUp() {
		Set<Channel> remove = new HashSet<>();
		
		for(Channel s : set)
			if(s != null && s.isClosed())
				remove.add(s);
		
		for(Channel r : remove)
			set.remove(r);
	}
	
	public void closeAll() {
		cleanUp();
		for(Channel s : set) {
			s.close();
		}
	}
}
