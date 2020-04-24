package org.apache.isis.viewer.common.model.binding.interaction;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;

import lombok.val;

public class CollectionInteractor extends MemberInteractor {

    public CollectionInteractor(ObjectInteractor objectInteractor) {
        super(objectInteractor);
    }

    public _Either<OneToManyAssociation, InteractionResponse> getPropertyThatIsVisibleForIntent(
            final String collectionId,
            final Where where,
            final AccessIntent intent) {

        val managedObject = objectInteractor.getManagedObject();
        
        val spec = managedObject.getSpecification();
        val collection = spec.getAssociation(collectionId).orElse(null);
        if(collection==null || !collection.isOneToOneAssociation()) {
            return _Either.right(InteractionResponse.failed(Veto.NOT_FOUND));
        }
        
        return super.memberThatIsVisibleForIntent(
                MemberType.COLLECTION,
                (OneToManyAssociation) collection, where, intent);
    }

}
