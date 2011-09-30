package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

public class ObjectAndCollection extends ObjectAndMember<OneToManyAssociation>{
    
    public ObjectAndCollection(ObjectAdapter objectAdapter, OneToManyAssociation collection) {
        super(objectAdapter, collection);
    }

}