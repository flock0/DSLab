package controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import node.SingleComputationHandler;
import util.Channel;
import util.Config;
import util.TcpChannel;
import util.TerminableThread;

public class ClientListener extends TerminableThread {

	private ServerSocket serverSocket;
	private Config config;
	private ExecutorService threadPool;

	public ClientListener(Config config) {
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
			try {
				while (true) {
					Socket socket = null;

					Channel nextRequest = new TcpChannel(serverSocket.accept());
					threadPool.execute(new SingleClientHandler(nextRequest, config));

				}
			} catch (SocketException e) {
				System.out.println("Socket shutdown: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("IOException occured: " + e.getMessage());
			}
		}
	}

}
