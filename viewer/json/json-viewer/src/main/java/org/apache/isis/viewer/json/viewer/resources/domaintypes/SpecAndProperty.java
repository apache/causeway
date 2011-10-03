package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class SpecAndProperty extends SpecAndMember<OneToOneAssociation> {
    
    public SpecAndProperty(ObjectSpecification objectSpecification, OneToOneAssociation property) {
        super(objectSpecification, property);
    }
    
}