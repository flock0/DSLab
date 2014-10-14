package node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TimerTask;

import util.Config;

public class AliveMessageTask extends TimerTask {

	private Config config;
	private String aliveMessage;
	private DatagramPacket alivePacket;
	private DatagramSocket socket;

	public AliveMessageTask(Config config) {
		this.config = config;
		try {

			initializeSocket();
			constructAliveMessage();
			constructPacket();

		} catch (SocketException e) {
			System.out.println("Error creating DatagramSocket: " + e.getMessage());
			cancel();
		} catch (UnknownHostException e) {
			System.out.println("Couldn't resolve IP address: " + e.getMessage());
			cancel();
		}

	}

	private void initializeSocket() throws SocketException {
		socket = new DatagramSocket();
	}

	private void constructAliveMessage() {
		aliveMessage = String.format("!alive %d %s",
				config.getInt("controller.udp.port"),
				config.getString("node.operators"));
	}

	private void constructPacket() throws UnknownHostException {
		byte[] messageBuffer = aliveMessage.getBytes();
		alivePacket = new DatagramPacket(messageBuffer, messageBuffer.length,
				InetAddress.getByName(config.getString("controller.host")),
				config.getInt("controller.udp.port"));

	}

	@Override
	public void run() {
		try {
			socket.send(alivePacket);
		} catch (IOException e) {
			System.out.println("Couldn't send alive message: " + e.getMessage());
			cancel();
		}
	}

	@Override
	public boolean cancel() {
		closeSocket();
		return super.cancel();
	}

	private void closeSocket() {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}

}
