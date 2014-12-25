package client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;


import java.security.PrivateKey;
import java.security.PublicKey;

import channels.Base64Channel;
import channels.Channel;
import channels.TcpChannel;
import cli.Command;
import cli.Shell;
import util.Config;
import util.Keys;

public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Channel channel = null;
	private Shell shell;
	private PublicKey controllerKey;
	private boolean successfullyInitialized = false;

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
			loadControllerPublicKey();
			initializeSocket();
			initializeShell();
			successfullyInitialized = true;
		} catch (UnknownHostException e) {
			System.out.println("Couldn't resolve IP address: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("Couldn't initialize client: " + e.getMessage());
		}
	}

	private void loadControllerPublicKey() throws IOException {
		String filePath = System.getProperty("user.dir") + File.separator + config.getString("controller.key");
		filePath = filePath.replace("/", File.separator);
		controllerKey = Keys.readPublicPEM(new File(filePath));
	}

	private void initializeSocket() throws UnknownHostException, IOException {
		channel = new Base64Channel(new TcpChannel(new Socket(config.getString("controller.host"), config.getInt("controller.tcp.port"))));
	}

	private void initializeShell() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	@Override
	public void run() {
		if(successfullyInitialized) {
			System.out.println(componentName + " up and waiting for commands!");
			shell.run();
		}
	}

	@Override
	@Command
	public String login(String username, String password) throws IOException {
		channel.println(String.format("!login %s %s", username, password));
		return channel.readStringLine();
	}

	@Override
	@Command
	public String logout() throws IOException {
		channel.println("!logout");
		return channel.readStringLine();
	}

	@Override
	@Command
	public String credits() throws IOException {
		channel.println("!credits");
		return channel.readStringLine();
	}

	@Override
	@Command
	public String buy(long credits) throws IOException {
		channel.println(String.format("!buy %d", credits));
		return channel.readStringLine();
	}

	@Override
	@Command
	public String list() throws IOException {
		channel.println("!list");
		return channel.readStringLine();
	}

	@Override
	@Command
	public String compute(String term) throws IOException {
		channel.println(String.format("!compute %s", term));
		return channel.readStringLine();
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

	@Override
	public String authenticate(String username) throws IOException {
		PrivateKey userPrivateKey = loadUserPrivateKey(username);
		SecureChannelSetup auth = new SecureChannelSetup(channel, userPrivateKey, controllerKey);
		return null; //TODO: auth durchführen, null nur temporär
	}

	private PrivateKey loadUserPrivateKey(String username) throws IOException {
		String filePath = System.getProperty("user.dir") + File.separator + config.getString("keys.dir")
				+ File.separator + username + ".pem";
		filePath = filePath.replace("/", File.separator);
		return Keys.readPrivatePEM(new File(filePath));
	}
}
