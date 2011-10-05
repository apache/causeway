package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

public class SpecAndActionParam extends SpecAndFeature<ObjectActionParameter> {
    
    public SpecAndActionParam(ObjectSpecification objectSpecification, ObjectActionParameter actionParam) {
        super(objectSpecification, actionParam);
    }
    
}