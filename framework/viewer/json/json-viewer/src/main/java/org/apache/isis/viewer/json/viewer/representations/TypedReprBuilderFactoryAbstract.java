package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public abstract class TypedReprBuilderFactoryAbstract implements TypedReprBuilderFactory {

    private final RepresentationType representationType;

    public TypedReprBuilderFactoryAbstract(RepresentationType representationType) {
        this.representationType = representationType;
    }
    
    @Override
    public RepresentationType getRepresentationType() {
        return representationType;
    }

    @SuppressWarnings("unchecked")
    public final <R extends TypedReprBuilder<R, T>, T> R newBuilder(ResourceContext resourceContext, Class<T> cls) {
        return (R) newBuilder(resourceContext);
    }

    
}
