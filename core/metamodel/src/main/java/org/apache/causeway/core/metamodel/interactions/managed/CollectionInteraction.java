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

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public final class CollectionInteraction
extends MemberInteraction<ManagedCollection, CollectionInteraction> {

    public static final CollectionInteraction start(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {

        var managedCollection = ManagedCollection.lookupCollection(owner, memberId, where);

        final InteractionRailway<ManagedCollection> railway = managedCollection.isPresent()
                ? InteractionRailway.success(managedCollection.get())
                : InteractionRailway.veto(InteractionVeto.notFound(Identifier.Type.COLLECTION, memberId));

        return new CollectionInteraction(railway);
    }

    CollectionInteraction(final @NonNull InteractionRailway<ManagedCollection> railway) {
        super(railway);
    }

    /**
     * @return optionally the ManagedCollection based on whether there
     * was no interaction veto within the originating chain
     */
    public Optional<ManagedCollection> getManagedCollection() {
        return railway.getSuccess();
    }

    /**
     * @return this Interaction's ManagedCollection
     * @throws X if there was any interaction veto within the originating chain
     */
    public <X extends Throwable>
    ManagedCollection getManagedCollectionElseThrow(final Function<InteractionVeto, ? extends X> onFailure) throws X {
        return super.getManagedMemberElseThrow(onFailure);
    }

}
