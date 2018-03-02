package org.apache.isis.core.metamodel;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.internal.context._Plugin;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

public interface IsisJdoRuntimePlugin {

	// -- INTERFACE
	
	public PersistenceSessionFactory getPersistenceSessionFactory(ConfigurationServiceInternal configuration);
	
	// -- LOOKUP

	public static IsisJdoRuntimePlugin get() {
		return _Plugin.getOrElse(IsisJdoRuntimePlugin.class, 
				ambigousPlugins->{
					throw new NonRecoverableException("Ambigous plugins implementing IsisJdoRuntimePlugin found on class path.");
				}, 
				()->{
					throw new NonRecoverableException("No plugin implementing IsisJdoRuntimePlugin found on class path.");
				}); 
	}

	
	
}
