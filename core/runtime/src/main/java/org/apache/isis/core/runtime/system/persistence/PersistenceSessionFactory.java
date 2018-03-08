package org.apache.isis.core.runtime.system.persistence;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.IsisJdoRuntimePlugin;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

public interface PersistenceSessionFactory {

	// -- INTERFACE

	PersistenceSession createPersistenceSession(ServicesInjector servicesInjector,
			AuthenticationSession authenticationSession);

	void init(SpecificationLoader specificationLoader);

	boolean isInitialized();
	
	void shutdown();
	
	// -- FACTORY

	static PersistenceSessionFactory of(ConfigurationServiceInternal configuration) {
		return IsisJdoRuntimePlugin.get().getPersistenceSessionFactory(configuration);
	}


}
