package channels;

import java.io.IOException;

import org.bouncycastle.util.encoders.Base64;

/**
 * A two way communication channel using the base64 encoding
 *
 */
public class Base64Channel extends ChannelDecorator {

	public Base64Channel(Channel underlying) {
		this.underlying = underlying;
	}

	@Override
	public String readStringLine() throws IOException {
		return new String(decodeLineFromUnderlying());
	}

	@Override
	public byte[] readByteLine() throws IOException {
		return decodeLineFromUnderlying();
	}

	@Override
	public void println(String out) {
		byte[] encoded = Base64.encode(out.getBytes());
		underlying.println(new String(encoded));
	}

	@Override
	public void println(byte[] out) {
		byte[] encoded = Base64.encode(out);
		underlying.println(new String(encoded));
	}
	
	private byte[] decodeLineFromUnderlying() throws IOException {
		String readLine = underlying.readStringLine();
		return Base64.decode(readLine);
	}
}
