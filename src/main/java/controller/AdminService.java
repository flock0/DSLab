package controller;

import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import util.Config;
import model.ComputationRequestInfo;
import admin.INotificationCallback;

public class AdminService implements IAdminConsole {

	private Registry registry = null;
	private IAdminConsole remote = null;
	private Config config;
	private ConcurrentHashMap<String, User> users;
	private LinkedHashMap<Character, Long> statistic;
	
	public AdminService(ConcurrentHashMap<String, User> users, LinkedHashMap<Character, Long> statistic, Config config)
	{
		super();
		this.config = config;
		this.users = users;
		this.statistic = statistic;
		try {            
            
            registry = LocateRegistry.createRegistry(config.getInt("controller.rmi.port"));
            remote = (IAdminConsole) UnicastRemoteObject.exportObject(this, 0);            
            registry.rebind(config.getString("binding.name"), remote);    
        } catch (RemoteException e) {            
            throw new RuntimeException("Error while starting AdminService.", e);
        } 
	}
	
	@Override
	public boolean subscribe(String username, int credits,
		INotificationCallback callback) throws RemoteException {
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
		//
		return null;
	}

	@Override
	public LinkedHashMap<Character, Long> statistics() throws RemoteException {
		synchronized(statistic)
		{
			return statistic;
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
	}		
}
