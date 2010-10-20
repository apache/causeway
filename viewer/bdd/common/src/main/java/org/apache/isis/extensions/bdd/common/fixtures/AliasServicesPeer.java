package org.apache.isis.extensions.bdd.common.fixtures;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.StoryValueException;
import org.apache.isis.extensions.bdd.common.story.registries.AliasRegistrySpi;


public class AliasServicesPeer extends AbstractSetUpFixturePeer {
    
	private AliasRegistrySpi aliasRegistrySpi;

	public AliasServicesPeer(AliasRegistry aliasesRegistry) {
        super(aliasesRegistry);
        this.aliasRegistrySpi = (AliasRegistrySpi) aliasesRegistry;
	}

	public void aliasService(String className, String aliasAs) throws StoryValueException {
		aliasRegistrySpi.aliasService(aliasAs, className);
	}

}
