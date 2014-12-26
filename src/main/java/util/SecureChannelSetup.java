package util;

import java.io.File;
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
import channels.Base64Channel;
import channels.Channel;


/**
 * Takes care of the client authentication with the controller and sets up a secure channel
 *
 */
public class SecureChannelSetup {

	private static final int CHALLENGE_LENGTH_IN_BYTES = 32;
	private static final String RSA_CIPHER_STRING = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
	private static final String AES_CIPHER_STRING = "AES/CTR/NoPadding";
	private static final int AES_KEY_LENGTH_IN_BYTES = 32;
	private static final int AES_IV_LENGTH_IN_BYTES = 16;
	private Cipher rsaCipher;
	private SecureRandom randomNumberGenerator;
	private Channel channel;
	private Config config;
	private String authenticatedUser;
	private PrivateKey privKey; //The private key of oneself
	private PublicKey pubKey; //The public key of the other endpoint
	private byte[] clientChallenge;
	private boolean successfullyInitialized = false;
	
	public SecureChannelSetup(Channel channel, PrivateKey privKey) {
		this.channel = new Base64Channel(channel);
		this.privKey = privKey;
		randomNumberGenerator = new SecureRandom();
		
		try {
			initializeRSA();
			successfullyInitialized = true;
		} catch (Exception e) {
			System.out.println("RSA Initialization Error: " + e.getMessage());
		}
	}

	public SecureChannelSetup(Channel channel, PrivateKey privKey, PublicKey pubKey) {
		this(channel, privKey);
		this.pubKey = pubKey;
	}

	private void initializeRSA() throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		rsaCipher = Cipher.getInstance(RSA_CIPHER_STRING);
	}

	/**
	 * Authenticates a client with the given username
	 * @param username The username to authenticate with the controller
	 * @return A valid AESChannel to communicate with the controller
	 */
	public Channel authenticate(String username) throws IOException {
		if(successfullyInitialized) {
			try {
				Channel aesChannel = null;
				clientChallenge = createChallenge();
				sendAuthenticationRequest(username, clientChallenge);
				String plainAnswer = receiveAuthenticationAnswer();
				if(answerIsValid(plainAnswer, clientChallenge)) {
					String[] splitAnswer = plainAnswer.split("\\s"); 
					aesChannel = setupAES(splitAnswer);
					sendAuthenticationAnswer(aesChannel, splitAnswer);

				} else {
					channel.close();
				}
				return aesChannel;
			} catch(Exception e) {
				channel.close();
				throw new IOException("Couldn't authenticate user: " + e.getMessage(), e);
			}
		}
		throw new IOException("Authentication failed: Couldn't initialize secure channel!");
	}
	
	private byte[] createChallenge() {
		return getRandomBytes(CHALLENGE_LENGTH_IN_BYTES);
	}
	
	private byte[] getRandomBytes(int byteCount) {
		final byte[] randomBytes = new byte[byteCount];
		randomNumberGenerator.nextBytes(randomBytes);
		return randomBytes;
	}

	private void sendAuthenticationRequest(String username, byte[] clientChallenge) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		String encodedClientChallenge = Base64.encode(clientChallenge).toString();
		String request = String.format("!authenticate %s %s", username, encodedClientChallenge);
		rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
		byte[] cipherText = rsaCipher.doFinal(request.getBytes());
		channel.println(cipherText);
	}

	private String receiveAuthenticationAnswer() throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
		rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] encryptedAnswer = channel.readByteLine();
		return new String(rsaCipher.doFinal(encryptedAnswer));		
	}

	private Channel setupAES(String[] splitAnswer) throws NoSuchAlgorithmException, NoSuchPaddingException {
		byte[] aesKey = getAESKey(splitAnswer);
		byte[] aesIV = getAESIV(splitAnswer);
		
		return new AESChannel(channel, aesKey, aesIV, AES_CIPHER_STRING);
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
	
	public Channel awaitAuthentication() throws IOException {
		if(successfullyInitialized) {
			try {
				String clearTextRequest = receiveRequest();
				String[] splitRequest = clearTextRequest.split("\\s");
				if(requestIsValid(splitRequest)) {
					PublicKey userPublicKey = loadUserPublicKey(splitRequest[1]);
					byte[] controllerChallenge = createChallenge();
					byte[] aesKey = getRandomBytes(AES_KEY_LENGTH_IN_BYTES);
					byte[] aesIV = getRandomBytes(AES_IV_LENGTH_IN_BYTES);
					
					sendOKAnswer(splitRequest[2], controllerChallenge, aesKey, aesIV, userPublicKey);
					Channel aesChannel = setupAES(aesKey, aesIV);//eigene Methode, nicht bestehende verwenden
					String clearTextOKAnswer = receiveOKAnswer(aesChannel);
					if(okAnswerIsValid(clearTextOKAnswer, controllerChallenge)) {
						authenticatedUser = splitRequest[1];
						return aesChannel;
					} else {
						return null; // Die zweite Nachricht vom Client (Dritte Nachricht im Protokoll) ist ungültig
					}
				} else {
					return null; // Die erste Nachricht vom Client ist ungültig
				}
			} catch(Exception e) {
				throw new IOException("Authentication failed: Couldn't initialize secure channel!", e);
			}
		}
		throw new IOException("Authentication failed: Couldn't initialize secure channel!");
	}

	private String receiveRequest() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
		rsaCipher.init(Cipher.DECRYPT_MODE, privKey);
		byte[] encryptedRequest = channel.readByteLine();
		return new String(rsaCipher.doFinal(encryptedRequest));
	}

	private boolean requestIsValid(String[] splitRequest) {
		if(splitRequest.length != 3)
			return false;
		if(!splitRequest[0].equals("!authenticate"))
			return false;
		return true;
	}

	private PublicKey loadUserPublicKey(String username) throws IOException {
		String filePath = System.getProperty("user.dir") + File.separator + config.getString("keys.dir")
				+ File.separator + username + ".pub.pem";
		filePath = filePath.replace("/", File.separator);
		return Keys.readPublicPEM(new File(filePath));
	}

	private void sendOKAnswer(String encodedClientChallenge, byte[] controllerChallenge, byte[] aesKey, byte[] aesIV, PublicKey userPublicKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		String encodedControllerChallenge = new String(Base64.encode(controllerChallenge));
		String encodedAesKey = new String(Base64.encode(aesKey));
		String encodedAesIV = new String(Base64.encode(aesIV));
		String answer = String.format("!ok %s %s %s %s", encodedClientChallenge, encodedControllerChallenge, encodedAesKey, encodedAesIV);
		
		rsaCipher.init(Cipher.ENCRYPT_MODE, userPublicKey);
		
		byte[] cipherText = rsaCipher.doFinal(answer.getBytes());
		channel.println(cipherText);
	}

	private Channel setupAES(byte[] aesKey, byte[] aesIV) throws NoSuchAlgorithmException, NoSuchPaddingException {
		return new AESChannel(channel, aesKey, aesIV, AES_CIPHER_STRING);
	}

	private String receiveOKAnswer(Channel aesChannel) throws IOException {
		return aesChannel.readStringLine();
	}

	private boolean okAnswerIsValid(String clearTextOKAnswer, byte[] controllerChallenge) {
		return Base64.encode(controllerChallenge).equals(clearTextOKAnswer);
	}

	public String getAuthenticatedUser() {
		return authenticatedUser;
	}
}
