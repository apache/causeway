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

import java.io.Serializable;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.Veto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
public class InteractionVeto implements Serializable {

    private static final long serialVersionUID = 1L;

    public static enum VetoType {
        NOT_FOUND,
        HIDDEN,
        READONLY,
        INVALID,

        ACTION_NOT_SAFE,
        ACTION_NOT_IDEMPOTENT,
        ACTION_PARAM_INVALID,
        ;
        public boolean isHidden() { return this == HIDDEN;};
    }

    @NonNull private final VetoType vetoType;
    @NonNull private final Consent vetoConsent;

    public static InteractionVeto notFound(
            @NonNull final Identifier.Type memberType,
            @Nullable final String memberId) {
        val reason = String.format("member '%s' in %s either does not exist, is disabled or is not visible",
                "" + memberId,
                memberType.name().toLowerCase());
        return of(VetoType.NOT_FOUND, new Veto(reason));
    }

    public static InteractionVeto hidden(@NonNull final Consent vetoConsent) {
        return of(VetoType.HIDDEN, vetoConsent);
    }

    public static InteractionVeto readonly(@NonNull final Consent vetoConsent) {
        return of(VetoType.READONLY, vetoConsent);
    }

    public static InteractionVeto invalid(@NonNull final Consent vetoConsent) {
        return of(VetoType.INVALID, vetoConsent);
    }

    public static InteractionVeto actionNotSafe(@NonNull final ManagedAction action) {
        val reason = String.format("Method not allowed; action '%s' does not have safe semantics",
                action.getId());
        return of(VetoType.ACTION_NOT_SAFE, new Veto(reason));
    }

    public static InteractionVeto actionNotIdempotent(@NonNull final ManagedAction action) {
        val reason = String.format("Method not allowed; action '%s' does not have idempotent semantics",
                action.getId());
        return of(VetoType.ACTION_NOT_IDEMPOTENT, new Veto(reason));
    }

    public static InteractionVeto actionParamInvalid(@NonNull final Consent vetoConsent) {
        return of(VetoType.ACTION_PARAM_INVALID, vetoConsent);
    }

    public static InteractionVeto actionParamInvalid(@NonNull final String reason) {
        return of(VetoType.ACTION_PARAM_INVALID, new Veto(reason));
    }

    public String getReason() {
        return getVetoConsent().getReason();
    }

    public String getDescription() {
        return getVetoConsent().getDescription();
    }

    @Override
    public String toString() {
        return getReason();
    }

}
