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

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;
import lombok.val;

public final class PropertyInteraction
extends MemberInteraction<ManagedProperty, PropertyInteraction> {

    public static final PropertyInteraction start(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {

        val managedProperty = ManagedProperty.lookupProperty(owner, memberId, where);

        final InteractionRailway<ManagedProperty> railway = managedProperty.isPresent()
                ? InteractionRailway.success(managedProperty.get())
                : InteractionRailway.veto(InteractionVeto.notFound(Identifier.Type.PROPERTY, memberId));

        return new PropertyInteraction(railway);
    }

    public static PropertyInteraction wrap(final @NonNull ManagedProperty managedProperty) {
        return new PropertyInteraction(InteractionRailway.success(managedProperty));
    }

    PropertyInteraction(@NonNull final InteractionRailway<ManagedProperty> railway) {
        super(railway);
    }

    public Optional<PropertyNegotiationModel> startPropertyNegotiation() {
        return getManagedProperty()
            .map(ManagedProperty::startNegotiation);
    }

    public PropertyInteraction modifyProperty(
            final @NonNull Function<ManagedProperty, ManagedObject> newProperyValueProvider) {

        railway = railway.chain(property->
            property.modifyProperty(newProperyValueProvider.apply(property))
            .map(super::vetoRailway)
            .orElse(railway));

        return this;
    }

    /**
     * @return optionally the ManagedProperty based on whether there
     * was no interaction veto within the originating chain
     */
    public Optional<ManagedProperty> getManagedProperty() {
        return super.getManagedMember().getSuccess();
    }

    /**
     * @return this Interaction's ManagedProperty
     * @throws X if there was any interaction veto within the originating chain
     */
    public <X extends Throwable>
    ManagedProperty getManagedPropertyElseThrow(final Function<InteractionVeto, ? extends X> onFailure) throws X {
        return super.getManagedMemberElseThrow(onFailure);
    }

}
