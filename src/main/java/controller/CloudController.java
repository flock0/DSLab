package controller;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import cli.Command;
import cli.Shell;

public class CloudController implements ICloudControllerCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Timer nodePurgeTimer;
	private Shell shell;
	private ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>> activeNodes;
	private ConcurrentHashMap<String, Node> allNodes;
	private ConcurrentHashMap<String, User> users;
	private AliveListener aliveListener;
	private ClientListener clientListener;
	

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
		initializeListeners();
		
		loadUsers();
		initializeShell();
	}

	private void initializeNodeMaps() {
		activeNodes = new ConcurrentHashMap<Character, ConcurrentSkipListSet<Node>>();
		allNodes = new ConcurrentHashMap<String, Node>();
	}

	private void initializeListeners() {
		aliveListener = new AliveListener(activeNodes, allNodes, config);
		clientListener = new ClientListener(users, activeNodes, config);
	}
	
	private void loadUsers() {
		users = new ConcurrentHashMap<>();
		Config userConfig = new Config("user");
		
		for(String key : userConfig.listKeys()) {
			
			String username = key.split("\\.")[0];
			if(!users.containsKey(username)) {
				User user = new User(username, userConfig);
				users.put(username, user);
			}
		}
		
	}

	private void initializeShell() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
		
	}

	@Override
	public void run() {
		
		startNodePurgeTimer();
		startListeners();
		startShell();
		
	}

	private void startNodePurgeTimer() {
		nodePurgeTimer.schedule(new NodePurgeTask(activeNodes, config), 0, config.getInt("node.checkPeriod"));
	}

	private void startListeners() {
		aliveListener.start();
		clientListener.start();
	}

	private void startShell() {
		new Thread(shell).start();
		System.out.println(componentName + " up and waiting for commands!");
	}

	@Override
	@Command
	public String nodes() throws IOException {
		int counter = 1;
		StringBuilder builder = new StringBuilder();
		
		for(Node n : allNodes.values()) {
			builder.append(counter++);
			builder.append(". ");
			builder.append(n);
		}
		
		return builder.toString();
	}

	@Override
	public String users() throws IOException {
		int counter = 1;
		StringBuilder builder = new StringBuilder();
		
		for(User u : users.values()) {
			builder.append(counter++);
			builder.append(". ");
			builder.append(u);
		}
		
		return builder.toString();
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
		new Thread(cloudController).start();
	}

}
