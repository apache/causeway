package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;

public abstract class SpecAndFeature<T extends ObjectFeature> {
    
    private final ObjectSpecification objectSpecification;
    private final T objectFeature;
    
    public SpecAndFeature(ObjectSpecification objectSpecification, T objectFeature) {
        this.objectSpecification = objectSpecification;
        this.objectFeature = objectFeature;
    }
    public ObjectSpecification getObjectSpecification() {
        return objectSpecification;
    }
    public T getObjectFeature() {
        return objectFeature;
    }

    
    
}