package channels;

import java.io.IOException;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import util.HMACUtils;
import util.TamperedException;

/**
 * A two-way communication channel that ensures the integrity of the messages via HMAC.
 * The check will only be carried out when the received message is a !compute message.
 * This means, that the second parameter of it (when split at the whitespaces) is '!compute'
 * 
 * When sending messages, the HMAC will only be prepended, when the cleartext message
 * starts with '!compute'
 * 
 * On any other messages, the channel behave completely like the underlying channel.
 */
public class ComputeHMACChannel extends ChannelDecorator {

	private HMACUtils hmacUtils;

	public ComputeHMACChannel(Channel underlying, HMACUtils hmac) {
		this.underlying = underlying;
		this.hmacUtils = hmac;
	}

	@Override
	public String readStringLine() throws TamperedException, IOException {

		String readLine = underlying.readStringLine();
		String[] splitReadLine = readLine.split("\\s");

		if(splitReadLine.length < 2 || !splitReadLine[1].equals("!compute")) return readLine; // Behave like the underlying

		String hmacBase64 = splitReadLine[0];
		String clearText = readLine.substring(hmacBase64.length() + 1);
		
		try {
			byte[] receivedHmac = Base64.decode(hmacBase64);
			byte[] calculatedHmac = hmacUtils.createHMAC(clearText);

			if(!HMACUtils.areEqual(receivedHmac, calculatedHmac))
				throw new TamperedException("HMAC does not match. The message received has been tampered!", clearText);

			return clearText;

		} catch (Base64DecodingException e) {
			throw new TamperedException("HMAC does not match. The message received has been tampered!", clearText);
		}
	}

	@Override
	public byte[] readByteLine() throws IOException {
		throw new UnsupportedOperationException("Sending raw bytes is not possible with the HMACChannel.");
	}

	@Override
	public void println(String out) {
		String message = out;
		if(out.startsWith("!compute")) {
			byte[] hmac = hmacUtils.createHMAC(out);
			String hmacBase64 = Base64.encode(hmac);
			message = String.format("%s %s", hmacBase64, out);
		}

		underlying.println(message);
	}

	@Override
	public void println(byte[] out) {
		throw new UnsupportedOperationException("Sending raw bytes is not possible with the HMACChannel.");
	}

}
