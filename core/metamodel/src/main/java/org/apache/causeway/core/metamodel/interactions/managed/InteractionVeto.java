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
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.consent.Veto;

public record InteractionVeto(
    @NonNull VetoType vetoType,
    @NonNull Consent vetoConsent
    ) implements Serializable {

    public static enum VetoType {
        NOT_FOUND,
        HIDDEN,
        READONLY,
        INVALID,

        ACTION_NOT_SAFE,
        ACTION_NOT_IDEMPOTENT,
        ACTION_PARAM_INVALID;

        public boolean isHidden() { return this == HIDDEN;}
    }


    public static InteractionVeto notFound(
            final Identifier.@NonNull Type memberType,
            final @Nullable String memberId) {
        var reason = String.format("member '%s' in %s either does not exist, is disabled or is not visible",
                "" + memberId,
                memberType.name().toLowerCase());
        return new InteractionVeto(VetoType.NOT_FOUND, new Veto(reason));
    }

    public static InteractionVeto hidden(final @NonNull Consent vetoConsent) {
        return new InteractionVeto(VetoType.HIDDEN, vetoConsent);
    }

    public static InteractionVeto readonly(final @NonNull Consent vetoConsent) {
        return new InteractionVeto(VetoType.READONLY, vetoConsent);
    }

    public static InteractionVeto invalid(final @NonNull Consent vetoConsent) {
        return new InteractionVeto(VetoType.INVALID, vetoConsent);
    }

    public static InteractionVeto invocationException(final Throwable e) {
        var reason = e.toString();
        return new InteractionVeto(VetoType.INVALID, new Veto(reason));
    }

    public static InteractionVeto actionNotSafe(final @NonNull ManagedAction action) {
        var reason = String.format("Method not allowed; action '%s' does not have safe semantics",
                action.getId());
        return new InteractionVeto(VetoType.ACTION_NOT_SAFE, new Veto(reason));
    }

    public static InteractionVeto actionNotIdempotent(final @NonNull ManagedAction action) {
        var reason = String.format("Method not allowed; action '%s' does not have idempotent semantics",
                action.getId());
        return new InteractionVeto(VetoType.ACTION_NOT_IDEMPOTENT, new Veto(reason));
    }

    public static InteractionVeto actionParamInvalid(final @NonNull Consent vetoConsent) {
        return new InteractionVeto(VetoType.ACTION_PARAM_INVALID, vetoConsent);
    }

    public static InteractionVeto actionParamInvalid(final @NonNull String reason) {
        return new InteractionVeto(VetoType.ACTION_PARAM_INVALID, new Veto(reason));
    }

    public Optional<VetoReason> getReason() {
        return vetoConsent().getReason();
    }
    public Optional<String> getReasonAsString() {
        return vetoConsent().getReasonAsString();
    }

    public String getDescription() {
        return vetoConsent().getDescription();
    }

    @Override
    public String toString() {
        return getReasonAsString().orElse("allowed (not vetoed)");
    }

}
