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

	private static Remote adminService = null;
	private static Registry registry = null;
	private static IAdminConsole stub = null;
	private static final String name = "adminService";
	
	@Override
	public boolean subscribe(String username, int credits,
			INotificationCallback callback) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<ComputationRequestInfo> getLogs() throws RemoteException {
		//
		return null;
	}

	@Override
	public LinkedHashMap<Character, Long> statistics() throws RemoteException {
		LinkedHashMap<Character, Long> a = new LinkedHashMap<Character, Long>();
		a.put('/', 12L);
		return a;
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
	
	public static void export(ConcurrentHashMap<String, User> users, Config config)
	{
        if (System.getSecurityManager() == null) {
       //     System.setSecurityManager(new SecurityManager());
        }
        try {            
            adminService = new AdminService();
            registry = LocateRegistry.createRegistry(config.getInt("controller.rmi.port"));
            stub = (IAdminConsole) UnicastRemoteObject.exportObject(adminService, 0);            
            registry.rebind(name, stub);                  
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }

	public static void unexport()
	{		
		try
		{
			if(registry != null)
			{
				registry.unbind(name);
			}
			if(adminService != null)
			{
				UnicastRemoteObject.unexportObject(adminService, true);
			}
		}
		catch(AccessException e)
		{
			e.printStackTrace();
		}	
		catch(NotBoundException e)
		{
			e.printStackTrace();
		}
		catch(RemoteException e)
		{
			e.printStackTrace();
		}		
	}	
}
