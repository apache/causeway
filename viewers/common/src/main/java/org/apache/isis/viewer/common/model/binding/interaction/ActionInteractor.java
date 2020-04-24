package org.apache.isis.viewer.common.model.binding.interaction;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;

import lombok.val;

public class ActionInteractor extends MemberInteractor {

    public ActionInteractor(ObjectInteractor objectInteractor) {
        super(objectInteractor);
    }

    public _Either<ObjectAction, InteractionResponse> getActionThatIsVisibleForIntent(
            final String actionId,
            final Where where,
            final AccessIntent intent) {

        val managedObject = objectInteractor.getManagedObject();
        
        val spec = managedObject.getSpecification();
        val action = spec.getObjectAction(actionId).orElse(null);
        
        if(action==null) {
            return _Either.right(InteractionResponse.failed(Veto.NOT_FOUND));
        }
        
        return super.memberThatIsVisibleForIntent(
                MemberType.ACTION,
                action, where, intent);
    }

}
