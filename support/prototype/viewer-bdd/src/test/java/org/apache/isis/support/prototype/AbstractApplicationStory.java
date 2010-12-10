package org.apache.isis.support.prototype;

import org.apache.isis.viewer.bdd.concordion.AbstractIsisConcordionScenario;

public abstract class AbstractApplicationStory extends AbstractIsisConcordionScenario {

    /**
     * This superclass also acts as the marker for the location of the
     * custom CSS.
     */
    @Override
    protected Class<?> customCssPackage() {
        return AbstractApplicationStory.class;
    }

}
