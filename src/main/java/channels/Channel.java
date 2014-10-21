package channels;

import java.io.IOException;

/**
 * A twoway connection channel that supports linebased reading and writing
 */
public interface Channel {
	public String readLine() throws IOException;
	public void println(String out);
	public void close();
	public boolean isClosed();
}
