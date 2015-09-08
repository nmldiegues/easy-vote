package ballotserver.persistence;

import java.util.Arrays;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;


import ballotserver.domain.BallotSheet;
import ballotserver.domain.Candidate;
import ballotserver.domain.Election;
import ballotserver.exceptions.BallotServerException;
import ballotserver.exceptions.ElectionDoesNotExistException;
import ballotserver.exceptions.NoActiveElectionException;
import ballotserver.exceptions.NoSuchTokenPairException;

public class BallotServerPersistence {

	private static SessionFactory sessionFactory;
	
	public static Session getSession() {
		if(sessionFactory == null) {
			sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
		}
		return sessionFactory.openSession();
	}
	
	public static void addElection(Election election) throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(election);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
	}
	
	public static void deleteElection(Election election) throws BallotServerException {
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.delete(election);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
	}
	
	public static void updateElection(Election election) throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(election);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public static List<Election> getElections() throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		List<Election> elections = null;
		try {
			tx = session.beginTransaction();
			elections = (List<Election>)session.createCriteria(Election.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
		return elections;
	}
	
	public static Election getElection(Long electionId) throws BallotServerException{
		for(Election e : getElections())
			if(electionId.equals(e.getId()))
				return e;
		throw new ElectionDoesNotExistException(electionId);
	}
	
	public static Election getCurrentElection() throws BallotServerException{
		for(Election e : getElections()){
			if(e.getStarted() && !e.getClosed()){
				return e;
			}
		}
		throw new NoActiveElectionException();
	}
	
	public static BallotSheet getBallotSheet(byte[] regToken, byte[] tcToken){
		for(BallotSheet bs : getBallotSheets()){
			if(Arrays.equals(bs.getRegToken(), regToken) && Arrays.equals(bs.getTcToken(), tcToken)){
				return bs;
			}
		}
		throw new NoSuchTokenPairException();
	}
	
	public static void updateBallotSheet(BallotSheet ballot) throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(ballot);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public static void addBallotSheetToCurrentElection(BallotSheet ballot) throws BallotServerException {
		while(true){
			Session session = getSession();
			List<Election> elections = null;
			Election currentElection = null;

			Transaction tx = null;
			try {
				tx = session.beginTransaction();

				elections = (List<Election>)session.createCriteria(Election.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

				for(Election e : elections){
					if(e.getStarted() && !e.getClosed()){
						currentElection = e;
					}
				}

				if(currentElection == null){
					throw new NoActiveElectionException();
				}

				currentElection.addBallotSheet(ballot);
				session.update(currentElection);
				tx.commit();
			} catch (RuntimeException ex) {
				if (tx != null) tx.rollback();
				session.disconnect();
				session.close();
				if(ex instanceof org.hibernate.StaleObjectStateException){
					continue;
				}
				throw new BallotServerException(ex);
			}
			session.disconnect();
			session.close();
			break;
		}
	}
	
	public static void addBallotSheet(BallotSheet ballot) throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(ballot);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
	}
	
	@SuppressWarnings("unchecked")
	public static List<BallotSheet> getBallotSheets() throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		List<BallotSheet> ballotSheets = null;
		try {
			tx = session.beginTransaction();
			ballotSheets = (List<BallotSheet>)session.createCriteria(BallotSheet.class).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
		return ballotSheets;
	}
	
	public static void addCandidate(Candidate candidate) throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(candidate);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
	}
	
	public static void updateCandidate(Candidate candidate) throws BallotServerException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(candidate);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new BallotServerException(ex);
		}
		session.disconnect();
		session.close();
	}
}
