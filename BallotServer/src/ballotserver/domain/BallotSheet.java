package ballotserver.domain;

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
@Table(name="BALLOT_SHEET_DATA")
public class BallotSheet {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Version
	private Long version;
	private int chosenSquare;
	@Lob
	@Column(length=1048576)
	private byte[] safeOffset;
	@Lob
	@Column(length=1048576)
	private byte[] regToken;
	@Lob
	@Column(length=1048576)
	private byte[] tcToken;
	private int k;
	@Lob
	@Column(length=1048576)
	private byte[] sharedKey;
	private String ballotServerGenerator;
	private Integer auditing;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public int getChosenSquare() {
		return chosenSquare;
	}
	public void setChosenSquare(int chosenSquare) {
		this.chosenSquare = chosenSquare;
	}
	public byte[] getSafeOffset() {
		return safeOffset;
	}
	public void setSafeOffset(byte[] safeOffset) {
		this.safeOffset = safeOffset;
	}
	public byte[] getRegToken() {
		return regToken;
	}
	public void setRegToken(byte[] regToken) {
		this.regToken = regToken;
	}
	public byte[] getTcToken() {
		return tcToken;
	}
	public void setTcToken(byte[] tcToken) {
		this.tcToken = tcToken;
	}
	public int getK() {
		return k;
	}
	public void setK(int k) {
		this.k = k;
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
	public String getBallotServerGenerator() {
		return ballotServerGenerator;
	}
	public void setBallotServerGenerator(String ballotServerGenerator) {
		this.ballotServerGenerator = ballotServerGenerator;
	}
	public boolean getAuditing() {
		return (auditing == 0) ? false : true;
	}
	public void setAuditing(boolean auditing) {
		if(auditing)
			this.auditing = 1;
		else
			this.auditing = 0;
	}
}
