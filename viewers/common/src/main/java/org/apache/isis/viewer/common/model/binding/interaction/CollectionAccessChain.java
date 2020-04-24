package org.apache.isis.viewer.common.model.binding.interaction;

import java.util.function.Consumer;

import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName = "of")
public class CollectionAccessChain {
    
    @NonNull private _Either<CollectionBinding, InteractionResponse> chain;

//    public CollectionAccessChain modifyProperty(
//            @NonNull final Function<OneToManyAssociation, ManagedObject> newProperyValueProvider) {
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

    public CollectionAccessChain onFailure(Consumer<InteractionResponse> onFailure) {
        chain.right().ifPresent(onFailure);
        return this;
    }

    public CollectionBinding getBinding() {
        return chain.left()
                .orElseThrow(_Exceptions::noSuchElement);
    }
    
    public OneToManyAssociation getCollection() {
        return chain.left()
                .map(CollectionBinding::getCollection)
                .orElseThrow(_Exceptions::noSuchElement);
    }

    public CollectionAccessChain ifPresent(Consumer<CollectionBinding> collectionBindingConsumer) {
        chain.left().ifPresent(collectionBindingConsumer::accept);
        return this;
    }

    

}
