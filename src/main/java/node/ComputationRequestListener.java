package node;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import util.Config;
import util.FixedParameters;
import util.HMACUtils;
import util.TerminableThread;
import channels.Channel;
import channels.ChannelSet;
import channels.HMACChannel;
import channels.TcpChannel;

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
	private Node node;
	private HMACUtils hmacUtils;

	public ComputationRequestListener(Config config, Node node) throws IOException {
		this.config = config;
		this.node = node;
		try {
			initializeHMAC();
			openServerSocket();
			createThreadPool();
			openChannels = new ChannelSet();
		} catch(Exception e) {
			throw new IOException("Couldn't setup request listener", e);
		}
	}

	private void initializeHMAC() throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		String keyPath = System.getProperty("user.dir") + File.separator 
				+ config.getString("hmac.key").replace("/", File.separator);
		hmacUtils = new HMACUtils(keyPath);
	}

	private void openServerSocket() throws IOException {
		serverSocket = new ServerSocket(config.getInt("tcp.port"));
	}

	private void createThreadPool() {
		threadPool = Executors.newFixedThreadPool(FixedParameters.CONCURRENT_NODE_THREADS);
	}

	@Override
	public void run() {
		if (serverSocket != null) {
			try {
				while (true) {
					Channel nextRequest = new HMACChannel(new TcpChannel(serverSocket.accept()), hmacUtils);
					openChannels.add(nextRequest);
					threadPool.execute(new SingleComputationHandler(nextRequest, config, node));
					openChannels.cleanUp(); // Make a semi-regular clean up

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
