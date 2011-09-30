package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class ObjectAndProperty extends ObjectAndMember<OneToOneAssociation>{
    
    public ObjectAndProperty(ObjectAdapter objectAdapter, OneToOneAssociation property) {
        super(objectAdapter, property);
    }

}