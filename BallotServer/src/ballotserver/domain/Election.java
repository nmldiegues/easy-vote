package ballotserver.domain;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.Transactional;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.CandidateDoesNotExistException;
import ballotserver.persistence.BallotServerPersistence;

@Entity
@Table(name="ELECTION_DATA")
public class Election {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Version
	private Long version;
	@OneToMany(fetch=FetchType.EAGER)
    @JoinTable(
        name = "ListCandidates",
        joinColumns = @JoinColumn(name = "ElectionCode"),
        inverseJoinColumns = @JoinColumn(name = "CandidateId")
    )
    private Set<Candidate> candidates = new HashSet<Candidate>();
	@OneToMany(fetch=FetchType.EAGER)
    @JoinTable(
        name = "ListSheets",
        joinColumns = @JoinColumn(name = "BelongingElection"),
        inverseJoinColumns = @JoinColumn(name = "BallotSheetId")
    )
    private Set<BallotSheet> ballotSheets = new HashSet<BallotSheet>();
	private Integer started;
	private Integer closed;
	private String question;

	public Election() {
		this.started = 0;
		this.closed = 0;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}
	
	public boolean getStarted() {
		return (this.started == 0) ? false : true;
	}
	
	public void setStarted() {
		this.started = 1;
	}
	
	public boolean getClosed() {
		return (this.closed == 0) ? false : true;
	}
	
	public void setClosed() {
		this.closed = 1;
	}
	

	public void setVersion(Long version) {
		this.version = version;
	}

	public Set<Candidate> getCandidates() {
		return this.candidates;
	}

	public void setCandidates(Set<Candidate> candidates) {
		this.candidates = candidates;
	}

	public Set<BallotSheet> getBallotSheets() {
		return this.ballotSheets;
	}

	public void setBallotSheets(Set<BallotSheet> ballotSheets) {
		this.ballotSheets = ballotSheets;
	}
	
	public void addCandidate(Candidate candidate){
		this.candidates.add(candidate);
	}
	
	public void addBallotSheet(BallotSheet ballotSheet){
		this.ballotSheets.add(ballotSheet);
	}
	
	public List<Candidate> getCandidateList() {
		ArrayList<Candidate> sortedList = new ArrayList<Candidate>(getCandidates());
		Collections.sort(sortedList);
		return sortedList;
	}
	
	public Candidate getCandidate(String name, Long id) {
		for(Candidate c: getCandidates()) {
			if(c.getPersonName().equalsIgnoreCase(name) && c.getId().equals(id))
				return c;
		}
		throw new CandidateDoesNotExistException(id);

	}
	
	public void deleteCandidate(String name, Long id) {
		for(Candidate c: getCandidates()) {
			if(name.equals(name) && c.getId().equals(id)) {
				this.candidates.remove(c);
			}
		}
		throw new CandidateDoesNotExistException(id);
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}
}
