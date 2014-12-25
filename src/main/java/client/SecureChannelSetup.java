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

import channels.AESChannel;
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

	public Channel authenticate(String username) throws IOException {
		try {
		
		clientChallenge = createClientChallenge();
		sendAuthenticationRequest(username, clientChallenge);
		String plainAnswer = receiveAuthenticationAnswer();
		if(answerIsValid(plainAnswer, clientChallenge)) {
			String[] splitAnswer = plainAnswer.split("\\s"); 
			Channel aesChannel = setupAES(splitAnswer);
			sendAuthenticationAnswer(aesChannel, splitAnswer);
			
		} else {
			channel.close();
		}
		return channel;
		} catch(Exception e) {
			channel.close();
			throw new IOException("Couldn't authenticate user: " + e.getMessage(), e);
		}
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
		String encryptedAnswer = channel.readStringLine();
		return new String(rsaCipher.doFinal(encryptedAnswer.getBytes()));		
	}

	private Channel setupAES(String[] splitAnswer) throws NoSuchAlgorithmException, NoSuchPaddingException {
		byte[] aesKey = getAESKey(splitAnswer);
		byte[] aesInitializationVector = getAESIV(splitAnswer);
		
		return new AESChannel(channel, aesKey, aesInitializationVector, AES_CIPHER_STRING);
	}

	private boolean answerIsValid(String message, byte[] clientChallenge) {
		if(message == null)
			return false;
		String[] split = message.split("\\s");
		if(split.length != 5)
			return false;
		if(!split[0].equals("!ok"))
			return false;
		if(!Base64.encode(clientChallenge).equals(split[1]))
			return false;
		return true;
	}

	private byte[] getAESIV(String[] splitAnswer) {
		return Base64.decode(splitAnswer[3]);
	}

	private byte[] getAESKey(String[] splitAnswer) {
		return Base64.decode(splitAnswer[4]);
	}

	private void sendAuthenticationAnswer(Channel aesChannel, String[] splitAnswer) {
		aesChannel.println(splitAnswer[2]);
	}
}
