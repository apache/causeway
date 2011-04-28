package org.apache.isis.viewer.restful.viewer.util;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.AdapterManager;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

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
