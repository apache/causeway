package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.fixtures.AliasServicesPeer;

public class AliasServicesForConcordion extends AbstractFixture<AliasServicesPeer> {

    public AliasServicesForConcordion(final AliasRegistry aliasesRegistry) {
    	super(new AliasServicesPeer(aliasesRegistry));
    }

    public String execute(final String aliasAs, final String className) {
    	try {
			getPeer().aliasService(className, aliasAs);
			return "ok";
		} catch (StoryValueException e) {
			return e.getMessage();
		}
    }

}
