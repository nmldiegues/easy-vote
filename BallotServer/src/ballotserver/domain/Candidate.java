package ballotserver.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="CANDIDATE_DATA")
public class Candidate implements Comparable<Candidate> {
	@Id
	private Long id;
	@Version
	private Long version;
	private String personName;

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

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	@Override
	public int compareTo(Candidate anotherCandidate) {
		return this.personName.compareTo(anotherCandidate.personName);
	}
	
}
