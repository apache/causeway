package org.apache.isis.viewer.restful.viewer.util;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.PersistenceSession;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;

public final class OidUtils {
	
	
	private OidUtils(){}

	public static ObjectAdapter getNakedObject(
			final String oidEncodedStr,
			final OidStringifier oidStringifier) {
		String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);
		final Oid oid = oidStringifier.deString(oidStr);
	    return getAdapterManager().getAdapterFor(oid);
	}

	public static String getOidStr(
			final ObjectAdapter nakedObject,
			final OidStringifier oidStringifier) {
		final Oid oid = nakedObject.getOid();
		return oid != null ? oidStringifier.enString(oid) : null;
	}
	
    ////////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    ////////////////////////////////////////////////////////////////
    
	public static AdapterManager getAdapterManager() {
		return getPersistenceSession().getAdapterManager();
	}

	protected static PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}


}
