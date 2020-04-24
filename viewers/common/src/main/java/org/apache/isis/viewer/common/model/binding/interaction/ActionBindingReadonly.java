package org.apache.isis.viewer.common.model.binding.interaction;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class ActionBindingReadonly implements ActionBinding {

    @Getter(onMethod = @__(@Override))
    private final ManagedObject managedObject;
    
    @Getter(onMethod = @__(@Override))
    private final ObjectAction action;
    

}
