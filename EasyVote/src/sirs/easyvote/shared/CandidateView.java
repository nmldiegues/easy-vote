package sirs.easyvote.shared;

import java.io.Serializable;

public class CandidateView implements Serializable{
	private static final long serialVersionUID = 1489300451937360477L;
	private String name;
	private Long uniqueIdentifier;

	public CandidateView(){}
	
	public CandidateView(String name, Long uid) {
		this.name = name;
		this.uniqueIdentifier = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	public void setUniqueIdentifier(Long uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}	
}