package node;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.Channel;
import util.ComputationChannel;
import util.Config;
import util.TcpChannel;

public class ComputationRequestListener extends Thread {

	private Config config;
	private ServerSocket serverSocket;
	private ExecutorService threadPool;
	
	public ComputationRequestListener(Config config) {
		this.config = config;
		openServerSocket();
		createThreadPool();
	}

	private void openServerSocket() {
		serverSocket = new ServerSocket(config.getInt("tcp.port")); // TODO: Exceptions abfangen
	}

	private void createThreadPool() {
		threadPool = Executors.newCachedThreadPool();
		
	}

	@Override
	public void run() {
		while(true) {
			Socket socket = null;
			Channel nextRequest = new TcpChannel(serverSocket.accept()); // TODO: Exceptions abfangen
			threadPool.execute(new SingleRequestHandler(nextRequest, config));
		}

	}

}
