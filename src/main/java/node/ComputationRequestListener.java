package node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.print.CancelablePrintJob;

import util.Channel;
import util.ChannelSet;
import util.Config;
import util.TcpChannel;
import util.TerminableThread;

/**
 * Listens for incoming computation requests and delegates them using a thread
 * pool.
 *
 */
public class ComputationRequestListener extends TerminableThread {

	private Config config;
	private ServerSocket serverSocket = null;
	private ExecutorService threadPool;
	private ChannelSet openChannels;

	public ComputationRequestListener(Config config) {
		this.config = config;
		openServerSocket();
		createThreadPool();
		openChannels = new ChannelSet();
	}

	private void openServerSocket() {
		try {
			serverSocket = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			System.out.println("Couldn't create ServerSocket: "
					+ e.getMessage());
		}
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
					threadPool.execute(new SingleComputationHandler(nextRequest, config));
					openChannels.cleanUp();

				}
			} catch (SocketException e) {
				System.out.println("Socket shutdown: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IOException occured: " + e.getMessage());
			}
		}

	}

	public void shutdown() {
		try {
			serverSocket.close();
			shutdownPoolAndAwaitTermination();

		} catch (IOException e) {
			// Nothing we can do about that
		}
	}

	/**
	 * Shuts down the ExecutorService in two phases, first by calling shutdown
	 * to reject incoming tasks, and then calling shutdownNow, if necessary, to
	 * cancel any lingering tasks.
	 * 
	 * Taken from http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/
	 * ExecutorService.html
	 */
	private void shutdownPoolAndAwaitTermination() {
		threadPool.shutdown(); // Disable new tasks from being submitted
		try {
			openChannels.closeAll();
			// Wait a while for existing tasks to terminate
			if (!threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
				threadPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!threadPool.awaitTermination(3, TimeUnit.SECONDS))
					System.err.println("Computation ThreadPool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			threadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

}
