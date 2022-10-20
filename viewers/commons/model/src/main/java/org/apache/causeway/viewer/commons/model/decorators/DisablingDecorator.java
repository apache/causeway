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
package org.apache.causeway.viewer.commons.model.decorators;

import java.io.Serializable;
import java.util.Optional;

import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.interactions.managed.MemberInteraction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@FunctionalInterface
public interface DisablingDecorator<T> {

    void decorate(T uiComponent, DisablingDecorationModel decorationModel);

    // -- DECORATION MODEL

    @Getter
    @RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    public static class DisablingDecorationModel implements Serializable {

        private static final long serialVersionUID = 1L;

        final @NonNull String reason;

        public static Optional<DisablingDecorationModel> of(@NonNull final Optional<InteractionVeto> usabilityVeto) {
            return usabilityVeto
                    .map(veto->of(veto.getReason()));
        }

        public static Optional<DisablingDecorationModel> of(@NonNull final MemberInteraction<?, ?> memberInteraction) {
            return of(memberInteraction.getInteractionVeto());
        }


    }

}
