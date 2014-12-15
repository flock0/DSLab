package admin;

import cli.Command;
import cli.Shell;
import controller.AdminService;
import controller.IAdminConsole;
import controller.Node;
import model.ComputationRequestInfo;
import util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;

/**
 * Please note that this class is not needed for Lab 1, but will later be
 * used in Lab 2. Hence, you do not have to implement it for the first
 * submission.
 */
public class AdminConsole implements IAdminConsole, Runnable {

	private String componentName;
	private Config config;
	private InputStream userRequestStream;
	private PrintStream userResponseStream;
	private Shell shell = null;
	private final String name = "adminService";
	
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
	public AdminConsole(String componentName, Config config,
			InputStream userRequestStream, PrintStream userResponseStream) {
		this.componentName = componentName;
		this.config = config;
		this.userRequestStream = userRequestStream;
		this.userResponseStream = userResponseStream;
		
		initializeShell();
	}

	private void initializeShell() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}
	
	private void shutdown() {		
		if(shell != null)
			shell.close();		
	}
	
	@Command
	public void exit()
	{
		shutdown();		
	}
	
	@Override
	public void run() {
		System.out.println(componentName + " up and waiting for commands!");
		shell.run();
	}

	@Override
	public boolean subscribe(String username, int credits,
			INotificationCallback callback) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ComputationRequestInfo> getLogs() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Command
	@Override
	public LinkedHashMap<Character, Long> statistics() throws RemoteException {
		LinkedHashMap<Character, Long> statistic = null;
		if (System.getSecurityManager() == null) {
	        //    System.setSecurityManager(new SecurityManager());
	        }
	        try {	            
	            Registry registry = LocateRegistry.getRegistry(config.getString("controller.host"), config.getInt("controller.rmi.port"));
	            IAdminConsole comp = (IAdminConsole) registry.lookup(name);
	            statistic = comp.statistics();
	            for(Entry<Character, Long> entry : statistic.entrySet())
	            {
	            	System.err.println(entry.getKey() + " " + entry.getValue());
	            }	            
	            return statistic;
	        } catch (Exception e) {
	            //System.err.println("statistics exception:");
	            e.printStackTrace();
	        }
	        return statistic;
	}

	@Override
	public Key getControllerPublicKey() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUserPublicKey(String username, byte[] key)
			throws RemoteException {
		// TODO Auto-generated method stub
	}

	/**
	 * @param args
	 *            the first argument is the name of the {@link AdminConsole}
	 *            component
	 */
	public static void main(String[] args) {
		AdminConsole adminConsole = new AdminConsole(args[0], new Config(
				"admin"), System.in, System.out);
		new Thread(adminConsole).start();
	}
}
