package channels;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESChannel extends ChannelDecorator {

	private SecretKey key;
	private IvParameterSpec initializationVector;
	Cipher aesCipher;

	public AESChannel(Channel underlying, SecretKey aesKey,
			byte[] initializationVector, String aesCipherString) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.underlying = underlying;
		this.key = aesKey;
		this.initializationVector = new IvParameterSpec(initializationVector);

		aesCipher = Cipher.getInstance(aesCipherString);
	}

	@Override
	public String readStringLine() throws IOException {
		try {
			aesCipher.init(Cipher.DECRYPT_MODE, key, initializationVector);
			byte[] clearText = aesCipher.doFinal(underlying.readByteLine());
			return new String(clearText);
		} catch (Exception e) {
			throw new IOException("Couldn't decrypt with AES: " +  e.getMessage(), e);
		}
		

	}

	@Override
	public byte[] readByteLine() throws IOException {
		try {
			aesCipher.init(Cipher.DECRYPT_MODE, key, initializationVector);
			byte[] clearText = aesCipher.doFinal(underlying.readByteLine());
			return clearText;
		} catch (Exception e) {
			throw new IOException("Couldn't decrypt with AES: " +  e.getMessage(), e);
		}
	}

	@Override
	public void println(String out) {
		try {
			aesCipher.init(Cipher.ENCRYPT_MODE, key, initializationVector);
			byte[] cipherText = aesCipher.doFinal(out.getBytes());
			underlying.println(cipherText);
		} catch (Exception e) {
			//TODO: Gscheit handeln
		}

	}

	@Override
	public void println(byte[] out) {
		try {
			aesCipher.init(Cipher.ENCRYPT_MODE, key, initializationVector);
			byte[] cipherText = aesCipher.doFinal(out);
			underlying.println(cipherText);
		} catch (Exception e) {
			//TODO: Gscheit handeln
		}
	}


}
