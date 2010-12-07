package org.apache.isis.support.prototype;

import org.apache.isis.viewer.bdd.concordion.AbstractIsisConcordionStory;

public abstract class AbstractApplicationStory extends AbstractIsisConcordionStory {

    /**
     * This superclass also acts as the marker for the location of the
     * custom CSS.
     */
    @Override
    protected Class<?> customCssPackage() {
        return AbstractApplicationStory.class;
    }

}
