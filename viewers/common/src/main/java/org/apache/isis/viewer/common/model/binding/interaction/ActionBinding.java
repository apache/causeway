package org.apache.isis.viewer.common.model.binding.interaction;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

public interface ActionBinding {

    ManagedObject getManagedObject();
    ObjectAction getAction();

//    InteractionResponse modifyProperty(ManagedObject newProperyValue);
//    
//    default InteractionResponse modifyProperty(
//            @NonNull final Function<OneToOneAssociation, ManagedObject> newProperyValueProvider) {
//        return modifyProperty(newProperyValueProvider.apply(getProperty()));
//    }
//    
//    default ManagedObject getPropertyValue() {
//        val property = getProperty();
//        
//        return Optional.ofNullable(property.get(getManagedObject()))
//        .orElse(ManagedObject.of(property.getSpecification(), null));
//    }

}
