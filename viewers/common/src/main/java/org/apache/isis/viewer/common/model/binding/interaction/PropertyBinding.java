package org.apache.isis.viewer.common.model.binding.interaction;

import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.val;

public interface PropertyBinding {

    ManagedObject getManagedObject();
    OneToOneAssociation getProperty();

    InteractionResponse modifyProperty(ManagedObject newProperyValue);
    
    default InteractionResponse modifyProperty(
            @NonNull final Function<OneToOneAssociation, ManagedObject> newProperyValueProvider) {
        return modifyProperty(newProperyValueProvider.apply(getProperty()));
    }
    
    default ManagedObject getPropertyValue() {
        val property = getProperty();
        
        return Optional.ofNullable(property.get(getManagedObject()))
        .orElse(ManagedObject.of(property.getSpecification(), null));
    }

}
