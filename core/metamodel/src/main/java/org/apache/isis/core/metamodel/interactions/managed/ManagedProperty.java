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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.binding.Observable;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.binding._Observables;
import org.apache.isis.commons.internal.binding._Observables.LazyObservable;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class ManagedProperty
extends ManagedMember {

    // -- FACTORIES

    public static final ManagedProperty of(
            final @NonNull ManagedObject owner,
            final @NonNull OneToOneAssociation property,
            final @NonNull Where where) {
        return new ManagedProperty(owner, property, where);
    }

    public static final Optional<ManagedProperty> lookupProperty(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {

        return ManagedMember.<OneToOneAssociation>lookup(owner, MemberType.PROPERTY, memberId)
        .map(objectAction -> of(owner, objectAction, where));
    }

    // -- IMPLEMENTATION

    @Getter private final OneToOneAssociation property;

    //XXX suggestion: instead of holding the 'owner' object, let it hold a supplier of the 'owner' object,
    //such that the supplier always returns the actual owner evaluated lazily without memoization.
    //Such a change would better support eg.
    //org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel.getManagedProperty()
    //Or as an alternative use a memento instead of the ManagedObject.
    private ManagedProperty(
            final @NonNull ManagedObject owner,
            final @NonNull OneToOneAssociation property,
            final @NonNull Where where) {
        super(owner, where);
        this.property = property;
        observablePropValue = _Observables.lazy(this::reassessPropertyValue);
    }

    @Override
    public OneToOneAssociation getMetaModel() {
        return getProperty();
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.PROPERTY;
    }

    public Can<ObjectAction> getAssociatedActions() {
        return Can.ofStream(ObjectAction.Util.findForAssociation(getOwner(), getProperty()));
    }

    // -- INTERACTION

    public Optional<InteractionVeto> checkValidity(final ManagedObject proposedNewValue) {
        try {

            val validityConsent =
                    property.isAssociationValid(getOwner(), proposedNewValue, InteractionInitiatedBy.USER);

            return validityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.invalid(validityConsent))
                    : Optional.empty();

        } catch (final Exception ex) {

            log.warn(ex.getLocalizedMessage(), ex);
            return Optional.of(InteractionVeto.invalid(new Veto("failure during validity evaluation")));

        }
    }


    /**
     * @param proposedNewValue
     * @return non-empty if the interaction is not valid for given {@code proposedNewValue}
     */
    public Optional<InteractionVeto> modifyProperty(final @Nullable ManagedObject proposedNewValue) {

        val interactionVeto = checkValidity(proposedNewValue);
        if(interactionVeto.isPresent()) {
            return interactionVeto;
        }

        ManagedObject managedObject = property.set(getOwner(), proposedNewValue, InteractionInitiatedBy.USER);
        setOwner(managedObject);
        observablePropValue.invalidate();
        return Optional.empty();
    }


    /**
     * If visibility is vetoed, returns an empty but specified ManagedObject.
     * @return the property value as to be used by the UI for representation
     */
    private ManagedObject reassessPropertyValue() {
        val property = getProperty();
        val owner = getOwner();

        return property.isVisible(owner, InteractionInitiatedBy.FRAMEWORK, getWhere()).isAllowed()
                && property.isVisible(owner, InteractionInitiatedBy.USER, getWhere()).isAllowed()
            ? property.get(owner, InteractionInitiatedBy.USER)
            : ManagedObject.empty(property.getElementType());
    }


    // -- NEGOTIATION

    public PropertyNegotiationModel startNegotiation() {
        return new PropertyNegotiationModel(this);
    }

    // -- BINDING

    @NonNull private final LazyObservable<ManagedObject> observablePropValue;

    public Observable<ManagedObject> getValue() {
        return observablePropValue;
    }

    public ManagedObject getPropertyValue() {
        return getValue().getValue();
    }

}

