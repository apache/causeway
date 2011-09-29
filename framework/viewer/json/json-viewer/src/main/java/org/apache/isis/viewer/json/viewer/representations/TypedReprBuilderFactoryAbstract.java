package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.RepresentationType;

public abstract class TypedReprBuilderFactoryAbstract implements TypedReprBuilderFactory {

    private final RepresentationType representationType;

    public TypedReprBuilderFactoryAbstract(RepresentationType representationType) {
        this.representationType = representationType;
    }
    
    @Override
    public RepresentationType getRepresentationType() {
        return representationType;
    }

    
}
