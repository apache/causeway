package org.apache.isis.extensions.bdd.fitnesse.internal.fixtures;

import org.apache.isis.extensions.bdd.common.AliasRegistry;
import org.apache.isis.extensions.bdd.common.StoryValueException;
import org.apache.isis.extensions.bdd.common.fixtures.AliasServicesPeer;
import org.apache.isis.extensions.bdd.fitnesse.StoryFitNesseException;
import org.apache.isis.extensions.bdd.fitnesse.internal.AbstractSetUpFixture;

public class AliasServicesForFitNesse extends AbstractSetUpFixture<AliasServicesPeer> {


    public AliasServicesForFitNesse(final AliasRegistry aliasesRegistry) {
    	super(new AliasServicesPeer(aliasesRegistry));
    }

    public void classNameAliasAs(final String className, final String aliasAs) {
    	try {
			getPeer().aliasService(className, aliasAs);
		} catch (StoryValueException ex) {
			throw new StoryFitNesseException(ex);
		}
    }

    public void classNameAliasEquals(final String className,
            final String aliasAs) {
    	classNameAliasAs(className, aliasAs);
    }

}
