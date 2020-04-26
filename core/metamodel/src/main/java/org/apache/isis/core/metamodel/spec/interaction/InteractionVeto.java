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
package org.apache.isis.core.metamodel.spec.interaction;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.interaction.ManagedMember.MemberType;

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
    }
    
    @NonNull private final VetoType vetoType;
    @NonNull private final Consent vetoConsent;
    
    public static InteractionVeto notFound(
            @NonNull MemberType memberType, 
            @Nullable String memberId) {
        val reason = String.format("%s '%s' either does not exist, is disabled or is not visible", 
                "" + memberId, 
                memberType.name().toLowerCase());
        return of(VetoType.NOT_FOUND, new Veto(reason));
    }
    
    public static InteractionVeto hidden(@NonNull Consent vetoConsent) {
        return of(VetoType.HIDDEN, vetoConsent);
    }
    
    public static InteractionVeto readonly(@NonNull Consent vetoConsent) {
        return of(VetoType.READONLY, vetoConsent);
    }
    
    public static InteractionVeto invalid(@NonNull Consent vetoConsent) {
        return of(VetoType.INVALID, vetoConsent);
    }

    public String getReason() {
        return getVetoConsent().getReason();
    }
    
    public String getDescription() {
        return getVetoConsent().getDescription();
    }
    
}
