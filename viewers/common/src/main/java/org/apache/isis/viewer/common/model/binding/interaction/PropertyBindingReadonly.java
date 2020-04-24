package org.apache.isis.viewer.common.model.binding.interaction;

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class PropertyBindingReadonly implements PropertyBinding {

    @Getter(onMethod = @__(@Override))
    private final ManagedObject managedObject;
    
    @Getter(onMethod = @__(@Override))
    private final OneToOneAssociation property;

    @Override
    public InteractionResponse modifyProperty(@Nullable ManagedObject newProperyValue) {
        // if this code is ever reached we have wired up something wrong internally
        return InteractionResponse.failed(Veto.FORBIDDEN);   
    }

}
