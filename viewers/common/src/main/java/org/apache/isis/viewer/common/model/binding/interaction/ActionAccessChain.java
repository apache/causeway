package org.apache.isis.viewer.common.model.binding.interaction;

import java.util.function.Consumer;

import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class ActionAccessChain {
    
    @NonNull private _Either<ActionBinding, InteractionResponse> chain;

//    public ActionAccessChain modifyProperty(
//            @NonNull final Function<OneToOneAssociation, ManagedObject> newProperyValueProvider) {
//
//        chain = chain.leftRemap(propertyBinding->{
//            final InteractionResponse iResponse = propertyBinding.modifyProperty(newProperyValueProvider);
//            if(iResponse.isFailure()) {
//                _Either.right(iResponse);
//            }
//            return _Either.left(propertyBinding);
//        });
//        
//        return this;
//    }

    public ActionAccessChain onFailure(Consumer<InteractionResponse> onFailure) {
        chain.right().ifPresent(onFailure);
        return this;
    }

    public ActionBinding getBinding() {
        return chain.left()
                .orElseThrow(_Exceptions::noSuchElement);
    }
    
    public ObjectAction getAction() {
        return chain.left()
                .map(ActionBinding::getAction)
                .orElseThrow(_Exceptions::noSuchElement);
    }

    public ActionAccessChain ifPresent(Consumer<ActionBinding> actionBindingConsumer) {
        chain.left().ifPresent(actionBindingConsumer::accept);
        return this;
    }
    

}
