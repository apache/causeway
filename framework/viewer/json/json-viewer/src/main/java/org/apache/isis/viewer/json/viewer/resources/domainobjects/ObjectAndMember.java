package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public abstract class ObjectAndMember<T extends ObjectMember> {
    
    private ObjectAdapter objectAdapter;
    private T objectMember;
    
    public ObjectAndMember(ObjectAdapter objectAdapter, T objectMember) {
        this.objectAdapter = objectAdapter;
        this.objectMember = objectMember;
    }
    public ObjectAdapter getObjectAdapter() {
        return objectAdapter;
    }
    public T getMember() {
        return objectMember;
    }
}