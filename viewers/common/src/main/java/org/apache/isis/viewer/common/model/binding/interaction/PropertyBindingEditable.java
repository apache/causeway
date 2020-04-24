package org.apache.isis.viewer.common.model.binding.interaction;

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
public class PropertyBindingEditable implements PropertyBinding {

    @Getter(onMethod = @__(@Override))
    private final ManagedObject managedObject;
    
    @Getter(onMethod = @__(@Override))
    private final OneToOneAssociation property;


    @Override
    public InteractionResponse modifyProperty(@Nullable ManagedObject proposedNewValue) {
            
        val consent = property.isAssociationValid(getManagedObject(), proposedNewValue, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            return InteractionResponse.failed(Veto.UNAUTHORIZED, consent.getReason());
        }
        
        property.set(managedObject, proposedNewValue, InteractionInitiatedBy.USER);
        
        return InteractionResponse.success();
    }

}
