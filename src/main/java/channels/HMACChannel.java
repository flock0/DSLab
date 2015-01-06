package channels;

import java.io.IOException;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

import util.HMACUtils;
import util.TamperedException;

/**
 * A two-way communication channel that ensures the integrity of the messages via HMAC
 * 
 */
public class HMACChannel extends ChannelDecorator {

	private HMACUtils hmacUtils;
	
	public HMACChannel(Channel underlying, HMACUtils hmac) {
		this.underlying = underlying;
		this.hmacUtils = hmac;
	}
	
	@Override
	public String readStringLine() throws TamperedException, IOException {
		try {
			String readLine = underlying.readStringLine();
			String[] splitReadLine = readLine.split("\\s");
			
			if(splitReadLine.length < 2)
				throw new IOException("Couldn't recognize HMAC!");
			
			String hmacBase64 = splitReadLine[0];
			String clearText = readLine.substring(hmacBase64.length() + 1);
			
			byte[] receivedHmac = Base64.decode(hmacBase64);
			byte[] calculatedHmac = hmacUtils.createHMAC(clearText);
			
			if(!HMACUtils.areEqual(receivedHmac, calculatedHmac))
				throw new TamperedException("HMAC does not match. The message received has been tampered!", clearText);
			
			return clearText;
			
		} catch (Base64DecodingException e) {
			throw new IOException("Couldn't recognize HMAC!", e);
		}
	}

	@Override
	public byte[] readByteLine() throws IOException {
		throw new UnsupportedOperationException("Sending raw bytes is not possible with the HMACChannel.");
	}

	@Override
	public void println(String out) {
		byte[] hmac = hmacUtils.createHMAC(out);
		String hmacBase64 = Base64.encode(hmac);
		String message = String.format("%s %s", hmacBase64, out);
		
		underlying.println(message);
	}

	@Override
	public void println(byte[] out) {
		throw new UnsupportedOperationException("Sending raw bytes is not possible with the HMACChannel.");
	}

}
