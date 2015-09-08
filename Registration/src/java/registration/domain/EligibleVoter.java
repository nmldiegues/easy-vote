package registration.domain;

import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import registration.exceptions.RegistrationException;
import sirs.framework.criptography.CriptoUtils;

@Entity
@Table(name="ELIGIBLE_VOTER_DATA")
public class EligibleVoter {
	@Id
	private Long id;
	@Version
	private Long version;
	@Lob
	@Column(length=1048576)
	private byte[] credentialsHashedWithSalt;
	@Lob
	@Column(length=1048576)
	private byte[] salt;
	@Lob
	@Column(length=1048576)
	private byte[] blindedSignedToken;
	private Integer hasVoted;
	@Lob
	@Column(length=1048576)
	private byte[] sharedKey;
	
	public EligibleVoter(){}

	public Long getId(){
		return this.id;
	}

	public Long getVersion(){
		return this.version;
	}

	public byte[] getBlindedSignedToken(){
		return this.blindedSignedToken;
	}
	
	public boolean isHasVoted(){
		if(hasVoted == 0)
			return false;
		return true;
	}
	
	public String getCredentials(){
		throw new RegistrationException("Cannot retrieve voter credentials");
	}
	
	public boolean verifyCredentials(String credentials){
		return Arrays.equals(this.credentialsHashedWithSalt,
				CriptoUtils.computeDigest(credentials.getBytes(), salt));
	}
	
	public byte[] getSharedKey(){
		return this.sharedKey;
	}
	
	public SecretKey getObjectSharedKey(){
		return CriptoUtils.recreateAESKey(this.sharedKey);
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	
	public void setBlindedSignedToken(byte[] blindedSignedToken){
		this.blindedSignedToken = blindedSignedToken;
	}
	
	public void setHasVoted(Boolean val){
		if(val)
			this.hasVoted = 1;
		else
			this.hasVoted = 0;
	}
	
	public void setCredentials(String credentials){
		if(this.salt == null){
			throw new RegistrationException("Setting password without any salt defined");
		}
		credentialsHashedWithSalt = CriptoUtils.computeDigest(
				credentials.getBytes(), this.salt);
	}
	
	public void setSharedKey(byte[] key){
		this.sharedKey = key;
	}
	
	public void setObjectSharedKey(SecretKey key){
		this.sharedKey = key.getEncoded();
	}

	public byte[] getSalt() {
		throw new RegistrationException("Cannot retrieve voter salt");
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}
}
