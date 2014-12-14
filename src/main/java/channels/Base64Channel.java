package channels;

import java.io.IOException;
import org.bouncycastle.util.encoders.Base64;

/**
 * A two way communication channel using the base64 encoding
 *
 */
public class Base64Channel extends ChannelDecorator {

	public Base64Channel(Channel underlying) {
		super(underlying);
	}

	@Override
	public String readLine() throws IOException {
		byte[] decoded = Base64.decode(super.readLine());
		return decoded.toString();
	}

	@Override
	public void println(String out) {
		byte[] encoded = Base64.encode(out.getBytes());
		super.println(encoded.toString());
	}
	
}
