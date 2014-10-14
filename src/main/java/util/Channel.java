package util;

import java.io.IOException;

/**
 * A two-way connection channel that supports line-based reading and writing
 */
public interface Channel {
	public String readLine() throws IOException;
	public void println(String out);
	public void close();
}
