package br.com.caelum.brutal.migration;

import java.math.BigInteger;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;

@ApplicationScoped
public class MigrationExecutor {

	private SessionFactory sf;
	private int currentMigration = -1;
	private Session session;
	private StatelessSession statelessSession;
	private DatabaseManager databaseManager;

	@Deprecated
	public MigrationExecutor() {
	}

	@Inject
	public MigrationExecutor(SessionFactory sf, DatabaseManager databaseManager) {
		this.sf = sf;
		this.databaseManager = databaseManager;
	}

	public void begin() {
		session = sf.openSession();
		statelessSession = sf.openStatelessSession();
		statelessSession.beginTransaction();
		session.beginTransaction();
	}

	public void end() {
		session.getTransaction().commit();
		statelessSession.getTransaction().commit();
	}

	public void rollback() {
		session.getTransaction().rollback();
	}
	
	public void rollback(SchemaMigration m) {
		rollback();
	}

	public void insertNewMigration(int lastMigration) {
		session.createSQLQuery("insert into brutalmigration(number) values(:last)").setInteger("last", lastMigration).executeUpdate();
	}

	public int currentMigration() {
		if (currentMigration == -1) {
			currentMigration = (int) session.createSQLQuery("select number from brutalmigration order by number desc limit 1").uniqueResult();
		}
		return currentMigration;
	}

	private void executeQueries(List<MigrationOperation> queries) {
		for (MigrationOperation rawSQLMigration : queries) {
			String sql = rawSQLMigration.execute();
			if (sql != null && !sql.isEmpty()) {
				session.createSQLQuery(sql).executeUpdate();
			}
		}
	}

	public void run(SchemaMigration m) {
		executeQueries(m.up());
	}

	public void prepareTables() {
		session.createSQLQuery("create table if not exists brutalmigration (number int(11))").executeUpdate();
		BigInteger result = (BigInteger) session.createSQLQuery("select count(*) from brutalmigration").uniqueResult();
		if (result.equals(BigInteger.ZERO)) {
			databaseManager.importAll("/db_structure.sql");
			session.createSQLQuery("insert into brutalmigration values(0)").executeUpdate();
		}
	}

	public void close() {
		session.close();
	}

}
