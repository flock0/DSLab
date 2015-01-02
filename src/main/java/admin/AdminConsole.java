package admin;

import cli.Command;
import cli.Shell;
import controller.IAdminConsole;
import model.ComputationRequestInfo;
import util.Config;

import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.List;

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
	private Shell shell;
	private INotificationCallback callbackStub;
	private NotificationCallback callback;
	
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
		initializeCallbackStub();
	}

	private void initializeShell() {
		shell = new Shell(componentName, userRequestStream, userResponseStream);
		shell.register(this);
	}
	private void initializeCallbackStub()
	{
		try
		{
			callback = new NotificationCallback();
			callbackStub = (INotificationCallback)UnicastRemoteObject.exportObject(callback, 0);			
		}
		catch(RemoteException e)
		{
			//Log somewhere...
			//throw new RuntimeException("RemoteException during initializeCallbackStub.", e);
		}
		
	}
	
	private void shutdown() {			
		if(callback != null)
		{
			try
			{
				UnicastRemoteObject.unexportObject(callback, true);
			}
			catch(NoSuchObjectException e)
			{
				//Log somewhere...
				//throw new RuntimeException("NoSuchObjectException during shutdown.", e);
			}			
		}
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
	
	@Command
	public String subscribe(String username, int credits)
	{
		try
		{			
			if(subscribe(username, credits, callbackStub))
			{
				return "Successfully subscribed for user " + username + ".";
			}
			else
			{
				return "Failed to subscribed for user " + username + ".";
			}
		}
		catch(RemoteException e)
		{
			throw new RuntimeException("RemoteException during subsribe.", e);
		}
				
	}	
	
	@Override
	public boolean subscribe(String username, int credits,
			INotificationCallback callback) throws RemoteException {				
		try
		{
			Registry registry = LocateRegistry.getRegistry(config.getString("controller.host"), config.getInt("controller.rmi.port"));
		    IAdminConsole comp = (IAdminConsole) registry.lookup(config.getString("binding.name"));
		    return comp.subscribe(username, credits, callback);   
	    } catch (Exception e) {
      	    throw new RuntimeException("Exception during statistics.", e);
        }  
	}

	@Command
	@Override
	public List<ComputationRequestInfo> getLogs() throws RemoteException {
		try
		{
			Registry registry = LocateRegistry.getRegistry(config.getString("controller.host"), config.getInt("controller.rmi.port"));
		    IAdminConsole comp = (IAdminConsole) registry.lookup(config.getString("binding.name"));
		    return comp.getLogs();
	    } catch (Exception e) {
      	    throw new RuntimeException("Exception during getLogs.", e);
        }  
	    
	}

	@Command
	@Override
	public LinkedHashMap<Character, Long> statistics() throws RemoteException {	
        try {	            
            Registry registry = LocateRegistry.getRegistry(config.getString("controller.host"), config.getInt("controller.rmi.port"));
            IAdminConsole comp = (IAdminConsole) registry.lookup(config.getString("binding.name"));
            return comp.statistics();       
        } catch (Exception e) {
        	throw new RuntimeException("Exception during statistics.", e);
        }        
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
