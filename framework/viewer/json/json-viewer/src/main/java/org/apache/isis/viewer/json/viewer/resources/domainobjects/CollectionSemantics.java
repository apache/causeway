package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public enum CollectionSemantics {

    SET("addToSet"),
    LIST("addToList");
    
    private final String addToKey;

    private CollectionSemantics(String addToKey) {
        this.addToKey = addToKey;
        
    }
    
    public String getAddToKey() {
        return addToKey;
    }
    
    public static CollectionSemantics determine(ResourceContext resourceContext, OneToManyAssociation collection) {
        return collection.getCollectionSemantics().isSet()?CollectionSemantics.SET:CollectionSemantics.LIST;
    }

}
