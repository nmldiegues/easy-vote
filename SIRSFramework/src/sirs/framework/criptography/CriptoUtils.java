package sirs.framework.criptography;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RSABlindingEngine;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.generators.RSABlindingFactorGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSABlindingParameters;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.util.encoders.Base64Encoder;

import sirs.framework.exception.CriptoFrameworkException;

public class CriptoUtils {

	private static int saltLength = 20;
	
	/**
	 * Generate a Java standard asymmetric RSA key pair
	 * @param keySize The strength of the keys
	 * @return	The key pair
	 */
	public static KeyPair generateJavaKeys(int keySize) throws CriptoFrameworkException{
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
	        keyGen.initialize(keySize);
	        return keyGen.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	public static SecretKey generateJavaSymKey(int keySize) throws CriptoFrameworkException{
		try{
	        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
	        keyGen.init(keySize);
	        return keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	/**
	 * Generates an asymmetric key pair
	 * @param keySize	The size of the key
	 * @return	An asymmetric key pair type of the Bouncy Castle
	 */
	public static AsymmetricCipherKeyPair generateKeys(int keySize) {
		RSAKeyPairGenerator r = new RSAKeyPairGenerator();

		/*
		 *	a BigInteger for the exponent, a SecureRandom type object, the strength of the key, and the number of iterations to
		 *	the algorithm that verifies the generation of the keys based off prime numbers. 80 is more than enough
		 */
		r.init(new RSAKeyGenerationParameters(new BigInteger("10001", 16), new SecureRandom(), keySize, 80));
		AsymmetricCipherKeyPair keys = r.generateKeyPair();

		return keys;
	}

	/**
	 * Convert a java PublicKey to a CipherParameters specialized in RSAKeyParameters
	 * @param key	The PublicKey
	 * @return	The Bouncy Castle version
	 */
	public static RSAKeyParameters publicKeyToCipherParameters(PublicKey key){
		return new RSAKeyParameters(false, ((RSAPublicKey)key).getModulus(), ((RSAPublicKey)key).getPublicExponent());
	}
	
	/**
	 * Convert a java PrivateKey to a CipherParameters specialized in RSAKeyParameters
	 * @param key	The PrivateKey
	 * @return	The Bouncy Castle version
	 */
	public static RSAKeyParameters privateKeyToCipherParameters(PrivateKey key){
		return new RSAKeyParameters(true, ((RSAPrivateKey)key).getModulus(), ((RSAPrivateKey)key).getPrivateExponent());
	}
	
	/**
	 * Converts a CipherParameters cast to a RSAKeyParameters which represents an
	 * asymmetric key in Bouncy Castle to the matching PublicKey in java security libraries
	 * @param key	The BouncyCastle CipherParameters of a public key
	 * @return	The java type of PublicKey of an asymmetric key
	 */
	public static PublicKey cipherParametersToPublicKey(RSAKeyParameters key) throws CriptoFrameworkException{
		try {
			RSAPublicKeySpec spec = new RSAPublicKeySpec(key.getExponent(), key.getModulus());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePublic(spec);
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeySpecException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	/**
	 * Converts a CipherParameters cast to a RSAKeyParameters which represents an
	 * asymmetric key in Bouncy Castle to the matching PrivateKey in java security libraries
	 * @param key	The BouncyCastle CipherParameters of a private key
	 * @return	The java type of PrivateKey of an asymmetric key
	 */
	public static PrivateKey cipherParametersToPrivateKey(RSAKeyParameters key) throws CriptoFrameworkException{
		try {
			RSAPrivateKeySpec spec = new RSAPrivateKeySpec(key.getExponent(), key.getModulus());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return keyFactory.generatePrivate(spec);
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeySpecException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	/**
	 * Generates a blinding factor for the given public key
	 * @param pubKey	The public key for the blinding process
	 * @return	The resulting blinding factor
	 */
	public static BigInteger generateBlindingFactor(CipherParameters pubKey) {
		RSABlindingFactorGenerator gen = new RSABlindingFactorGenerator();

		gen.init(pubKey);

		return gen.generateBlindingFactor();
	}

	/**
	 * Blinds a text with a blinding factor and the respective used public key
	 * @param key	The public key used in the blinding factor creation process
	 * @param factor	The blinding factor for the blinding process
	 * @param msg	The text to blind
	 * @return	The blinded text
	 */
	public static byte[] blind(CipherParameters key, BigInteger factor, byte[] msg) throws CriptoFrameworkException{
		RSABlindingEngine eng = new RSABlindingEngine();

		RSABlindingParameters params = new RSABlindingParameters((RSAKeyParameters) key, factor);
		
		/*
		 *	notice the RSABlindingEngine eng being passed to the PSSSigner
		 *	the last argument of the PSSSigner is arbitrary, define at will 
		 */
		PSSSigner blindSigner = new PSSSigner(eng, new SHA1Digest(), saltLength);
		blindSigner.init(true, params);

		blindSigner.update(msg, 0, msg.length);

		byte[] blinded = null;
			
		/*
		 *	method name is misleading, it depends on the Engine passed
		 *	since it's a blinding engine. thus this method will return
		 *	the given text to the update method, blinded with the
		 *	given RSABlindingParameters
		 */
		try {
			blinded = blindSigner.generateSignature();
		} catch (DataLengthException e) {
			throw new CriptoFrameworkException(e);
		} catch (CryptoException e) {
			throw new CriptoFrameworkException(e);
		}

		return blinded;
	}

	/**
	 * Unblinds a blinded text with a blinding factor and the respective used public key
	 * @param key	The public key previously used to generate the blinding factor and to blind the text
	 * @param factor	The supposedly private blinding factor
	 * @param msg	The blinded text
	 * @return	The unblinded text
	 */
	public static byte[] unblind(CipherParameters key, BigInteger factor, byte[] msg) {
		/*
		 * Once again notice the usage of a RSABlindingEngine and respective Parameters
		 */
		RSABlindingEngine eng = new RSABlindingEngine();
		RSABlindingParameters params = new RSABlindingParameters((RSAKeyParameters) key,factor);
		eng.init(false, params);
		/*
		 * Another odd method. It returns the unblinded text.
		 */
		return eng.processBlock(msg, 0, msg.length);
	}

	/**
	 * Creates a digital signature
	 * @param key	The private key to cipher the hash of the text
	 * @param toSign	The bytes of the corresponding text
	 * @return	The bytes of the digital signature
	 */
	public static byte[] sign(CipherParameters key, byte[] toSign) throws CriptoFrameworkException{
		/*
		 * Works as expected, it uses an RSAEngine and a Digester as parameters
		 * Once again the last parameter is arbitrary, but has to match the others used previously
		 */
		PSSSigner signer = new PSSSigner(new RSAEngine(), new SHA1Digest(), saltLength);
		signer.init(true, key);
		signer.update(toSign, 0, toSign.length);

		byte[] sig = null;

		try {
			sig = signer.generateSignature();
		} catch (DataLengthException e) {
			throw new CriptoFrameworkException(e);
		} catch (CryptoException e) {
			throw new CriptoFrameworkException(e);
		}

		return sig;
	}

	/**
	 * Verifies that a text matches the given signature
	 * @param key	Public key that pairs up with the used private key to generate the given signature
	 * @param msg	The message to check upon
	 * @param sig	The signature that has to match the given msg text
	 * @return	True if the given text is valid upon the given signature, false otherwise
	 */
	public static boolean verify(CipherParameters key, byte[] msg, byte[] sig) {
		/*
		 * Typical methods and arguments
		 */
		PSSSigner signer = new PSSSigner(new RSAEngine(), new SHA1Digest(), saltLength);
		signer.init(false, key);

		signer.update(msg,0,msg.length);

		return signer.verifySignature(sig);
	}

	/**
	 * Signs a blinded message, which is actually ciphering the blinded message with his own private key
	 * @param key	The private key to use in the signature
	 * @param msg	The message to cipher with private key
	 * @return	The message ciphered with a private key to serve as a blind signature
	 */
	public static byte[] signBlinded(CipherParameters key, byte[] msg) {
		RSAEngine signer = new RSAEngine();
		signer.init(true, key);
		return signer.processBlock(msg, 0, msg.length);
	}

	/**
	 * Base64 encoder
	 * @param bytes	Bytes to encode as text
	 * @return	The string representing the text
	 */
	public static String base64encode(byte[] bytes) throws CriptoFrameworkException{
		Base64Encoder b64e = new Base64Encoder();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			b64e.encode(bytes, 0, bytes.length, os);
		} catch (IOException e) {
			throw new CriptoFrameworkException(e);
		}
		return os.toString();
	}

	/**
	 * Base64 decoder
	 * @param encoded	String representing the text to decode as bytes
	 * @return	The bytes resulting of the decode
	 */
	public static byte[] base64decode(String encoded) throws CriptoFrameworkException{
		Base64Encoder b64e = new Base64Encoder();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			b64e.decode(encoded, os);
		} catch (IOException e) {
			throw new CriptoFrameworkException(e);
		}
		return os.toByteArray();
	}
	
	public static SecretKey recreateAESKey(byte[] key) throws CriptoFrameworkException{
		return new SecretKeySpec(key, "AES");
	}
	
	/**
	 * Recreates a Private Key from its bytes representation
	 * @param key	The bytes corresponding to the key obtained with getEncoded()
	 * @return	The Private Key
	 */
	public static PrivateKey recreatePrivateKey(byte[] key) throws CriptoFrameworkException{
		try {
			return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key));
		} catch (InvalidKeySpecException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	/**
	 * Recreates a Public Key from its bytes representation
	 * @param key	The bytes corresponding to the key obtained with getEncoded()
	 * @return	The Public Key
	 */
	public static PublicKey recreatePublicKey(byte[] key) throws CriptoFrameworkException{
		try {
			return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
		} catch (InvalidKeySpecException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	/**
	 * Recreates a X509Certificate from its bytes representation
	 * @param cert	The bytes corresponding to the certificate
	 * @return	The X509Certificate
	 */
	public static X509Certificate recreateX509Certificate(byte[] cert) throws CriptoFrameworkException{
    	InputStream in = new ByteArrayInputStream(cert);
    	try {
			return (X509Certificate)CertificateFactory.getInstance("X.509").generateCertificate(in);
		} catch (CertificateException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	public static byte[] cipherWithSymKey(byte[] message, Key key) throws CriptoFrameworkException{
		try{
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(message);
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchPaddingException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeyException e) {
			throw new CriptoFrameworkException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CriptoFrameworkException(e);
		} catch (BadPaddingException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	public static byte[] decipherWithSymKey(byte[] message, Key key) throws CriptoFrameworkException{
		try{
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(message);
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchPaddingException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeyException e) {
			throw new CriptoFrameworkException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CriptoFrameworkException(e);
		} catch (BadPaddingException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	/**
	 * Ciphers with Java public key
	 * @param message	The message to cipher
	 * @param pubKey	The public key
	 * @return	The ciphered text
	 */
	public static byte[] cipherWithPublicKey(byte[] message, PublicKey pubKey) throws CriptoFrameworkException{
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			return cipher.doFinal(message);
//			byte[] result = new byte[0];
//			int i,j;
//			int size = message.length;
//			int curPos = 0;
//			while(size > 0){
//				int roundSize = (size > 100) ? 100 : size;
//				byte[] block = new byte[roundSize];
//				for(i = 0; i < roundSize; i++){
//					block[i] = message[curPos + i]; 
//				}
//				curPos += roundSize;
//				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//				cipher.init(Cipher.ENCRYPT_MODE, pubKey);
//				byte[] tmp = cipher.doFinal(block);
//				byte[] merge = new byte[result.length + tmp.length];
//				for(i = 0; i < result.length; i++){
//					merge[i] = result[i];
//				}
//				for(j = 0; j < tmp.length; j++, i++){
//					merge[i] = tmp[j];
//				}
//				size -= tmp.length;
//				result = merge;
//			}
//			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchPaddingException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeyException e) {
			throw new CriptoFrameworkException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CriptoFrameworkException(e);
		} catch (BadPaddingException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	public static byte[] cipherWithPrivateKey(byte[] message, PrivateKey privKey) throws CriptoFrameworkException{
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, privKey);
			return cipher.doFinal(message);
//			byte[] result = new byte[0];
//			int i,j;
//			int size = message.length;
//			int curPos = 0;
//			while(size > 0){
//				int roundSize = (size > 100) ? 100 : size;
//				byte[] block = new byte[roundSize];
//				for(i = 0; i < roundSize; i++){
//					block[i] = message[curPos + i]; 
//				}
//				curPos += roundSize;
//				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//				cipher.init(Cipher.ENCRYPT_MODE, privKey);
//				byte[] tmp = cipher.doFinal(block);
//				byte[] merge = new byte[result.length + tmp.length];
//				for(i = 0; i < result.length; i++){
//					merge[i] = result[i];
//				}
//				for(j = 0; j < tmp.length; j++, i++){
//					merge[i] = tmp[j];
//				}
//				size -= tmp.length;
//				result = merge;
//			}
//			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchPaddingException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeyException e) {
			throw new CriptoFrameworkException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CriptoFrameworkException(e);
		} catch (BadPaddingException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	public static byte[] decipherWithPublicKey(byte[] message, PublicKey pubKey) throws CriptoFrameworkException{
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, pubKey);
			return cipher.doFinal(message);
//			byte[] result = new byte[0];
//			int i,j;
//			int size = message.length;
//			int curPos = 0;
//			while(size > 0){
//				int roundSize = (size > 100) ? 100 : size;
//				byte[] block = new byte[roundSize];
//				for(i = 0; i < roundSize; i++){
//					block[i] = message[curPos + i]; 
//				}
//				curPos += roundSize;
//				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//				cipher.init(Cipher.DECRYPT_MODE, pubKey);
//				byte[] tmp = cipher.doFinal(block);
//				byte[] merge = new byte[result.length + tmp.length];
//				for(i = 0; i < result.length; i++){
//					merge[i] = result[i];
//				}
//				for(j = 0; j < tmp.length; j++, i++){
//					merge[i] = tmp[j];
//				}
//				size -= tmp.length;
//				result = merge;
//			}
//			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchPaddingException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeyException e) {
			throw new CriptoFrameworkException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CriptoFrameworkException(e);
		} catch (BadPaddingException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	public static byte[] decipherWithPrivateKey(byte[] message, PrivateKey privKey) throws CriptoFrameworkException{
		try {
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privKey);
			return cipher.doFinal(message);
//			byte[] result = new byte[0];
//			int i,j;
//			int size = message.length;
//			int curPos = 0;
//			System.out.println("Starting decipher. message length: " + size);
//			while(size > 0){
//				System.out.println("Left to decipher: " + size + "\nAlready done: " + result.length);
//				int roundSize = (size > 100) ? 100 : size;
//				byte[] block = new byte[roundSize];
//				for(i = 0; i < roundSize; i++){
//					block[i] = message[curPos + i]; 
//				}
//				System.out.println("Round size: " + roundSize);
//				curPos += roundSize;
//				Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
//				cipher.init(Cipher.DECRYPT_MODE, privKey);
//				byte[] tmp = cipher.doFinal(block);
//				byte[] merge = new byte[result.length + tmp.length];
//				System.out.println("Deciphered round block, moving " + tmp.length + " to the old " + result.length + " summing " + merge.length);
//				for(i = 0; i < result.length; i++){
//					merge[i] = result[i];
//				}
//				for(j = 0; j < tmp.length; j++, i++){
//					merge[i] = tmp[j];
//				}
//				size -= tmp.length;
//				result = merge;
//				System.out.println("Finished round with cur result " + result.length);
//			}
//			System.out.println("End");
//			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		} catch (NoSuchPaddingException e) {
			throw new CriptoFrameworkException(e);
		} catch (InvalidKeyException e) {
			throw new CriptoFrameworkException(e);
		} catch (IllegalBlockSizeException e) {
			throw new CriptoFrameworkException(e);
		} catch (BadPaddingException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
	public static byte[] computeDigest(byte[] ... messages){
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			for(byte[] message : messages){
				messageDigest.update(message);
			}
			return messageDigest.digest();
		} catch (NoSuchAlgorithmException e) {
			throw new CriptoFrameworkException(e);
		}
	}
	
}
