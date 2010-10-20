package org.apache.isis.extensions.bdd.common.fixtures;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.CellBinding;
import org.apache.isis.runtime.persistence.objectstore.ObjectStorePersistence;


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
