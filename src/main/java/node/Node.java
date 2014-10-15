package node;

import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.util.Timer;

import cli.Command;
import cli.Shell;

public class Node implements INodeCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Shell shell;
	private Timer aliveTimer;
	private ComputationRequestListener listener;
	
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
	public Node(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		
		aliveTimer = new Timer();
		listener = new ComputationRequestListener(config);
		initializeShell();
		
	}

	private void initializeShell() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	@Override
	public void run() {
		startAliveTimer();
		startRequestListener();
		startShell();
	}

	private void startAliveTimer() {
		aliveTimer.schedule(new AliveTask(config), 0, config.getInt("node.alive"));
	}

	private void startRequestListener() {
		listener.start();
	}

	private void startShell() {
		new Thread(shell).start();
		System.out.println(componentName + " up and waiting for commands!");
	}

	@Override
	@Command
	public String exit() throws IOException {
		listener.shutdown();
		aliveTimer.cancel();
		shell.close();
		return "Shut down completed! Bye ..";
	}

	@Override
	public String history(int numberOfRequests) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Node} component,
	 *            which also represents the name of the configuration
	 */
	public static void main(String[] args) {
		Node node = new Node(args[0], new Config(args[0]), System.in,
				System.out);
		new Thread(node).start();
		
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String resources() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
