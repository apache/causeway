package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public class SpecAndAction extends SpecAndFeature<ObjectAction> {
    
    public SpecAndAction(ObjectSpecification objectSpecification, ObjectAction action) {
        super(objectSpecification, action);
    }
    
}