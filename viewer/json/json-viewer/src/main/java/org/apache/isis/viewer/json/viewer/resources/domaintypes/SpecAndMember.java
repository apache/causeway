package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public abstract class SpecAndMember<T extends ObjectMember> {
    
    private final ObjectSpecification objectSpecification;
    private final T objectMember;
    
    public SpecAndMember(ObjectSpecification objectSpecification, T objectMember) {
        this.objectSpecification = objectSpecification;
        this.objectMember = objectMember;
    }
    public ObjectSpecification getObjectSpecification() {
        return objectSpecification;
    }
    public T getObjectMember() {
        return objectMember;
    }

    
    
}