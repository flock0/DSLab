package util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketException;

public class ConcreteShellExceptionHandler implements ShellExceptionHandler {

	@Override
	public boolean handle(Throwable throwable, PrintStream out) {
		Throwable cause = throwable;
		if(throwable.getCause() != null)
			cause = throwable.getCause();
		
		if(cause instanceof SocketException) {
			out.println("Error: Socket has been closed");
			return true;
		} else if(cause instanceof IllegalArgumentException) {
			out.println("Error: Illegal argument");
			return true;
		} else if(cause instanceof NumberFormatException) {
			out.println("Error: Number format invalid");
			return true;
		}
		return false;
	}
}
