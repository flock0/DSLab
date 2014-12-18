package controller;


import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
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
	private LinkedHashMap<Integer, LinkedList<INotificationCallback>> notificationCallbacks;
	
	public User(String username, Config config) {
		this.username = username;
		credits = new AtomicInteger(config.getInt(username + ".credits"));
		password = config.getString(username + ".password");
		concurrentOnlineCounter = new AtomicInteger(0);
		notificationCallbacks = new LinkedHashMap<Integer, LinkedList<INotificationCallback>>();
	}
	
	public int getCredits() {
		return credits.get();
	}
	public void setCredits(int credits) {		
		this.credits.set(credits);
		synchronized(notificationCallbacks)
		{
			for(Entry<Integer, LinkedList<INotificationCallback>> entry: notificationCallbacks.entrySet())
			{
				if(entry.getKey() > credits)
				{
					try
					{
						INotificationCallback callback = entry.getValue().poll();
						synchronized(callback)	
						{							
							while(callback != null)
							{
								callback.notify(username, entry.getKey());	
								callback = entry.getValue().poll();
							}		
							notificationCallbacks.remove(callback);
						}
					}
					catch(RemoteException e)
					{
						throw new RuntimeException("Remoteexception during notify.", e);
					}
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
		LinkedList<INotificationCallback> callbacks;
		synchronized(notificationCallbacks)
		{
			callbacks = notificationCallbacks.get(credits);
			if(callbacks == null)
			{
				callbacks = new LinkedList<INotificationCallback>();
				notificationCallbacks.put(credits, callbacks);
			}							
			synchronized(callbacks)
			{
				callbacks.add(callback);
			}
		}	
	
	}
}
