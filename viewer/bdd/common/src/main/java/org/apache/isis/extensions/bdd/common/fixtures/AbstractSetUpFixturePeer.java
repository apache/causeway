package org.apache.isis.extensions.bdd.common.fixtures;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.CellBinding;

public class AbstractSetUpFixturePeer extends AbstractFixturePeer {

	public AbstractSetUpFixturePeer(AliasRegistry aliasRegistry,
			CellBinding... cellBindings) {
		super(aliasRegistry, cellBindings);
	}

}
