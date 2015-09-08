package trustedcenter.persistence;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import trustedcenter.domain.Server;
import trustedcenter.exceptions.DoesNotHaveCertificateException;
import trustedcenter.exceptions.PersistenceException;
import trustedcenter.exceptions.TrustedCenterException;

public class TrustedCenterPersistence {
	
	private static SessionFactory sessionFactory;
	
	public static Session getSession() {
		if(sessionFactory == null) {
			sessionFactory = new Configuration().configure().buildSessionFactory();
		}
		return sessionFactory.openSession();
	}

	public static Server getTrustedServer(String distinguishedName) throws TrustedCenterException{
		for(Server s : TrustedCenterPersistence.getTrustedServers())
			if(distinguishedName.equals(s.getDistinguishedName()))
				return s;
		throw new DoesNotHaveCertificateException(distinguishedName);
	}
	
	public static void updateServer(Server server) throws TrustedCenterException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.update(server);
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
	
	@SuppressWarnings("unchecked")
	public static List<Server> getTrustedServers() throws TrustedCenterException{
		Session session = getSession();
		
		Transaction tx = null;
		List<Server> servers = null;
		try {
			tx = session.beginTransaction();
			servers = (List<Server>)session.createCriteria(Server.class).list();
			tx.commit();
		} catch (RuntimeException ex) {
			if (tx != null) tx.rollback();
			session.disconnect();
			session.close();
			throw new PersistenceException(ex);
		}
		session.disconnect();
		session.close();
		return servers;
	}
	
	public static void addTrustedServer(Server trustedServer) throws TrustedCenterException{
		Session session = getSession();
		
		Transaction tx = null;
		try {
			tx = session.beginTransaction();
			session.save(trustedServer);
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
	
	public static X509Certificate getTrustedCenterCertificate(){
		return getTrustedServer("TrustedCenter").getObjectCert();
	}
	
	public static PublicKey getTrustedCenterPublicKey(){
		return getTrustedServer("TrustedCenter").getObjectPublicKey();
	}
	
	public static PrivateKey getTrustedCenterPrivateKey(){
		return getTrustedServer("TrustedCenter").getObjectPrivateKey();
	}
	
}
