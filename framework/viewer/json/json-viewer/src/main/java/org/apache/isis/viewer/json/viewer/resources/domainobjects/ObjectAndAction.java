package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public class ObjectAndAction extends ObjectAndMember<ObjectAction>{
    
    public ObjectAndAction(ObjectAdapter objectAdapter, ObjectAction action) {
        super(objectAdapter, action);
    }

}