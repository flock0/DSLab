package channels;

import java.io.IOException;

public abstract class ChannelDecorator implements Channel {

	protected Channel underlying;
	
	@Override
	public String readStringLine() throws IOException {
		return underlying.readStringLine();
	}
	
	public byte[] readByteLine() throws IOException {
		return underlying.readByteLine();
	}

	@Override
	public void println(String out) {
		underlying.println(out);

	}

	public void println(byte[] out) {
		underlying.println(out);
	}
	
	@Override
	public void close() {
		underlying.close();
	}

	@Override
	public boolean isClosed() {
		return underlying.isClosed();
	}
}
