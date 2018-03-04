package org.apache.isis.core.metamodel;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.internal.context._Plugin;

public interface IsisJdoMetamodelPlugin {

	// -- INTERFACE
	
	/**
	 * Equivalent to org.datanucleus.enhancement.Persistable.class.isAssignableFrom(cls).
	 * @param cls
	 * @return
	 */
	public boolean isPersistenceEnhanced(@Nullable Class<?> cls);
	
	/**
	 * Equivalent to org.datanucleus.enhancement.Persistable.class.getDeclaredMethods().
	 * @return
	 */
	public Method[] getMethodsProvidedByEnhancement();
	
	// -- LOOKUP

	public static IsisJdoMetamodelPlugin get() {
		return _Plugin.getOrElse(IsisJdoMetamodelPlugin.class, 
				ambigousPlugins->{
					throw new NonRecoverableException("Ambigous plugins implementing IsisJdoMetamodelPlugin found on class path.");
				}, 
				()->{
					throw new NonRecoverableException("No plugin implementing IsisJdoMetamodelPlugin found on class path.");
				}); 
	}
	
}
