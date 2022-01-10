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
import java.util.function.BiFunction;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public abstract class ManagedMember
implements ManagedFeature {

    // only used to create failure messages
    @RequiredArgsConstructor
    public static enum MemberType {
        PROPERTY(OneToOneAssociation.class, (spec, propertyId)->
            spec.getProperty(propertyId)),

        COLLECTION(OneToManyAssociation.class, (spec, collectionId)->
            spec.getCollection(collectionId)),

        ACTION(ObjectAction.class, (spec, actionId)->
            spec.getAction(actionId));

        @Getter private final Class<? extends ObjectMember> memberType;
        private final BiFunction<
                ObjectSpecification,
                String,
                Optional<? extends ObjectMember>> memberProvider;

        public <T extends ObjectMember> Optional<T> lookup(
                final @NonNull ManagedObject owner,
                final @NonNull String memberId) {
            val onwerSpec = owner.getSpecification();
            val member = memberProvider.apply(onwerSpec, memberId);
            return _Casts.uncheckedCast(member);
        }

        public boolean isPropertyOrCollection() {
            return this == PROPERTY
                    || this == COLLECTION;
        }

    }

    /**
     * Some representations may vary according to whether the member is to be represented for read
     * (render the state of the property or collection) or for write (render additional hypermedia controls to allow
     * the property to be modified/cleared, or the collection to be added to/removed from).
     */
    public enum RepresentationMode {
        AUTO,
        READ,
        WRITE;
        public boolean isAuto() {return this == AUTO;}
        public boolean isRead() {return this == READ;}
        public boolean isWrite() {return this == WRITE;}
        public boolean isExplicit() {return !isAuto();}
    }

    @NonNull private ManagedObject owner;
    public ManagedObject getOwner() {
        //XXX this is a safeguard
        // see also org.apache.isis.core.metamodel.interactions.managed.ManagedProperty.ManagedProperty(ManagedObject, OneToOneAssociation, Where)
        EntityUtil.refetch(owner);
        return owner;
    }

    @Getter @NonNull private final Where where;

    /**
     * Allows a managed property of a view model to replace its owner with a clone.
     *
     * <p>
     *     This is because view models are intrinsically immutable.
     * </p>
     *
     * @param managedObject
     */
    protected void setOwner(final @NonNull ManagedObject managedObject) {
        this.owner = managedObject;
    }


    @Override
    public abstract ObjectMember getMetaModel();

    public abstract MemberType getMemberType();

    @Override
    public ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

    public String getId() {
        return getMetaModel().getId();
    }

    @Override
    public Identifier getIdentifier() {
        return getMetaModel().getFeatureIdentifier();
    }

    @Override
    public String getFriendlyName() {
        return getMetaModel().getFriendlyName(this::getOwner);
    }

    @Override
    public Optional<String> getDescription() {
        return getMetaModel().getDescription(this::getOwner);
    }

    @Getter @Setter @NonNull
    private RepresentationMode representationMode = RepresentationMode.AUTO;


    /**
     * @return non-empty if hidden
     */
    public Optional<InteractionVeto> checkVisibility() {

        try {
            val visibilityConsent =
                    getMetaModel()
                    .isVisible(getOwner(), InteractionInitiatedBy.USER, where);

            return visibilityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.hidden(visibilityConsent))
                    : Optional.empty();

        } catch (final Exception ex) {

            log.warn(ex.getLocalizedMessage(), ex);
            return Optional.of(InteractionVeto.hidden(new Veto("failure during visibility evaluation")));

        }

    }

    /**
     * @return non-empty if not usable/editable (meaning if read-only)
     */
    public Optional<InteractionVeto> checkUsability() {

        try {

            val usabilityConsent =
                    getMetaModel()
                    .isUsable(getOwner(), InteractionInitiatedBy.USER, where);

            return usabilityConsent.isVetoed()
                    ? Optional.of(InteractionVeto.readonly(usabilityConsent))
                    : Optional.empty();

        } catch (final Exception ex) {

            log.warn(ex.getLocalizedMessage(), ex);
            return Optional.of(InteractionVeto.readonly(new Veto("failure during usability evaluation")));

        }

    }

    protected static <T extends ObjectMember> Optional<T> lookup(
            final @NonNull ManagedObject owner,
            final @NonNull MemberType memberType,
            final @NonNull String memberId) {
        return memberType.lookup(owner, memberId);
    }



}
