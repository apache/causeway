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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.internal.base._Casts;

public sealed interface MemberInteraction<T extends ManagedMember, H extends MemberInteraction<T, ?>>
permits ActionInteraction, CollectionInteraction, PropertyInteraction {

    public static enum AccessIntent {
        ACCESS, MUTATE;
        public boolean isMutate() { return this == MUTATE; }
    }

    @NonNull InteractionRailway<T> railway();

    default H checkVisibility() {
        railway().update(ManagedMember::checkVisibility);
        return _Casts.uncheckedCast(this);
    }

    default H checkUsability() {
        railway().update(ManagedMember::checkUsability);
        return _Casts.uncheckedCast(this);
    }

    /**
     * Only check usability if intent is {@code MUTATE}.
     * @param intent
     * @return self
     */
    default H checkUsability(final @NonNull AccessIntent intent) {
        if(intent.isMutate()) return checkUsability();

        return _Casts.uncheckedCast(this);
    }

    default <X extends Throwable> H validateElseThrow(
            final Function<InteractionVeto, ? extends X> onFailure) throws X {
        var veto = getInteractionVeto().orElse(null);
        if (veto == null) {
            return _Casts.uncheckedCast(this);
        } else {
            throw onFailure.apply(veto);
        }
    }

    /**
     * @return optionally the InteractionVeto based on whether there
     * was any interaction veto within the originating chain
     */
    default Optional<InteractionVeto> getInteractionVeto() {
        return railway().getVeto();
    }

    /**
     * @return this Interaction's ManagedMember
     * @throws X if there was any interaction veto within the originating chain
     */
    default <X extends Throwable> T getManagedMemberElseThrow(
            final Function<InteractionVeto, ? extends X> onFailure) throws X {
        return railway().getSuccessElseFail(onFailure);
    }

    default InteractionRailway<T> vetoRailway(final InteractionVeto veto) {
        return InteractionRailway.veto(veto);
    }

}
