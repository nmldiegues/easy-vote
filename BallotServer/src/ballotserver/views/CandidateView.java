package ballotserver.views;

import java.io.Serializable;

public class CandidateView implements Serializable{

	private static final long serialVersionUID = 6521867096492351693L;
	private String name;
	private Long uniqueIdentifier;

	public CandidateView(){}
	
	public CandidateView(String name, Long uniqueIdentifier){
		this.name = name;
		this.uniqueIdentifier = uniqueIdentifier;
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
