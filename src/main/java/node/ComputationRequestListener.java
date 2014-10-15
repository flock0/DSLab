package node;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.print.CancelablePrintJob;

import util.Channel;
import util.ComputationChannel;
import util.Config;
import util.TcpChannel;

public class ComputationRequestListener extends Thread {

	private Config config;
	private ServerSocket serverSocket = null;
	private ExecutorService threadPool;

	public ComputationRequestListener(Config config) {
		this.config = config;
		openServerSocket();
		createThreadPool();
	}

	private void openServerSocket() {
		try {
			serverSocket = new ServerSocket(config.getInt("tcp.port"));
		} catch (IOException e) {
			System.out.println("Couldn't create ServerSocket: " + e.getMessage());
		} 
	}

	private void createThreadPool() {
		threadPool = Executors.newCachedThreadPool();
	}

	@Override
	public void run() {
		if (serverSocket != null) {
			
			while (true) {
				Socket socket = null;
				Channel nextRequest;
				try {
					nextRequest = new TcpChannel(serverSocket.accept());
					threadPool.execute(new SingleRequestHandler(nextRequest, config));

				} catch (IOException e) {
					System.out.println("IOException occured: " + e.getMessage());
					break;
				}
			}
		}

	}

}
