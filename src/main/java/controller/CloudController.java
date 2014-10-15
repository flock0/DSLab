package controller;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class CloudController implements ICloudControllerCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Timer nodePurgeTimer;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private ConcurrentHashMap<String, Node> allNodes;
	private AliveListener aliveListener;

	/**
	 * @param componentName
	 *            the name of the component - represented in the prompt
	 * @param config
	 *            the configuration to use
	 * @param userRequestStream
	 *            the input stream to read user input from
	 * @param userResponseStream
	 *            the output stream to write the console output to
	 */
	public CloudController(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		
		nodePurgeTimer = new Timer();
		Node.TimeoutPeriod = config.getInt("node.timeout");
		initializeNodeMaps();
		aliveListener = new AliveListener(activeNodes, allNodes, config);
		loadUsers();
		initializeShell();
	}

	private void initializeNodeMaps() {
		activeNodes = new ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>>();
		allNodes = new ConcurrentHashMap<String, Node>();
	}

	private void loadUsers() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() {
		
		startNodePurgeTimer();
		startAliveListener();
		startRequestListener();
		startShell();
		
	}

	private void startNodePurgeTimer() {
		nodePurgeTimer.schedule(new NodePurgeTask(activeNodes, config), 0, config.getInt("node.checkPeriod"));
	}

	private void startAliveListener() {
		aliveListener.start();
	}

	@Override
	public String nodes() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String users() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String exit() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link CloudController}
	 *            component
	 */
	public static void main(String[] args) {
		CloudController cloudController = new CloudController(args[0],
				new Config("controller"), System.in, System.out);
		// TODO: start the cloud controller
	}

}
