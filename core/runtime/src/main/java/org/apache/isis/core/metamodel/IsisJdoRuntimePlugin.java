package org.apache.isis.core.metamodel;

import org.apache.isis.commons.internal.context._Plugin;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.runtime.system.persistence.PersistenceSessionFactory;

public interface IsisJdoRuntimePlugin {

	// -- INTERFACE
	
	public PersistenceSessionFactory getPersistenceSessionFactory(ConfigurationServiceInternal configuration);
	
	// -- LOOKUP

	public static IsisJdoRuntimePlugin get() {
		return _Plugin.getOrElse(IsisJdoRuntimePlugin.class, 
				ambiguousPlugins->{
					throw _Plugin.ambiguityNonRecoverable(IsisJdoRuntimePlugin.class, ambiguousPlugins); 
				}, 
				()->{
					throw _Plugin.absenceNonRecoverable(IsisJdoRuntimePlugin.class);
				}); 
	}

	
	
}
