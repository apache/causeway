package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;

public class AbstractSetUpFixturePeer extends AbstractFixturePeer {

	public AbstractSetUpFixturePeer(AliasRegistry aliasRegistry,
			CellBinding... cellBindings) {
		super(aliasRegistry, cellBindings);
	}

}
