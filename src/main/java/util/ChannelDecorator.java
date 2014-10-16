package util;

import java.io.IOException;

public abstract class ChannelDecorator implements Channel {

	protected Channel underlying;
	
	public ChannelDecorator(Channel underlying) {
		this.underlying = underlying;
	}
	
	@Override
	public String readLine() throws IOException {
		return underlying.readLine();
	}

	@Override
	public void println(String out) {
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
