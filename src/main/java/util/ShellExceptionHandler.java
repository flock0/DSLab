package util;

import java.io.PrintStream;

/**
 * Handles exceptions throws by the Shell
 */
public interface ShellExceptionHandler {
	/**
	 * Handles exceptions throws by the Shell 
	 * @param throwable The throwable that has been throws
	 * @param out An optional PrintStream for writing mesages to output
	 * @return whether the Exception has been handled
	 */
	public boolean handle(Throwable throwable, PrintStream out);
}
