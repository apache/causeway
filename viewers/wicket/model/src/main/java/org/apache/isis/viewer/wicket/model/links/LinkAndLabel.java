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
package org.apache.isis.viewer.wicket.model.links;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.wicket.markup.html.link.AbstractLink;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.ActionLayout.Position;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.action.HasManagedAction;
import org.apache.isis.viewer.common.model.mixin.HasUiComponent;
import org.apache.isis.viewer.common.model.object.ObjectUiModel;
import org.apache.isis.viewer.wicket.model.util.CommonContextUtils;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinkAndLabel
implements
    HasUiComponent<AbstractLink>,
    HasManagedAction,
    Serializable {

    private static final long serialVersionUID = 1L;

    public static LinkAndLabel of(
            final ActionLinkUiComponentFactoryWkt uiComponentFactory,
            final ObjectUiModel actionHolderModel,
            final ObjectAction objectAction) {
        return new LinkAndLabel(uiComponentFactory, actionHolderModel, objectAction);
    }

    //FIXME[ISIS-2877] we have serializable action models - use one of these instead
    private transient ManagedAction managedAction;

    @Override
    public ManagedAction getManagedAction() {
        if(managedAction==null) {
            managedAction = ManagedAction.of(actionHolder.getManagedObject(), objectAction, Where.ANYWHERE);
        }
        return managedAction;
    }

    protected final ActionLinkUiComponentFactoryWkt uiComponentFactory;

    /**
     * used when explicitly named (eg. menu bar layout file), otherwise {@code null}
     */
    @Getter private String named;

    /**
     * domain object that is the <em>Action's</em> holder or owner
     */
    @Getter private final ObjectUiModel actionHolder;

    /**
     * framework internal <em>Action</em> model
     */
    @Getter private final ObjectAction objectAction;

    // implements HasUiComponent<T>
    @Getter(lazy = true, onMethod_ = {@Override})
    private final AbstractLink uiComponent = uiComponentFactory
        .newActionLinkUiComponent(getManagedAction());

    @Override
    public String toString() {
        return Optional.ofNullable(named).orElse("") +
                " ~ " + objectAction.getFeatureIdentifier().getFullIdentityString();
    }

    // -- VISIBILITY

    //FIXME[ISIS-2877] de-duplicate
    public boolean isVisible() {
        val owner = actionHolder.getManagedObject();

        // check hidden
        if (owner.getSpecification().isHidden()) {
            return false;
        }
        // check visibility
        final Consent visibility = objectAction.isVisible(
                owner,
                InteractionInitiatedBy.USER,
                Where.ANYWHERE);
        if (visibility.isVetoed()) {
            return false;
        }
        return true;
    }


    // -- UTILITY

    public static Predicate<LinkAndLabel> isPositionedAt(final Position panel) {
        return HasManagedAction.isPositionedAt(panel);
    }

    public static List<LinkAndLabel> recoverFromIncompleteDeserialization(final List<SerializationProxy> proxies) {
        return proxies.stream()
                .map(SerializationProxy::restore)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    // -- SERIALIZATION PROXY

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final @NonNull ActionLinkUiComponentFactoryWkt uiComponentFactory;
        private final @Nullable String named;
        private final @NonNull ObjectUiModel actionHolder;
        private final @NonNull LogicalType actionHolderLogicalType;
        private final @NonNull String objectActionId;

        private SerializationProxy(final LinkAndLabel target) {
            this.uiComponentFactory = target.uiComponentFactory;
            this.named = target.getNamed();
            this.actionHolder = target.getActionHolder();
            // make sure we do this without side-effects
            this.actionHolderLogicalType = actionHolder
                    .getManagedObject().getSpecification().getLogicalType();
            this.objectActionId = target.getObjectAction().getId();
        }

        private Object readResolve() {
            return restore();
        }

        private LinkAndLabel restore() {
            val commonContext = CommonContextUtils.getCommonContext();
            val objectMember = commonContext.getSpecificationLoader()
            .specForLogicalType(actionHolderLogicalType)
            .flatMap(actionHolderSpec->actionHolderSpec.getMember(objectActionId))
            .orElseThrow(()->
                _Exceptions.noSuchElement("could not restore objectAction from id %s", objectActionId));
            return new LinkAndLabel(uiComponentFactory, actionHolder, (ObjectAction) objectMember);
        }

    }



}
