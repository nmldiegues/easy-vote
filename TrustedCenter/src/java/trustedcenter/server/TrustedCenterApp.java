package trustedcenter.server;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;

import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import sirs.framework.criptography.CriptoUtils;
import trustedcenter.domain.Server;
import trustedcenter.exceptions.CertificateDoesNotExistException;
import trustedcenter.exceptions.ErrorGeneratingCertificateException;
import trustedcenter.exceptions.InvalidCertificateException;
import trustedcenter.exceptions.LackingServerDataException;
import trustedcenter.exceptions.TrustedCenterException;
import trustedcenter.persistence.TrustedCenterPersistence;



public class TrustedCenterApp {
	
	private PublicKey tcPubKey;
	private PrivateKey tcPrivKey;
	private BigInteger tcCertSerialNumber;
	
	public TrustedCenterApp(){
		Security.addProvider(new BouncyCastleProvider());
		try{
			Server tc = TrustedCenterPersistence.getTrustedServer("TrustedCenter");
			this.tcPubKey = tc.getObjectCert().getPublicKey();
			this.tcPrivKey = tc.getObjectPrivateKey();
			this.tcCertSerialNumber = tc.getObjectCert().getSerialNumber();
		}
		catch(TrustedCenterException e){
			//generate keypair and store it with a certificate
			KeyPair keys = CriptoUtils.generateJavaKeys(1024);
			Server server = new Server();
			server.setDistinguishedName("TrustedCenter");
			server.setObjectPublicKey(keys.getPublic());
			this.tcPubKey = keys.getPublic();
			this.tcPrivKey = keys.getPrivate();
			server.setObjectPrivateKey(keys.getPrivate());
			TrustedCenterPersistence.addTrustedServer(server);
			server = TrustedCenterPersistence.getTrustedServer("TrustedCenter");
			this.tcCertSerialNumber = generateCertificate("TrustedCenter", tcPubKey).getSerialNumber();
		}
	}
	
	public PublicKey getTrustedServerKey(){
		return this.tcPubKey;
	}
	
	public PrivateKey getTrustedServerPrivateKey(){
		return this.tcPrivKey;
	}
	
	public X509Certificate generateCertificate(String distinguishedName, PublicKey publicKey) 
	throws TrustedCenterException{
		Server server = null;
		try{
			server = TrustedCenterPersistence.getTrustedServer(distinguishedName);
		} catch(TrustedCenterException e){
			server = new Server();
			server.setDistinguishedName(distinguishedName);
			server.setObjectPublicKey(publicKey);
			server.setHasVoted(false);
			TrustedCenterPersistence.addTrustedServer(server);
		}
		try {
			if(server == null || server.getDistinguishedName() == null || server.getPublicKey() == null) {
				throw new LackingServerDataException();
			}
			
			X509V3CertificateGenerator generator = new X509V3CertificateGenerator();
			generator.setSerialNumber(new BigInteger(String.valueOf(System.currentTimeMillis())/*.concat(String.valueOf(new SecureRandom().nextLong()))*/));
			if(distinguishedName.equals("TrustedCenter")){
				generator.setIssuerDN(new X500Principal("CN=TrustedCenter Certification Authority")); 
				generator.setSubjectDN(new X500Principal("CN=TrustedCenter Certification Authority")); 
				generator.setPublicKey(TrustedCenterPersistence.getTrustedCenterPublicKey()); 
				generator.setSignatureAlgorithm("SHA1with"+TrustedCenterPersistence.getTrustedCenterPublicKey().getAlgorithm());
			}
			else{
				generator.setIssuerDN(TrustedCenterPersistence.getTrustedCenterCertificate().getSubjectX500Principal());
				generator.setSubjectDN(new X500Principal("CN=" + distinguishedName)); 
				generator.setPublicKey(publicKey); 
				generator.setSignatureAlgorithm("SHA1with" + TrustedCenterPersistence.getTrustedCenterCertificate().getPublicKey().getAlgorithm());	
			}

			Calendar date = Calendar.getInstance(); 
			generator.setNotBefore(date.getTime()); 
			date.add(Calendar.DAY_OF_YEAR, 365); 
			generator.setNotAfter(date.getTime()); 

			X509Certificate certificate = generator.generate(TrustedCenterPersistence.getTrustedCenterPrivateKey());
			server.setObjectCert(certificate);
			
			TrustedCenterPersistence.updateServer(server);
			
			return certificate;
		}
		catch (GeneralSecurityException e) {
			throw new ErrorGeneratingCertificateException(e);
		}
	}
	
	private boolean validateCertificate(X509Certificate cert){
		return true;
	}
	
	public PublicKey getPublicKey(BigInteger serialNumber) throws TrustedCenterException{
		for(Server s :TrustedCenterPersistence.getTrustedServers()){
			if(s.getObjectCert().getSerialNumber().equals(serialNumber)){
				if(!validateCertificate(s.getObjectCert()))
					throw new InvalidCertificateException(s.getDistinguishedName(), serialNumber);				
				return s.getObjectCert().getPublicKey();
			}
		}
		throw new CertificateDoesNotExistException(serialNumber.toString());
	}
	
	public PublicKey getPublicKey(String distinguishedName) throws TrustedCenterException{
		for(Server s :TrustedCenterPersistence.getTrustedServers()){
			if(s.getDistinguishedName().equals(distinguishedName)){
				if(!validateCertificate(s.getObjectCert()))
					throw new InvalidCertificateException(distinguishedName, s.getObjectCert().getSerialNumber());				
				return s.getObjectCert().getPublicKey();
			}
		}
		throw new CertificateDoesNotExistException(distinguishedName);
	}	
	
	public String getDistinguishedName(BigInteger serialNumber) throws TrustedCenterException{
		for(Server s :TrustedCenterPersistence.getTrustedServers()){
			if(s.getObjectCert().getSerialNumber().equals(serialNumber)){
				if(!validateCertificate(s.getObjectCert()))
					throw new InvalidCertificateException(s.getDistinguishedName(), serialNumber);				
				return s.getDistinguishedName();
			}
		}
		throw new CertificateDoesNotExistException(serialNumber.toString());
	}
	
	public boolean hasUserRequestedToken(Long voterId){
		Server voter = TrustedCenterPersistence.getTrustedServer("voter"+voterId);
		if(voter.isHasVoted()){
			return false;
		}
		else{
			return true;
		}
	}
	
	public byte[] getRegisteredToken(Long voterId){
		 return TrustedCenterPersistence.getTrustedServer("voter"+voterId).getBlindedSignedToken();
	}
	
	public byte[] blindSignature(byte[] text){
		RSAKeyParameters regPrivKey = CriptoUtils.privateKeyToCipherParameters(this.tcPrivKey);
		return CriptoUtils.signBlinded(regPrivKey, text);
	}
	
	public void registerSignedToken(Long voterId, byte[] signedToken){
		Server voter = TrustedCenterPersistence.getTrustedServer("voter"+voterId);
		voter.setBlindedSignedToken(signedToken);
		voter.setHasVoted(true);
		TrustedCenterPersistence.updateServer(voter);
	}
	
	public BigInteger getSerialNumber(){
		return this.tcCertSerialNumber;
	}
	
	public void registerSharedKey(Long voterId, SecretKey key){
		Server voter = TrustedCenterPersistence.getTrustedServer("voter"+voterId);
		voter.setObjectSharedKey(key);
		TrustedCenterPersistence.updateServer(voter);
	}
	
	public SecretKey getSharedKey(Long voterId){
		return TrustedCenterPersistence.getTrustedServer("voter"+voterId).getObjectSharedKey();
	}
}
