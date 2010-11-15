package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import net.sf.isiscontrib.bdd.fitnesse.StoryFitNesseException;
import net.sf.isiscontrib.bdd.fitnesse.internal.AbstractSetUpFixture;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.StoryValueException;
import org.apache.isis.viewer.bdd.common.fixtures.AliasServicesPeer;

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

    public void classNameAliasEquals(final String className, final String aliasAs) {
        classNameAliasAs(className, aliasAs);
    }

}
