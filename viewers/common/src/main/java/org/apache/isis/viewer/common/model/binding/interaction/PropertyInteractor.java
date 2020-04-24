package org.apache.isis.viewer.common.model.binding.interaction;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;

import lombok.val;

public class PropertyInteractor extends MemberInteractor {

    public PropertyInteractor(ObjectInteractor objectInteractor) {
        super(objectInteractor);
    }

    public _Either<OneToOneAssociation, InteractionResponse> getPropertyThatIsVisibleForIntent(
            final String propertyId,
            final Where where,
            final AccessIntent intent) {

        val managedObject = objectInteractor.getManagedObject();
        
        val spec = managedObject.getSpecification();
        val property = spec.getAssociation(propertyId).orElse(null);
        if(property==null || !property.isOneToOneAssociation()) {
            return _Either.right(InteractionResponse.failed(Veto.NOT_FOUND));
        }
        
        return super.memberThatIsVisibleForIntent(
                MemberType.PROPERTY,
                (OneToOneAssociation)property, where, intent);
    }

}
