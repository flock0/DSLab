package util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;

public class HMACUtils {
	
	private static final String HMAC_ALGO = "hmacSHA256";
	private Mac hMac;
	public HMACUtils(String secretKeyPath) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
		hMac = Mac.getInstance(HMAC_ALGO);
		hMac.init(Keys.readSecretKey(new File(secretKeyPath)));
	}
	
	public byte[] create(String message) {
		return hMac.doFinal(message.getBytes());
	}
	
	public static boolean areEqual(byte[] received_hMac, byte[] calculated_hMac) {
		return Arrays.equals(received_hMac, calculated_hMac);
	}
}
