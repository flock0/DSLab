package node;

import util.Config;
import util.NodeLogger;
import util.TerminableThread;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import model.ComputationRequestInfo;
import cli.Command;
import cli.Shell;

/**
 * Initializes and starts a computation node
 */
public class Node implements INodeCli, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Shell shell = null;
	private Timer aliveTimer = null;
	private TerminableThread listener = null;
	private TerminableThread commit = null;
	private boolean successfullyInitialized = false;
	private int resources;
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
		
		try {
			NodeLogger.NodeID = componentName;
			NodeLogger.Directory = config.getString("log.dir");
			
			initializeListener();
			aliveTimer = new Timer();
			initializeShell();
			joinCloud();
		} catch (IOException e) {
			System.out.println("Couldn't create ServerSocket: " + e.getMessage());
		}
	}

	private void initializeListener() throws IOException {
		listener = new ComputationRequestListener(config, this);
	}

	private void initializeShell() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}

	private void joinCloud() throws IOException {
		System.out.println("Trying to join cloud...");
		commit = new CommitHandler(this, config);
		commit.start();
	}
	
	public void finishInitialization(boolean result) {
		successfullyInitialized = result;
		if(!successfullyInitialized) {
			System.out.println("Can't join cloud, shutting down!");
			shutdown();
		} else {
			// make sure everything is shut down before 
			// starting shell and all the listeners
			commit.shutdown();
			new Thread(this).start();
		}
	}
	
	@Override
	public void run() {
		if(successfullyInitialized) {
			startAliveTimer();
			startRequestListener();
			startShell();
		}
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
		shutdown();
		return "Shut down completed! Bye ..";
	}

	private void shutdown() {
		if(listener != null)
			listener.shutdown();
		if(aliveTimer != null)
			aliveTimer.cancel();
		if(shell != null)
			shell.close();
		
	}

	public List<ComputationRequestInfo> getLogs()
	{
		ArrayList<ComputationRequestInfo> returnList = new ArrayList<ComputationRequestInfo>();
		String directory = (System.getProperty("user.dir") + File.separator + config.getString("log.dir")).replace("/", File.separator);
		File[] files = new File(directory).listFiles();		
		if(files != null)
		{
			try
			{
				for(int i = 0; i < files.length; i++)
				{
					List<String> lines = Files.readAllLines(files[i].toPath(), Charset.defaultCharset());
					if(lines.size() > 1)
					{
						ComputationRequestInfo info = new ComputationRequestInfo();
						info.setNodeName(componentName);
						info.setTerm(lines.get(0));
						info.setResult(lines.get(1));				
						String[] timeStampSplitted = files[i].getName().split("_");
						if(timeStampSplitted.length == 3) //If not three => malformed => ignore file
						{
							info.setTimeStamp(timeStampSplitted[0] + "_" + timeStampSplitted[1]);
							returnList.add(info);
						}											
					}									
				}		
			}
			catch(IOException e)
			{
				 throw new RuntimeException("IOException during getLogs.", e);
			}
		}
		return returnList;
	}
	
	public int getRmin() {
		return config.getInt("node.rmin");
	}
	
	public void updateResources(int resources) {
		this.resources = resources;
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
	}

	// --- Commands needed for Lab 2. Please note that you do not have to
	// implement them for the first submission. ---

	@Override
	@Command
	public String resources() throws IOException {
		return String.valueOf(resources);
	}

}
