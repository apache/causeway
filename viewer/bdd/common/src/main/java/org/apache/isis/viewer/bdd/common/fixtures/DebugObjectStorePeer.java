package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStorePersistence;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;


public class DebugObjectStorePeer extends AbstractFixturePeer {

	public DebugObjectStorePeer(AliasRegistry aliasesRegistry,
			CellBinding... cellBindings) {
		super(aliasesRegistry, cellBindings);
	}

	public String debugObjectStore() {
        final ObjectStorePersistence objectStore = getObjectStore();
        final DebugString debug = new DebugString();
        objectStore.debugData(debug);
        return debug.toString().replaceAll("\n", "<br>");
	}

}
