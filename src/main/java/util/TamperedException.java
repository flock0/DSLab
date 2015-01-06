package util;

import java.io.IOException;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;

/**
 * Signals that the message integrity algorithm has detected an attempt to tamper with the message
 */
public class TamperedException extends IOException {

	private String clearText;
	
	public String getClearText() {
		return clearText;
	}

	public TamperedException(String message) {
		super(message);
	}

	public TamperedException(String message, Exception cause) {
		super(message, cause);
	}

	public TamperedException(String message, String clearText) {
		super(message);
		this.clearText = clearText;
		
	}

}
