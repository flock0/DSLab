package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import cli.Command;
import cli.Shell;
import util.Channel;
import util.Config;
import util.TcpChannel;

public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Channel channel = null;
	private Shell shell;
	private boolean initializedSuccessfully = false;

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
	public Client(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;

		try {
			
			initializeSocket();
			initializeShell();
			initializedSuccessfully = true;
		} catch (UnknownHostException e) {
			System.out.println("Couldn't resolve IP address: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Exception on socket creation: " + e.getMessage());
		}
	}

	private void initializeShell() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	private void initializeSocket() throws UnknownHostException, IOException {
		channel = new TcpChannel(new Socket(config.getString("controller.host"), config.getInt("controller.tcp.port")));
	}

	@Override
	public void run() {
		if(initializedSuccessfully) {
			System.out.println(componentName + " up and waiting for commands!");
			shell.run();
		}
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		channel.println(String.format("!login %s %s", username, password));
		return channel.readLine();
	}

	@Override
	@Command
	public String logout() throws IOException {
		channel.println("!logout");
		return channel.readLine();
	}

	@Override
	@Command
	public String credits() throws IOException {
		channel.println("!credits");
		return channel.readLine();
	}

	@Override
	@Command
	public String buy(long credits) throws IOException {
		channel.println(String.format("!buy %d", credits));
		return channel.readLine();
	}

	@Override
	@Command
	public String list() throws IOException {
		channel.println("!list");
		return channel.readLine();
	}

	@Override
	@Command
	public String compute(String term) throws IOException {
		channel.println(String.format("!compute %s", term));
		return channel.readLine();
	}

	@Override
	@Command
	public String exit() throws IOException {
		shell.close();
		channel.close();
		return "Shut down completed! Bye ..";
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link Client} component
	 */
	public static void main(String[] args) {
		Client client = new Client(args[0], new Config("client"), System.in,
				System.out);
		new Thread(client).start();
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	public String authenticate(String username) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
