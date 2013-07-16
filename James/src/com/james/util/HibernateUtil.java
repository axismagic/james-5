package com.james.util;

import java.io.File;

import org.apache.log4j.Logger;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
public class HibernateUtil {
	private static SessionFactory sessionFactory;
	private static Logger log = Logger.getLogger(HibernateUtil.class);

	static {
		try {
			File configFile = new File("hibernate.cfg.xml");
			log.info(configFile.getAbsolutePath());
			Configuration hibernate_configuration = new Configuration().configure(configFile);
			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(
					hibernate_configuration.getProperties()).buildServiceRegistry();
			sessionFactory = hibernate_configuration.buildSessionFactory(serviceRegistry);

		} catch (Throwable ex) {
			log.error(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null)

			try {
				Configuration hibernate_configuration = new Configuration()
						.configure("hibernate.cfg.xml");
				ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(
						hibernate_configuration.getProperties()).buildServiceRegistry();
				sessionFactory = hibernate_configuration.buildSessionFactory(serviceRegistry);

			} catch (Exception ex) {
				log.error(ex);
			}

		return sessionFactory;
	}

	public static void main(String[] args) {
		getSessionFactory().getCurrentSession();
	}

}