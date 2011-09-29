package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.viewer.ResourceContext;

public abstract class TypedReprBuilderAbstract<T> implements TypedReprBuilder<T> {

    private final ResourceContext resourceContext;

    public TypedReprBuilderAbstract(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }
    
    public ResourceContext getResourceContext() {
        return resourceContext;
    }

}
