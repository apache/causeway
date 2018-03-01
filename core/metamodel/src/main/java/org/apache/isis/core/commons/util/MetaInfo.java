package org.apache.isis.core.commons.util;

public class MetaInfo {

	public static boolean isPersistenceEnhanced(Class<?> cls) {
		return org.datanucleus.enhancement.Persistable.class.isAssignableFrom(cls);
	}

}
