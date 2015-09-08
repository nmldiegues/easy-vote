package registration.server;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Properties;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.bouncycastle.crypto.params.RSAKeyParameters;

import registration.domain.EligibleVoter;
import registration.exceptions.ErrorCreatingRegCertException;
import registration.exceptions.ErrorRetreivingTcKeyException;
import registration.exceptions.InvalidVoterCredentialsException;
import registration.exceptions.RegistrationException;
import registration.persistence.RegistrationPersistence;
import sirs.framework.config.Config;
import sirs.framework.criptography.CriptoUtils;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.ws.client.service.GenerateCertificateService;
import trustedcenter.ws.client.service.GetPublicKeyService;

public class RegistrationApp {

	private BigInteger serialNumber;
	private KeyPair regKeys;
	private PublicKey tcPubKey;

	public RegistrationApp(){
		//Note that acquiring TC key and generating the certificate, should be done in a secure
		//fashion, most likely within physical presence to prove our identity for issuing of a certificate

		//generate keypair and request a certificate for it from the TrustedCenter
		this.regKeys = CriptoUtils.generateJavaKeys(1024);
		
		//but first, get TC key
		GetPublicKeyService keyRequest = new GetPublicKeyService(
				Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"));
		
		try {
			this.tcPubKey = keyRequest.execute();
		} catch(TrustedCenterException e) {
			throw new ErrorRetreivingTcKeyException(e.getMessage());
		} 
		

		//requesting a certificate creation now
		GenerateCertificateService request = new GenerateCertificateService(
				Config.getInstance().getInitParameter("trustedcenter.ws.EndpointAddress"),
				"Registration",
				this.regKeys.getPublic());
			
		try {
			this.serialNumber = request.execute();
		} catch(TrustedCenterException e) {
			throw new ErrorCreatingRegCertException(e.getMessage());
		}
		
		
		//read voter file and update persistence if needed
		Properties voters = Config.getInstance().getInitProperties();
		for(Object propertyKey : voters.keySet()){
			String possibleVoter = (String)propertyKey;
			String password = null;
			Long voterId = null;
			if(possibleVoter.contains("voter")){
				voterId = new Long(possibleVoter.substring("voter".length()));
				password = voters.getProperty(possibleVoter);
				EligibleVoter v;
				try{
					v = RegistrationPersistence.getEligibleVoter(voterId);
				} catch(RegistrationException e){
					v = new EligibleVoter();
					v.setId(voterId);
					v.setSalt(UUID.randomUUID().toString().getBytes());
					v.setCredentials(password);
					v.setHasVoted(false);
					RegistrationPersistence.addEligibleVoter(v);
				}
			}
		}
	}

	public KeyPair getRegKeys(){
		return this.regKeys;
	}

	public BigInteger getSerialNumber(){
		return this.serialNumber;
	}
	
	public PublicKey getTCPubKey(){
		return this.tcPubKey;
	}

	public byte[] blindSignature(byte[] text){
		RSAKeyParameters regPrivKey = CriptoUtils.privateKeyToCipherParameters(regKeys.getPrivate());
		return CriptoUtils.signBlinded(regPrivKey, text);
	}

	public boolean validateVoterAttempt(Long voterId, String credentials){
		EligibleVoter voter = RegistrationPersistence.getEligibleVoter(voterId);
		if(voter.getId().equals(voterId) && voter.verifyCredentials(credentials)){
			if(voter.isHasVoted()){
				return false;
			}
			else{
				return true;
			}
		}
		else{
			throw new InvalidVoterCredentialsException("Invalid credentials for " + voterId);
		}
	}
	
	public byte[] getRegisteredToken(Long voterId){
		 return RegistrationPersistence.getEligibleVoter(voterId).getBlindedSignedToken();
	}
	
	public void registerSignedToken(Long voterId, byte[] signedToken){
		EligibleVoter voter = RegistrationPersistence.getEligibleVoter(voterId);
		voter.setBlindedSignedToken(signedToken);
		voter.setHasVoted(true);
		RegistrationPersistence.updateEligibleVoter(voter);
	}

	public void registerSharedKey(Long voterId, String credentials, SecretKey key){
		EligibleVoter voter = RegistrationPersistence.getEligibleVoter(voterId);
		if(voter.getId().equals(voterId) && voter.verifyCredentials(credentials)){
			voter.setObjectSharedKey(key);
			RegistrationPersistence.updateEligibleVoter(voter);
		}
		else{
			throw new InvalidVoterCredentialsException("Invalid credentials for " + voterId);
		}
	}
	
	public SecretKey getSharedKey(Long voterId){
		return RegistrationPersistence.getEligibleVoter(voterId).getObjectSharedKey();
	}
	
	public int getNumberVotersAllowed(){
		return RegistrationPersistence.getEligibleVoters().size();
	}
}
