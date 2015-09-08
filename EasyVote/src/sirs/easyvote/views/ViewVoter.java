package sirs.easyvote.views;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyPair;
import java.util.UUID;

import javax.crypto.SecretKey;

import ballotserver.views.BallotSheetView;

public class ViewVoter implements Serializable {

	private static final long serialVersionUID = 6492793603795737783L;

	private UUID regToken;
	private UUID tcToken;
	private BigInteger regBlindingFactor;
	private BigInteger tcBlindingFactor;
	private byte[] regBlindedToken;
	private byte[] tcBlindedToken;
	private byte[] regBlindedSignedToken;
	private byte[] tcBlindedSignedToken;
	private byte[] regSignedToken;
	private byte[] tcSignedToken;
	private BigInteger voterSerialNumber;
	private KeyPair voterKeys;
	private Long voterId;
	private String password;
	private String ballotServerEndpoint;
	private String ballotServerName;
	private BallotSheetView ballotSheetView;
	private Integer chosenSquare;
	private SecretKey sharedKey;
	private Boolean auditing;
	private Integer auditingOffset;

	public ViewVoter(){
	}
	
	public ViewVoter(Long voterId, String password){
		this.voterId = voterId;
		this.password = password;
	}

	public Long getVoterId(){
		return voterId;
	}
	
	public String getPassword(){
		return password;
	}
	
	public UUID getRegToken() {
		return regToken;
	}

	public void setRegToken(UUID regToken) {
		this.regToken = regToken;
	}

	public UUID getTcToken() {
		return tcToken;
	}
	
	public byte[] getRegBlindedToken(){
		return regBlindedToken;
	}
	
	public byte[] getTcBlindedToken(){
		return tcBlindedToken;
	}

	public void setTcToken(UUID tcToken) {
		this.tcToken = tcToken;
	}

	public BigInteger getRegBlindingFactor() {
		return regBlindingFactor;
	}

	public void setRegBlindingFactor(BigInteger regBlindingFactor) {
		this.regBlindingFactor = regBlindingFactor;
	}

	public BigInteger getTcBlindingFactor() {
		return tcBlindingFactor;
	}

	public void setTcBlindingFactor(BigInteger tcBlindingFactor) {
		this.tcBlindingFactor = tcBlindingFactor;
	}

	public byte[] getRegBlindedSignedToken() {
		return regBlindedSignedToken;
	}

	public void setRegBlindedSignedToken(byte[] regBlindedSignedToken) {
		this.regBlindedSignedToken = regBlindedSignedToken;
	}

	public byte[] getTcBlindedSignedToken() {
		return tcBlindedSignedToken;
	}

	public void setTcBlindedSignedToken(byte[] tcBlindedSignedToken) {
		this.tcBlindedSignedToken = tcBlindedSignedToken;
	}

	public BigInteger getVoterSerialNumber() {
		return voterSerialNumber;
	}

	public void setVoterSerialNumber(BigInteger voterSerialNumber) {
		this.voterSerialNumber = voterSerialNumber;
	}

	public KeyPair getVoterKeys() {
		return voterKeys;
	}

	public void setVoterKeys(KeyPair voterKeys) {
		this.voterKeys = voterKeys;
	}

	public void setRegBlindedToken(byte[] regBlindedToken){
		this.regBlindedToken = regBlindedToken;
	}
	
	public void setTcBlindedToken(byte[] tcBlindedToken){
		this.tcBlindedToken = tcBlindedToken;
	}

	public byte[] getRegSignedToken() {
		return regSignedToken;
	}

	public void setRegSignedToken(byte[] regSignedToken) {
		this.regSignedToken = regSignedToken;
	}

	public byte[] getTcSignedToken() {
		return tcSignedToken;
	}

	public void setTcSignedToken(byte[] tcSignedToken) {
		this.tcSignedToken = tcSignedToken;
	}

	public String getBallotServerEndpoint() {
		return ballotServerEndpoint;
	}

	public void setBallotServerEndpoint(String ballotServerEndpoint) {
		this.ballotServerEndpoint = ballotServerEndpoint;
	}

	public String getBallotServerName() {
		return ballotServerName;
	}

	public void setBallotServerName(String ballotServerName) {
		this.ballotServerName = ballotServerName;
	}

	public BallotSheetView getBallotSheetView() {
		return ballotSheetView;
	}

	public void setBallotSheetView(BallotSheetView ballotSheetView) {
		this.ballotSheetView = ballotSheetView;
	}

	public Integer getChosenSquare() {
		return chosenSquare;
	}

	public void setChosenSquare(Integer chosenSquare) {
		this.chosenSquare = chosenSquare;
	}

	public SecretKey getSharedKey() {
		return sharedKey;
	}

	public void setSharedKey(SecretKey sharedKey) {
		this.sharedKey = sharedKey;
	}

	public Boolean getAuditing() {
		return auditing;
	}

	public void setAuditing(Boolean auditing) {
		this.auditing = auditing;
	}

	public Integer getAuditingOffset() {
		return auditingOffset;
	}

	public void setAuditingOffset(Integer auditingOffset) {
		this.auditingOffset = auditingOffset;
	}
		
}
