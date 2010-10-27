package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.story.registries.AliasRegistrySpi;


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
