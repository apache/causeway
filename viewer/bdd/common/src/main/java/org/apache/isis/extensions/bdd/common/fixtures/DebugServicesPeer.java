package org.apache.isis.extensions.bdd.common.fixtures;

import java.util.List;

import org.apache.isis.commons.debug.DebugString;
import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.CellBinding;



public class DebugServicesPeer extends AbstractFixturePeer {
	
	public DebugServicesPeer(AliasRegistry aliasesRegistry,
			CellBinding... cellBindings) {
		super(aliasesRegistry, cellBindings);
	}

	public String debugServices() {
        final DebugString debug = new DebugString();

        final List<Object> services = getServices();

        for (final Object service : services) {
            debug.append(service.getClass().getName());
            debug.append("\n");
        }
        return debug.toString().replaceAll("\n", "<br>");
	}

}
