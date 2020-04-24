package org.apache.isis.viewer.common.model.binding.interaction;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.viewer.common.model.binding.interaction.InteractionResponse.Veto;
import org.apache.isis.viewer.common.model.binding.interaction.ObjectInteractor.AccessIntent;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class MemberInteractor {
    
    // only used to create failure messages
    static enum MemberType {
        PROPERTY,
        COLLECTION,
        ACTION
    }
    
    protected final ObjectInteractor objectInteractor;

    public <T extends ObjectMember> 
    _Either<T, InteractionResponse> memberThatIsVisibleForIntent(
            final MemberType memberType,
            final T objectMember, 
            final Where where, 
            final AccessIntent intent) {

        val managedObject = objectInteractor.getManagedObject();
        val visibilityConsent =
                objectMember.isVisible(
                        managedObject, InteractionInitiatedBy.USER, where);
        if (visibilityConsent.isVetoed()) {
            val memberId = objectMember.getId();
            return _Either.right(InteractionResponse.failed(
                    Veto.HIDDEN,
                    String.format("%s '%s' either does not exist, is disabled or is not visible", 
                            memberId, 
                            memberType.name().toLowerCase())));
        }
        if (intent.isMutate()) {
            final Consent usabilityConsent = objectMember.isUsable(
                    managedObject, InteractionInitiatedBy.USER, where);
            if (usabilityConsent.isVetoed()) {
                return _Either.right(InteractionResponse.failed(
                        Veto.FORBIDDEN,
                        usabilityConsent.getReason()));
            }
        }
        return _Either.left(objectMember);
    }
    
}
