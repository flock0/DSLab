package util;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Base64;

import com.sun.crypto.provider.AESKeyGenerator;

import channels.AESChannel;
import channels.Base64Channel;
import channels.Channel;


/**
 * Takes care of the client authentication with the controller and sets up a secure channel
 *
 */
public class SecureChannelSetup {

	
	private static final String RSA_CIPHER_STRING = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
	private static final String AES_CIPHER_STRING = "AES/CTR/NoPadding";
	private static final int CHALLENGE_LENGTH_IN_BYTES = 32;
	private static final int AES_KEY_LENGTH_IN_BITS = 256;
	private static final int AES_IV_LENGTH_IN_BYTES = 16;
	private static final String B64 = "a-zA-Z0-9/+"; // Used for validating the authentication messages
	private Cipher rsaCipher;
	private SecureRandom randomNumberGenerator;
	private Channel channel;
	private Config config;
	private String authenticatedUsername;
	private PrivateKey ownPrivKey; //The private key of oneself
	private PublicKey otherPubKey; //The public key of the other endpoint
	private byte[] clientChallenge;
	private boolean successfullyInitialized = false;
	
	public SecureChannelSetup(Channel channel, PrivateKey privKey, Config config) {
		this.channel = new Base64Channel(channel);
		this.ownPrivKey = privKey;
		this.config = config;
		randomNumberGenerator = new SecureRandom();
		
		try {
			initializeRSA();
			successfullyInitialized = true;
		} catch (Exception e) {
			System.out.println("RSA Initialization Error: " + e.getMessage());
		}
	}

	public SecureChannelSetup(Channel channel, PrivateKey privKey, PublicKey pubKey, Config config) {
		this(channel, privKey, config);
		this.otherPubKey = pubKey;
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
				
				sendFirstMessage(username, clientChallenge);
				String receivedSecondMessage = receiveSecondMessage();
				
				if(secondMessageIsValid(receivedSecondMessage, clientChallenge)) {
					String[] splitSecondMessage = receivedSecondMessage.split("\\s"); 
					
					aesChannel = setupClientAES(splitSecondMessage);
					sendThirdMessage(aesChannel, splitSecondMessage);

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

	private void sendFirstMessage(String username, byte[] clientChallenge) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		String encodedClientChallenge = new String(Base64.encode(clientChallenge));
		// !authenticate <username> <client challenge>
		String request = String.format("!authenticate %s %s", username, encodedClientChallenge);
		rsaCipher.init(Cipher.ENCRYPT_MODE, otherPubKey);
		byte[] cipherText = rsaCipher.doFinal(request.getBytes());
		channel.println(cipherText);
	}

	private String receiveSecondMessage() throws InvalidKeyException, IOException, IllegalBlockSizeException, BadPaddingException {
		rsaCipher.init(Cipher.DECRYPT_MODE, ownPrivKey);
		byte[] encryptedSecondMessage = channel.readByteLine();
		return new String(rsaCipher.doFinal(encryptedSecondMessage));		
	}

	private Channel setupClientAES(String[] splitSecondMessage) throws NoSuchAlgorithmException, NoSuchPaddingException {
		byte[] aesKey = getAESKey(splitSecondMessage);
		byte[] aesIV = getAESIV(splitSecondMessage);

		return new AESChannel(channel, new SecretKeySpec(aesKey, "AES"), aesIV, AES_CIPHER_STRING);
	}

	private boolean secondMessageIsValid(String message, byte[] clientChallenge) {
		if(message == null)
			return false;

		boolean answerIsValid = message.matches("!ok ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{43}= ["+B64+"]{22}==");
		assert answerIsValid : "2nd message";
		if(!answerIsValid)
			return false;

		String[] split = message.split("\\s");
		if(!challengeResponseIsCorrect(clientChallenge, split[1]))
			return false;
		return true;
	}

	private boolean challengeResponseIsCorrect(byte[] challenge,
			String responseToChallenge) {
		return new String(Base64.encode(challenge)).equals(responseToChallenge);
	}

	private byte[] getAESIV(String[] splitSecondMessage) {
		return Base64.decode(splitSecondMessage[4]);
	}

	private byte[] getAESKey(String[] splitSecondMessage) {
		return Base64.decode(splitSecondMessage[3]);
	}

	private void sendThirdMessage(Channel aesChannel, String[] splitSecondMessage) {
		// <controller challenge>
		aesChannel.println(splitSecondMessage[2]);
	}
	
	public Channel awaitAuthentication() throws IOException {
		if(successfullyInitialized) {
			try {
				String clearFirstMessage = receiveFirstMessage();
				String[] splitFirstMessage = clearFirstMessage.split("\\s");
				
				if(firstMessageIsValid(clearFirstMessage)) {
					
					PublicKey userPublicKey = loadUserPublicKey(splitFirstMessage[1]);
					byte[] controllerChallenge = createChallenge();
					
					KeyGenerator aesKeyGen = KeyGenerator.getInstance("AES");
					aesKeyGen.init(AES_KEY_LENGTH_IN_BITS);
					SecretKey aesKey = aesKeyGen.generateKey();
					byte[] aesIV = getRandomBytes(AES_IV_LENGTH_IN_BYTES);
					
					sendSecondMessage(splitFirstMessage[2], controllerChallenge, aesKey, aesIV, userPublicKey);
					Channel aesChannel = setupControllerAES(aesKey, aesIV);
					String clearThirdMessage = receiveThirdMessage(aesChannel);
					if(thirdMessageIsValid(clearThirdMessage, controllerChallenge)) {
						
						authenticatedUsername = splitFirstMessage[1];
						return aesChannel;
					} else {
						throw new IOException("Response from client to our challenge was invalid");
					}
				} else {
					throw new IOException("Request from client was invalid.");
				}
			} catch(Exception e) {
				throw new IOException("Authentication failed: Couldn't initialize secure channel!", e);
			}
		}
		throw new IOException("Authentication failed: Couldn't initialize secure channel!");
	}

	private String receiveFirstMessage() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
		rsaCipher.init(Cipher.DECRYPT_MODE, ownPrivKey);
		byte[] encryptedFirstMessage = channel.readByteLine();
		return new String(rsaCipher.doFinal(encryptedFirstMessage));
	}

	private boolean firstMessageIsValid(String request) {
		boolean requestIsValid = request.matches("!authenticate \\w+ ["+B64+"]{43}=");
		assert requestIsValid : "1st message"; 
		return requestIsValid;
	}

	private PublicKey loadUserPublicKey(String username) throws IOException {
		String filePath = System.getProperty("user.dir") + File.separator + config.getString("keys.dir")
				+ File.separator + username + ".pub.pem";
		filePath = filePath.replace("/", File.separator);
		return Keys.readPublicPEM(new File(filePath));
	}

	private void sendSecondMessage(String encodedClientChallenge, byte[] controllerChallenge, SecretKey aesKey, byte[] aesIV, PublicKey userPublicKey) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		String encodedControllerChallenge = new String(Base64.encode(controllerChallenge));
		String encodedAesKey = new String(Base64.encode(aesKey.getEncoded()));
		String encodedAesIV = new String(Base64.encode(aesIV));
		// !ok <client challenge> <controller challenge> <aes key> <aes initialization vector>
		String secondMessage = String.format("!ok %s %s %s %s", encodedClientChallenge, encodedControllerChallenge, encodedAesKey, encodedAesIV);
		
		rsaCipher.init(Cipher.ENCRYPT_MODE, userPublicKey);
		
		byte[] cipherText = rsaCipher.doFinal(secondMessage.getBytes());
		channel.println(cipherText);
	}

	private Channel setupControllerAES(SecretKey aesKey, byte[] aesIV) throws NoSuchAlgorithmException, NoSuchPaddingException {
		return new AESChannel(channel, aesKey, aesIV, AES_CIPHER_STRING);
	}

	private String receiveThirdMessage(Channel aesChannel) throws IOException {
		return aesChannel.readStringLine();
	}

	private boolean thirdMessageIsValid(String clearThirdMessage, byte[] controllerChallenge) {
		boolean thirdMessageIsValid = clearThirdMessage.matches("["+B64+"]{43}=");
		assert thirdMessageIsValid :  "3rd message";
		return thirdMessageIsValid && challengeResponseIsCorrect(controllerChallenge, clearThirdMessage);
	}

	public String getAuthenticatedUsername() {
		return authenticatedUsername;
	}
}
