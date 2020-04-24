package org.apache.isis.viewer.common.model.binding.interaction;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class PropertyAccessChain {
    
    @NonNull private _Either<PropertyBinding, InteractionResponse> chain;

    public PropertyAccessChain modifyProperty(
            @NonNull final Function<OneToOneAssociation, ManagedObject> newProperyValueProvider) {

        chain = chain.leftRemap(propertyBinding->{
            final InteractionResponse iResponse = propertyBinding.modifyProperty(newProperyValueProvider);
            if(iResponse.isFailure()) {
                _Either.right(iResponse);
            }
            return _Either.left(propertyBinding);
        });
        
        return this;
    }

    public PropertyAccessChain onFailure(Consumer<InteractionResponse> onFailure) {
        chain.right().ifPresent(onFailure);
        return this;
    }

    public PropertyBinding getBinding() {
        return chain.left()
                .orElseThrow(_Exceptions::noSuchElement);
    }
    
    public OneToOneAssociation getProperty() {
        return chain.left()
                .map(PropertyBinding::getProperty)
                .orElseThrow(_Exceptions::noSuchElement);
    }

    public PropertyAccessChain ifPresent(Consumer<PropertyBinding> propertyBindingConsumer) {
        chain.left().ifPresent(propertyBindingConsumer::accept);
        return this;
    }

    

}
