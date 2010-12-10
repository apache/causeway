package net.sf.isiscontrib.bdd.fitnesse.internal.fixtures;

import net.sf.isiscontrib.bdd.fitnesse.ScenarioFitNesseException;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.ScenarioValueException;

import fitlibrary.SetUpFixture;

public class AliasServicesForFitNesse extends SetUpFixture {

    private final AliasRegistry aliasesRegistry;

    public AliasServicesForFitNesse(final AliasRegistry aliasesRegistry) {
        this.aliasesRegistry = aliasesRegistry;
    }

    public void classNameAliasAs(final String className, final String aliasAs) {
        try {
            aliasesRegistry.aliasService(className, aliasAs);
        } catch (ScenarioValueException ex) {
            throw new ScenarioFitNesseException(ex);
        }
    }

    public void classNameAliasEquals(final String className, final String aliasAs) {
        classNameAliasAs(className, aliasAs);
    }

}
