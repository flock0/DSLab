package channels;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * A two-way communication channel that uses AES to encrypt its messages
 */
public class AESChannel extends ChannelDecorator {

	private SecretKey key;
	private IvParameterSpec initializationVector;
	Cipher aesCipher;

	public AESChannel(Channel underlying, SecretKey aesKey, byte[] initializationVector, String aesCipherString) throws NoSuchAlgorithmException, NoSuchPaddingException {
		this.underlying = underlying;
		this.key = aesKey;
		this.initializationVector = new IvParameterSpec(initializationVector);

		aesCipher = Cipher.getInstance(aesCipherString);
	}

	@Override
	public String readStringLine() throws IOException {
		return new String(decryptLineFromUnderlying());
	}

	@Override
	public byte[] readByteLine() throws IOException {
		return decryptLineFromUnderlying();
	}

	private byte[] decryptLineFromUnderlying() throws IOException {
		try {
			aesCipher.init(Cipher.DECRYPT_MODE, key, initializationVector);
			return aesCipher.doFinal(underlying.readByteLine());
		} catch (Exception e) {
			throw new IOException("Couldn't decrypt with AES: " +  e.getMessage(), e);
		}
	}

	@Override
	public void println(String out) {
		encryptAndPrintToUnderlying(out.getBytes());

	}

	@Override
	public void println(byte[] out) {
		encryptAndPrintToUnderlying(out);
	}
	
	private void encryptAndPrintToUnderlying(byte[] out) {
		try {
			aesCipher.init(Cipher.ENCRYPT_MODE, key, initializationVector);
			byte[] cipherText = aesCipher.doFinal(out);
			underlying.println(cipherText);
		} catch (Exception e) {
			//TODO: Nicht gut gehandelt. Darf nicht raufpropagiert werden, 
			// da sonst die Zusicherungen im Channel-Interface verletzt würden.
			// Was tun?
		}
	}

}
