package node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import channels.Channel;
import channels.ChannelSet;
import channels.TcpChannel;
import util.Config;
import util.TerminableThread;

public class CommitHandler extends TerminableThread {
	
	private Node node;
	private Config config;
	private DatagramPacket helloPacket;
	private DatagramSocket datagramSocket;
	
	private ArrayList<controller.Node> onlineNodes;
	private ArrayList<Boolean> nodeReplies;
	private int rmax;
	
	private String[] initMessage;
	
	public CommitHandler(Node node, Config config) {
		this.node = node;
		this.config = config;
		sendHello();
	}
	
	private void constructPacket() throws UnknownHostException {
		byte[] messageBuffer = "hello".getBytes();
		helloPacket = new DatagramPacket(messageBuffer, messageBuffer.length,
				InetAddress.getByName(config.getString("controller.host")),
				config.getInt("controller.udp.port"));
	}
	
	private void sendHello() {
		try {
			constructPacket();
		} catch (UnknownHostException e) {
			System.out.println("Couldn't resolve IP address: " + e.getMessage());
			closeDatagramSocket();
		}
		
		try {
			datagramSocket = new DatagramSocket();
			datagramSocket.send(helloPacket);
		} catch (PortUnreachableException e) {
			// Ignore and keep sending packets
			closeDatagramSocket();
		} catch (IOException e) {
			System.out.println("Couldn't send hello message: " + e.getMessage());
			closeDatagramSocket();
		}
		
		receiveInit();
	}
	
	private void receiveInit() {		
		try {
			byte[] buffer = new byte[4096];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

			datagramSocket.receive(packet);
			
			String message = new String(packet.getData());
			initMessage = message.trim().split("\\s");
			
			if (messageIsValid(initMessage)) {
				rmax = Integer.valueOf(initMessage[initMessage.length-1]);
			} else {
				node.finishInitialization(false);
			}
		} catch (SocketException e) {
			System.out.println("Socket shutdown: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IOException occured: " + e.getMessage());
		} finally {
			closeDatagramSocket();
		}
	}
	
	@Override
	public void run() {
		if(initMessage.length == 2) {
			node.finishInitialization(true);
			shutdown();
		} else {
			// send !share <resources> to all active nodes
			int res = (int) Math.floor(rmax/onlineNodes.size());
			ChannelSet channelSet = new ChannelSet();
			ExecutorService threadPool = Executors.newFixedThreadPool(onlineNodes.size());
			try {
				for(int i = 0; i < onlineNodes.size(); i++) {
					Channel channel = new TcpChannel(
							new Socket(onlineNodes.get(i).getIPAddress().getHostAddress(), 
									   onlineNodes.get(i).getTCPPort()));
					channelSet.add(channel);
					threadPool.execute(new SingleCommitHandler(channel, this, res));
				}
			}  catch (UnknownHostException e) {
				System.out.println("Couldn't resolve IP address: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("Couldn't create socket: " + e.getMessage());
			}
		}
	}
	
	public void addNodeReply(boolean reply) {
		nodeReplies.add(reply);
		if(nodeReplies.size() == onlineNodes.size()) {
			for(int i = 0; i < nodeReplies.size(); i++) {
				if(nodeReplies.get(i).booleanValue() == false) {
					node.finishInitialization(false);
					shutdown();
				}
			}
			// TODO: inform that a new node is joining the cloud
			
			node.finishInitialization(true);
			shutdown();
		}
	}
	
	private boolean messageIsValid(String[] message) throws NumberFormatException, 
															UnknownHostException {
		/*
		 *  if at least 1 or more nodes active: "!init <node_1:port> ... <node_n:port> rmax"
		 *  if none nodes active: "!init rmax"
		 */
		if (message.length >= 2 && message[0].equals("!init")
			&& isInteger(message[message.length-1])) {
			onlineNodes = new ArrayList<controller.Node>();
			nodeReplies = new ArrayList<Boolean>();
			if(message.length > 2) {
				int i = 1;
				do {
					if (!message[i].isEmpty()) {
						// assuming that node information <address:port> is correct
						String[] splitNode = message[i].trim().split(":");
						// taking care of forward or backward slashes in case of localhost
						splitNode[0] = splitNode[0].replace("\\", "");
						splitNode[0] = splitNode[0].replace("/", "");
						onlineNodes.add(new controller.Node(InetAddress.getByName(splitNode[0]), 
												 			Integer.valueOf(splitNode[1])));
					} else {
						return false;
					}
				} while(i < (message.length-2));
			}
			return true;
		} else 
			return false;
	}
	
	private boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	private void closeDatagramSocket() {
		if (datagramSocket != null && !datagramSocket.isClosed()) {
			datagramSocket.close();
		}
	}

	@Override
	public void shutdown() {
		closeDatagramSocket();
	}
}
