package channels;

import java.io.IOException;

/**
 * A twoway connection channel that supports linebased reading and writing
 */
public interface Channel {
	public String readStringLine() throws IOException;
	public byte[] readByteLine() throws IOException;
	public void println(String out);
	public void println(byte[] out);
	public void close();
	public boolean isClosed();
}
