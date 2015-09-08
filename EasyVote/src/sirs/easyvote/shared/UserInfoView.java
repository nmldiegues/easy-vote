package sirs.easyvote.shared;

import java.io.Serializable;

public class UserInfoView implements Serializable {
	private static final long serialVersionUID = 9945678439L;
	private Boolean hasVoted;
	private Boolean isAuditing;
	
	public UserInfoView() {}
	
	public UserInfoView(Boolean hasVoted, Boolean auditing) {
		this.hasVoted = hasVoted;
		this.isAuditing = auditing;
	}

	public void setIsAuditing(Boolean isAuditing) {
		this.isAuditing = isAuditing;
	}

	public void setHasVoted(Boolean hasVoted) {
		this.hasVoted = hasVoted;
	}

	public Boolean getHasVoted() {
		return hasVoted;
	}

	public Boolean getIsAuditing() {
		return isAuditing;
	}
}
