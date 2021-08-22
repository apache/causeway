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
package org.apache.isis.core.metamodel.interactions;

import java.util.Objects;
import java.util.Optional;

import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Model that holds the objects involved with the interaction.
 * That is a tuple of {regular-object, (same) regular-object}
 * or {mixee, mixin}, based on whether a regular object or a mixee/mixin pair
 * is represented.
 * @since 2.0
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class InteractionHead {
    /**
     * The owning object of an interaction.
     */
    @NonNull private final ManagedObject owner;

    /**
     * Typically equal to {@code owner}, except for mixins,
     * where {@code target} is the mixin instance.
     */
    @NonNull private final ManagedObject target;

    /** Regular case, when owner equals target. (no mixin) */
    public static InteractionHead regular(final ManagedObject owner) {
        return InteractionHead.of(owner, owner);
    }

    /** Mixin case, when target is a mixin for the owner. */
    public static InteractionHead mixin(final @NonNull ManagedObject owner, final @NonNull ManagedObject target) {
        return InteractionHead.of(owner, target);
    }

    /**
     * as used by the domain event subsystem
     * @return optionally the owner, based on whether the target is a mixin
     */
    public Optional<ManagedObject> getMixedIn() {
        return Objects.equals(getOwner(), getTarget())
                ? Optional.empty()
                : Optional.of(getOwner());
    }

    // -- HELPER

    /** factory with consistency checks */
    private static InteractionHead of(final @NonNull ManagedObject owner, final @NonNull ManagedObject target) {
        if(ManagedObjects.isSpecified(owner)
                && owner.getSpecification().getBeanSort().isMixin()) {
            throw _Exceptions.unrecoverableFormatted("unexpected: owner is a mixin %s", owner);
        }
        if(ManagedObjects.isSpecified(target)
                && target.getSpecification().getBeanSort().isMixin()
                && target.getPojo()==null) {
            throw _Exceptions.unrecoverableFormatted("target not spec. %s", target);
        }
        return new InteractionHead(owner, target);
    }

}