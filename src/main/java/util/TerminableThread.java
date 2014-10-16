package util;

public abstract class TerminableThread extends Thread {
	
	/**
	 * Shuts down the thread and closes/releases all ressources used by it
	 */
	public abstract void shutdown();
}
