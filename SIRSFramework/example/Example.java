import java.math.BigInteger;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;

import sirs.framework.criptography.CriptoUtils;


public class Example {
	/*
	 * Example of usage
	 */
	public static void main(String[] args) {
		AsymmetricCipherKeyPair bob_keyPair = CriptoUtils.generateKeys(1024);
		AsymmetricCipherKeyPair alice_keyPair = CriptoUtils.generateKeys(1024);

		try {
			byte[] msg = "OK".getBytes("UTF-8");

			//----------- Bob: Generating blinding factor based on Alice's public key -----------//
			BigInteger blindingFactor = CriptoUtils.generateBlindingFactor(alice_keyPair.getPublic());

			//----------------- Bob: Blinding message with Alice's public key -----------------//
			byte[] blinded_msg =
				CriptoUtils.blind(alice_keyPair.getPublic(), blindingFactor, msg);

			//------------- Bob: Signing blinded message with Bob's private key -------------//
			byte[] sig = CriptoUtils.sign(bob_keyPair.getPrivate(), blinded_msg);

			//------------- Alice: Verifying Bob's signature -------------//
			if (CriptoUtils.verify(bob_keyPair.getPublic(), blinded_msg, sig)) {

				//---------- Alice: Signing blinded message with Alice's private key ----------//
				byte[] sigByAlice =
					CriptoUtils.signBlinded(alice_keyPair.getPrivate(), blinded_msg);

				//------------------- Bob: Unblinding Alice's signature -------------------//
				byte[] unblindedSigByAlice =
					CriptoUtils.unblind(alice_keyPair.getPublic(), blindingFactor, sigByAlice);

				//---------------- Bob: Verifying Alice's unblinded signature ----------------//
				System.out.println(CriptoUtils.verify(alice_keyPair.getPublic(), msg,
						unblindedSigByAlice));
				// Now Bob has Alice's signature for the original message
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
