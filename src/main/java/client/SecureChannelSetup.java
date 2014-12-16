package client;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.util.encoders.Base64;

import channels.Channel;


/**
 * Takes care of the client authentication with the controller and sets up a secure channel
 *
 */
public class SecureChannelSetup {

	private static final int CHALLENGE_LENGTH_IN_BYTES = 32;
	private static final String RSA_CIPHER_STRING = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
	private static final String AES_CIPHER_STRING = "AES/CTR/NoPadding";
	private Cipher rsaCipher;
	private SecureRandom randomNumberGenerator;
	private Channel channel;
	private PrivateKey privKey; //The private key of oneself
	private PublicKey pubKey; //The public key of the other endpoint
	private byte[] clientChallenge;
	private boolean successfullyInitialized = false;
	
	public SecureChannelSetup(Channel channel, PrivateKey privKey, PublicKey pubKey) {
		this.channel = channel;
		this.privKey = privKey;
		this.pubKey = pubKey;
		randomNumberGenerator = new SecureRandom();
		
		try {
			initializeRSA();
			successfullyInitialized = true;
		} catch (Exception e) {
			System.out.println("RSA Initialization Error: " + e.getMessage());
		}
		
	}
	
	private void initializeRSA() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		rsaCipher = Cipher.getInstance(RSA_CIPHER_STRING);
	}

	public Channel authenticate(String username) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException {
		clientChallenge = createClientChallenge();
		sendAuthenticationRequest(username, clientChallenge);
		String plainAnswer = receiveAuthenticationAnswer();
		if(answerIsValid(plainAnswer, clientChallenge)) {
			setupAES(plainAnswer);
		} else {
			//TODO: closeChannel();
		}
		return null; //TODO: Temporär
	}
	
	private void setupAES(String plainAnswer) {
		//TODO: AES aufsetzen und zurückliefern an Caller
		
	}

	private byte[] createClientChallenge() {
		final byte[] clientChallenge = new byte[CHALLENGE_LENGTH_IN_BYTES];
		randomNumberGenerator.nextBytes(clientChallenge);
		return clientChallenge;
	}
	
	private void sendAuthenticationRequest(String username, byte[] clientChallenge) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		String encodedClientChallenge = Base64.encode(clientChallenge).toString();
		String request = String.format("!authenticate %s %s", username, encodedClientChallenge);
		rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherText = rsaCipher.doFinal(request.getBytes());
		channel.println(new String(cipherText));
	}

	private String receiveAuthenticationAnswer() throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
		rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
		String encryptedAnswer = channel.readLine();
		return new String(rsaCipher.doFinal(encryptedAnswer.getBytes()));		
	}

	private boolean answerIsValid(String message, byte[] clientChallenge) {
		String[] split = message.split("\\s");
		if(split.length != 5)
			return false;
		if(!split[0].equals("!ok"))
			return false;
		if(!Base64.encode(clientChallenge).equals(split[1]))
			return false;
		return true;
	}
}
