package registration.persistence;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import registration.domain.EligibleVoter;
import registration.exceptions.NotEligibleVoterException;
import registration.exceptions.PersistenceException;

public class RegistrationPersistence {
	
	private static SessionFactory sessionFactory;
	
	public static Session getSession() {
		if(sessionFactory == null) {
			sessionFactory = new Configuration().configure().buildSessionFactory();
		}
		return sessionFactory.openSession();
	}

	public static EligibleVoter getEligibleVoter(Long voterId){
		for(EligibleVoter v : getEligibleVoters()){
			if(v.getId().equals(voterId)){
				return v;	
			}
		}
		throw new NotEligibleVoterException("There is no voter with ID " + voterId);
	}
	
	@SuppressWarnings("unchecked")
	public static List<EligibleVoter> getEligibleVoters(){
		Session session = getSession();
		
		Transaction tx = null;
		List<EligibleVoter> voters = null;
		try {
			tx = session.beginTransaction();
			voters = (List<EligibleVoter>)session.createCriteria(EligibleVoter.class).list();
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new PersistenceException(ex);
		}
		session.disconnect();
		session.close();
		return voters;
	}
	
	public static void addEligibleVoter(EligibleVoter voter){
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(voter);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new PersistenceException(ex);
		}
		session.disconnect();
		session.close();
	}
	
	public static void updateEligibleVoter(EligibleVoter voter){
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(voter);
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new PersistenceException(ex);
		}
		session.disconnect();
		session.close();
	}
	
}
