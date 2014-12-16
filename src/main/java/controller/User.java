package controller;


import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import admin.INotificationCallback;
import util.Config;
import util.FixedParameters;

/**
 * Contains a single user loaded from the config files
 */
public class User {
	
	private String username;
	private AtomicInteger credits;
	private String password;
	private AtomicInteger concurrentOnlineCounter;
	private ConcurrentHashMap<Integer, List<INotificationCallback>> notificationCallbacks;
	
	public User(String username, Config config) {
		this.username = username;
		credits = new AtomicInteger(config.getInt(username + ".credits"));
		password = config.getString(username + ".password");
		concurrentOnlineCounter = new AtomicInteger(0);
		notificationCallbacks = new ConcurrentHashMap<Integer, List<INotificationCallback>>();
	}
	
	public int getCredits() {
		return credits.get();
	}
	public void setCredits(int credits) {		
		this.credits.set(credits);
		for(Entry<Integer, List<INotificationCallback>> entry: notificationCallbacks.entrySet())
		{
			if(entry.getKey() > credits)
			{
				try
				{
					synchronized(entry.getValue())
					{
						for(INotificationCallback callback: entry.getValue())
						{
							callback.notify(username, entry.getKey());				
						}
						entry.getValue().clear();
					}					
				}
				catch(RemoteException e)
				{
					throw new RuntimeException("Remoteexception during notify.", e);
				}
			}
		}
	}
	public String getUsername() {
		return username;
	}

	public boolean isOnline() {
		return concurrentOnlineCounter.get() > 0;
	}
	private String isOnlineAsString() {
		if(isOnline())
			return "online";
		return "offline";
	}

	public void increaseOnlineCounter() {
		concurrentOnlineCounter.incrementAndGet();
	}
	public void decreaseOnlineCounter() {
		concurrentOnlineCounter.decrementAndGet();
	}
	public boolean isCorrectPassword(String passwordToCheck) {
		return password.equals(passwordToCheck);
	}
	public boolean hasEnoughCredits(ClientRequest request) {
		return credits.get() >= (request.getOperators().length * FixedParameters.CREDIT_COST_PER_OPERATOR);
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(username);
		builder.append(" ");
		builder.append(isOnlineAsString());
		builder.append(" Credits: ");
		builder.append(credits.get());
		builder.append('\n');
		return builder.toString();
	}

	public void addNotificationCallback(int credits,
			INotificationCallback callback) {
		List<INotificationCallback> callbacks;
		synchronized(notificationCallbacks)
		{
			callbacks = notificationCallbacks.get(credits);
			if(callbacks == null)
			{
				callbacks = new ArrayList<INotificationCallback>();
				notificationCallbacks.put(credits, callbacks);
			}			
		}
		
		synchronized(callbacks)
		{
			callbacks.add(callback);
		}		
	}
}
