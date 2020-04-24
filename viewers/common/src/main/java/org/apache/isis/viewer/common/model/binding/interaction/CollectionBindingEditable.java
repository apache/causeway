package org.apache.isis.viewer.common.model.binding.interaction;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class CollectionBindingEditable implements CollectionBinding {

    @Getter(onMethod = @__(@Override))
    private final ManagedObject managedObject;
    
    @Getter(onMethod = @__(@Override))
    private final OneToManyAssociation collection;
    

}
