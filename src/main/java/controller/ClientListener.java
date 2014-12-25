package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import util.Config;
import util.TerminableThread;
import channels.Base64Channel;
import channels.Channel;
import channels.ChannelSet;
import channels.TcpChannel;

/**
 * Listens for client connections at the TCP port
 * and delegates them to the handlers
 */
public class ClientListener extends TerminableThread {

	private ServerSocket serverSocket;
	private Config config;
	private ExecutorService threadPool;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private ConcurrentHashMap<String, User> users;
	private ChannelSet openChannels; // Keeps track of all open channels for clients or nodes. Used for shutdown 

	public ClientListener(ConcurrentHashMap<String, User> users, ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes, Config config) throws IOException {
		this.users = users;
		this.activeNodes = activeNodes;
		this.config = config;
		
		openServerSocket();
		createThreadPool();
		openChannels = new ChannelSet();
	}

	private void openServerSocket() throws IOException {
		serverSocket = new ServerSocket(config.getInt("tcp.port"));
	}

	private void createThreadPool() {
		threadPool = Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		if (serverSocket != null) {
			try {
				while (true) {
					Channel nextRequest = new TcpChannel(serverSocket.accept());
					openChannels.add(nextRequest);
					threadPool.execute(new SingleClientHandler(nextRequest, activeNodes, users, openChannels, config));					
					openChannels.cleanUp(); // Make a semi-regular clean up
				}
			} catch (SocketException e) {
				System.out.println("ClientSocket shutdown: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IOException occured: " + e.getMessage());
			}
		}
	}

	public void shutdown() {
		try {
			serverSocket.close();
			shutdownSocketsAndPool();

		} catch (IOException e) {
			// Nothing we can do about that
		}
	}

	/**
	 * Shuts down the ExecutorService in two phases, first by calling shutdown
	 * to reject incoming tasks and closing all channels, and then calling shutdownNow, if necessary, to
	 * cancel any lingering tasks.
	 * 
	 * Taken from http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/
	 * ExecutorService.html
	 */
	private void shutdownSocketsAndPool() {
		threadPool.shutdown(); // Disable new tasks from being submitted
		try {
			openChannels.closeAll();
			// Wait a while for existing tasks to terminate
			if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
				threadPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!threadPool.awaitTermination(3, TimeUnit.SECONDS))
					System.err.println("Client ThreadPool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			threadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
