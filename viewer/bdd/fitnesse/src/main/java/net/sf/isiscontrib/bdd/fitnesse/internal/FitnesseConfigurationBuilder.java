package net.sf.isiscontrib.bdd.fitnesse.internal;

import org.apache.isis.core.metamodel.config.ConfigurationBuilderFileSystem;
import org.apache.isis.core.metamodel.config.NotFoundPolicy;

public class FitnesseConfigurationBuilder extends ConfigurationBuilderFileSystem {

    public static final String TESTEDOBJECTS_FITNESSE_CONFIG_FILE = "testedobjects.fitnesse.properties";

    public FitnesseConfigurationBuilder(final String configurationDirectory) {
        super(configurationDirectory);
    }

    @Override
    protected void addDefaultConfigurationResources() {
        super.addDefaultConfigurationResources();
        addConfigurationResource(TESTEDOBJECTS_FITNESSE_CONFIG_FILE, NotFoundPolicy.CONTINUE);
    }

}
