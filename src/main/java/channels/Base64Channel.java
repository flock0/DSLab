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
		byte[] decoded = Base64.decode(underlying.readStringLine());
		return new String(decoded);
	}

	@Override
	public void println(String out) {
		byte[] encoded = Base64.encode(out.getBytes());
		super.println(new String(encoded));
	}

	@Override
	public byte[] readByteLine() throws IOException {
		byte[] decoded = Base64.decode(underlying.readStringLine());
		return decoded;
	}

	@Override
	public void println(byte[] out) {
		byte[] encoded = Base64.encode(out);
		super.println(new String(encoded));
		
	}
	
}
