package controller;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import channels.Channel;
import channels.ChannelSet;
import channels.ComputationCommunicator;
import channels.TcpChannel;
import computation.LogRequest;
import computation.LogResult;
import computation.Result;
import util.Config;
import model.ComputationRequestInfo;
import admin.INotificationCallback;

public class AdminService implements IAdminConsole {

	private Registry registry = null;
	private IAdminConsole remote = null;
	private Config config;
	private ConcurrentHashMap<String, User> users;
	private LinkedHashMap<Character, Long> statistic;
	private ConcurrentHashMap<String, Node> allNodes;
	private ChannelSet channelSet;
	
	public AdminService(Config config, ConcurrentHashMap<String, User> users, LinkedHashMap<Character, Long> statistic, ConcurrentHashMap<String, Node> allNodes)
	{
		super();
		this.config = config;
		this.users = users;
		this.statistic = statistic;
		this.allNodes = allNodes;
		channelSet = new ChannelSet();
		try {            
            
            registry = LocateRegistry.createRegistry(config.getInt("controller.rmi.port"));
            remote = (IAdminConsole) UnicastRemoteObject.exportObject(this, 0);            
            registry.rebind(config.getString("binding.name"), remote);    
        } catch (RemoteException e) {            
        	//Log somewhere...
        	//throw new RuntimeException("Error while starting AdminService.", e);
        } 
	}
	
	@Override
	public boolean subscribe(String username, int credits,
		INotificationCallback callback) throws RemoteException {		
		if(credits < 0)
		{
			return false;
		}
		
		User u = users.get(username);
		if(u != null)
		{
			u.addNotificationCallback(credits, callback);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public List<ComputationRequestInfo> getLogs() throws RemoteException {

		List<ComputationRequestInfo> logs = new ArrayList<ComputationRequestInfo>();
		ComputationCommunicator currentComputationCommunicator = null;
		LogRequest request = new LogRequest();
		
		for(Node node: allNodes.values())
		{
			if(node.isOnline())
			{
				currentComputationCommunicator = null;
				try {
					Channel channelForCommunicator = new TcpChannel(
							new Socket(node.getIPAddress(), node.getTCPPort())); 
					
					synchronized(channelSet) {channelSet.add(channelForCommunicator);}					
					
					currentComputationCommunicator = new ComputationCommunicator(channelForCommunicator);
					
					currentComputationCommunicator.sendRequest(request);
	
					Result result = currentComputationCommunicator.getResult();
	
					//// Check Result ////
					switch(result.getStatus()) {
					case OK:
						if(result instanceof LogResult)
						{
							LogResult lr = (LogResult)result;
							logs.addAll(lr.getLogs());
						}
						else
						{
							synchronized(channelSet) {channelSet.cleanUp();}
							//Log somewhere...
							//throw new RuntimeException("Failed to gather Node logs (wrong resulttype).");
						}
						break;					
					default: // Something went wrong
						synchronized(channelSet) {channelSet.cleanUp();}
						//Log somewhere...
						//throw new RuntimeException("Failed to gather Node logs (wrong status).");
					}					
				} catch (SocketException e) {
					//Ignore Node
					//throw new RuntimeException("SocketException during getLogs.", e);
				} catch (IOException e) {
					//Ignore Node
					//throw new RuntimeException("IOException during getLogs.", e);
				} finally {
					if(currentComputationCommunicator != null)
						currentComputationCommunicator.close();					
					}
			}
		}
		synchronized(channelSet) {channelSet.cleanUp();}
		Collections.sort(logs);
		return logs;		
	}

	@Override
	public LinkedHashMap<Character, Long> statistics() throws RemoteException {
				
		synchronized(statistic)
		{			
			ArrayList<Entry<Character, Long>> entryList = new ArrayList<Entry<Character, Long>>(statistic.entrySet());
		    Collections.sort(entryList, new Comparator<Entry<Character, Long>>() {
		         @Override
		         public int compare(Entry<Character, Long> first, Entry<Character, Long> second) {
		            return (-1) * first.getValue().compareTo(second.getValue());
		         }
		     });					
		    
		    LinkedHashMap<Character, Long> returnMap = new LinkedHashMap<Character, Long>();
		    for(Entry<Character, Long> e: entryList)
		    	returnMap.put(e.getKey(), e.getValue());
		    
		    return returnMap;		    				
		}		
	}	


	@Override
	public Key getControllerPublicKey() throws RemoteException {
		throw new UnsupportedOperationException("This operation is not supported in Lab 2.");
	}

	@Override
	public void setUserPublicKey(String username, byte[] key)
			throws RemoteException {
		throw new UnsupportedOperationException("This operation is not supported in Lab 2.");
	}
		
	public void close()
	{		
		try
		{			
			UnicastRemoteObject.unexportObject(this, true);
		}
		catch(NoSuchObjectException e)
		{
			System.err.println("Error while unexporting object: " + e.getMessage());
		}	
						
		try
		{
			if(registry != null)
			{
				registry.unbind(config.getString("binding.name"));
			}			
		}
		catch(Exception e)
		{
			System.err.println("Error while unbinding object: " + e.getMessage());
		}	
		
		synchronized(channelSet)
		{
			channelSet.closeAll();
		}
	}		
}
