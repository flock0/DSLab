package client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import channels.Base64Channel;
import channels.Channel;
import channels.TcpChannel;
import cli.Command;
import cli.Shell;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.Config;
import util.Keys;
import util.SecureChannelSetup;
import util.SecurityUtils;

public class Client implements IClientCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Channel underlyingChannel = null; // The original underlying channel
	private Channel channel = null; // The channel that may be decorated during a session with i.e. AES encryption
	private Shell shell;
	private PublicKey controllerPublicKey;
	private boolean successfullyInitialized = false;
	private boolean authenticated = false;
	
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
			SecurityUtils.registerBouncyCastle();
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
		controllerPublicKey = Keys.readPublicPEM(new File(filePath));
	}

	private void initializeSocket() throws UnknownHostException, IOException {
		underlyingChannel = channel = new TcpChannel(new Socket(config.getString("controller.host"), config.getInt("controller.tcp.port")));
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
		throw new UnsupportedOperationException("!login command not used in lab 2. Use !authenticate <username>");
	}

	@Override
	@Command
	public String logout() throws IOException {
		if(authenticated) {
			authenticated = false;
			channel.println("!logout");
			String response = channel.readStringLine();
			channel = underlyingChannel;
			return response;
		} else
			return "Currently not logged in. Please !authenticate first";
	}

	@Override
	@Command
	public String credits() throws IOException {
		if(authenticated) {
			channel.println("!credits");
			return channel.readStringLine();
		} else
			return "Currently not logged in. Please !authenticate first";
	}

	@Override
	@Command
	public String buy(long credits) throws IOException {
		if(authenticated) {
		channel.println(String.format("!buy %d", credits));
		return channel.readStringLine();
		} else
			return "Currently not logged in. Please !authenticate first";
	}

	@Override
	@Command
	public String list() throws IOException {
		if(authenticated) {
			channel.println("!list");
			return channel.readStringLine();
		} else
			return "Currently not logged in. Please !authenticate first";
	}

	@Override
	@Command
	public String compute(String term) throws IOException {
		if(authenticated) {
			channel.println(String.format("!compute %s", term));
			return channel.readStringLine();
		} else
			return "Currently not logged in. Please !authenticate first";
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

	@Command
	@Override
	public String authenticate(String username) throws IOException {
		if(!authenticated) {
			PrivateKey userPrivateKey = loadUserPrivateKey(username);
			SecureChannelSetup auth = new SecureChannelSetup(channel, userPrivateKey, controllerPublicKey, config);
			Channel aesChannel = auth.authenticate(username);
			if(aesChannel == null)
				return "Authentication error!";
			else {
				authenticated = true;
				channel = aesChannel;
				return "Successfully logged in as " + username;
			}
		} else
			return "Currently already logged in. Please !logout first";
	}

	private PrivateKey loadUserPrivateKey(String username) throws IOException {
		String filePath = System.getProperty("user.dir") + File.separator + config.getString("keys.dir")
				+ File.separator + username + ".pem";
		filePath = filePath.replace("/", File.separator);
		return Keys.readPrivatePEM(new File(filePath));
	}
}
