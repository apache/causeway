/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
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
class MemberInteractor {
    
    // only used to create failure messages
    static enum MemberType {
        PROPERTY,
        COLLECTION,
        ACTION
    }
    
    protected final ObjectInteractor objectInteractor;

    protected <T extends ObjectMember> 
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
            return notFound(memberType, memberId, Veto.HIDDEN);
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
    
    protected <T extends ObjectMember> 
    _Either<T, InteractionResponse> notFound(MemberType memberType, String memberId, Veto veto) {
        return _Either.right(InteractionResponse.failed(
                veto,
                String.format("%s '%s' either does not exist, is disabled or is not visible", 
                        memberId, 
                        memberType.name().toLowerCase())));
    }
    
    
}
