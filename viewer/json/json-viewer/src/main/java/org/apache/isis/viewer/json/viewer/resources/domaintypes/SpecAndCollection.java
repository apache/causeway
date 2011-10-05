package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

public class SpecAndCollection extends SpecAndFeature<OneToManyAssociation> {
    
    public SpecAndCollection(ObjectSpecification objectSpecification, OneToManyAssociation collection) {
        super(objectSpecification, collection);
    }
    
}