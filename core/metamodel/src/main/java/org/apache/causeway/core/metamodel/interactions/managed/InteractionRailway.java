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
import java.util.function.Function;

import org.apache.causeway.commons.functional.Railway;
import org.apache.causeway.commons.functional.Railway.HasRailway;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.val;

/**
 * Follows the <em>Railway Pattern</em>, that is, once vetoed, stays vetoed.
 * @see Railway
 */
@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
@ToString @EqualsAndHashCode
public final class InteractionRailway<T extends ManagedMember>
implements
    HasRailway<InteractionVeto, T>,
    Serializable {
    private static final long serialVersionUID = 1L;

    public static <T extends ManagedMember> InteractionRailway<T> success(final T managedMember) {
        return new InteractionRailway<T>(Railway.<InteractionVeto, T>success(managedMember));
    }

    public static <T extends ManagedMember> InteractionRailway<T> veto(final InteractionVeto veto) {
        return new InteractionRailway<>(Railway.<InteractionVeto, T>failure(veto));
    }

    @Getter private final Railway<InteractionVeto, T> railway;

    @Override // type-safe override
    public InteractionRailway<T> chain(final @NonNull Function<T, Railway<InteractionVeto, T>> chainingFunction) {
        val railway = HasRailway.super.chain(chainingFunction);
        return railway instanceof InteractionRailway
            ? (InteractionRailway<T>) railway
            : new InteractionRailway<>(railway);
    }

}


