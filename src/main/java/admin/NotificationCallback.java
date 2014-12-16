package admin;

import java.io.Serializable;
import java.rmi.RemoteException;

public class NotificationCallback implements INotificationCallback, Serializable {

	private static final long serialVersionUID = -1843491644280623620L;
	
	
	@Override
	public void notify(String username, int credits) throws RemoteException {
		System.out.format("Notification: %1$s has less than %2$d credits.%3$s", username, credits, System.getProperty("line.separator"));

	}

}
