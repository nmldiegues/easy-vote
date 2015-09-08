package trustedcenter.domain;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import sirs.framework.criptography.CriptoUtils;



@Entity
@Table(name="SERVER_DATA")
public class Server {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Version
	private Long version;
	private String distinguishedName;
	@Lob
	@Column(length=1048576)
	private byte[] publicKey;
	@Lob
	@Column(length=1048576)
	private byte[] privateKey; //only used in case of TC
	@Lob
	@Column(length=1048576)
	private byte[] cert;
	@Lob
	@Column(length=1048576)
	private byte[] blindedSignedToken;
	private Integer hasVoted;
	@Lob
	@Column(length=1048576)
	private byte[] sharedKey;
	
	public Server(){}
	
    public Long getId(){
    	return this.id;
    }
    
    public Long getVersion(){
    	return this.version;
    }
    
    public String getDistinguishedName(){
    	return this.distinguishedName;
    }
    
    public byte[] getPublicKey(){
    	return this.publicKey;
    }
    
    public byte[] getPrivateKey(){
    	return this.privateKey;
    }
    
    public byte[] getCert(){
    	return this.cert;
    }
    
	public void setId(Long id) {
		this.id = id;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
    
    public void setDistinguishedName(String distinguishedName){
    	this.distinguishedName = distinguishedName;
    }
    
    public void setPublicKey(byte[] publicKey){
    	this.publicKey = publicKey;
    }
    
    public void setPrivateKey(byte[] privateKey){
    	this.privateKey = privateKey;
    }

    public void setCert(byte[] cert){
    	this.cert = cert;
    }
    
    public X509Certificate getObjectCert(){
    	return CriptoUtils.recreateX509Certificate(this.cert);
    }
    
    public PublicKey getObjectPublicKey(){
    	return CriptoUtils.recreatePublicKey(this.publicKey);
    }
    
    public PrivateKey getObjectPrivateKey(){
    	return CriptoUtils.recreatePrivateKey(this.privateKey);
    }
    
    public void setObjectCert(X509Certificate cert){
    	try {
			this.cert = cert.getEncoded();
		} catch (CertificateEncodingException e) {
			e.printStackTrace();
		}
    }
    
    public void setObjectPublicKey(PublicKey publicKey){
    	this.publicKey = publicKey.getEncoded();
    }
    
    public void setObjectPrivateKey(PrivateKey privateKey){
    	this.privateKey = privateKey.getEncoded();
    }
    
    public byte[] getBlindedSignedToken(){
		return this.blindedSignedToken;
	}
	
	public boolean isHasVoted(){
		if(hasVoted == 0)
			return false;
		return true;
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
	
	public byte[] getSharedKey(){
		return this.sharedKey;
	}
	
	public SecretKey getObjectSharedKey(){
		return CriptoUtils.recreateAESKey(this.sharedKey);
	}
	
	public void setSharedKey(byte[] key){
		this.sharedKey = key;
	}
	
	public void setObjectSharedKey(SecretKey key){
		this.sharedKey = key.getEncoded();
	}
}
