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
package org.apache.isis.viewer.common.model.action;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.memento.ActionMemento;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;
import org.apache.isis.viewer.common.model.decorator.icon.FontAwesomeUiModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ActionUiMetaModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final ActionMemento actionMemento;
    @Getter private final String label;
    @Getter private final String description;
    @Getter private final boolean blobOrClob;
    @Getter private final boolean prototyping;
    @Getter private final String actionIdentifier;
    @Getter private final String cssClass;
    @Getter private final Optional<FontAwesomeUiModel> fontAwesomeUiModel;
    @Getter private final ActionLayout.Position position;
    @Getter private final SemanticsOf semantics;
    @Getter private final PromptStyle promptStyle;
    @Getter private final Parameters parameters;
    @Getter private final Optional<DisablingUiModel> disableUiModel;
    /**
     * An action with no parameters AND an are-you-sure semantics
     * does require an immediate confirmation dialog.
     */
    @Getter private final boolean requiresImmediateConfirmation;

    public static <T> ActionUiMetaModel of(
            final ManagedAction managedAction) {
        return new ActionUiMetaModel(managedAction.getOwner(), managedAction.getAction());
    }

    public static <T> ActionUiMetaModel of(
            final ManagedObject actionHolder,
            final ObjectAction objectAction) {
        return new ActionUiMetaModel(actionHolder, objectAction);
    };

    private ActionUiMetaModel(
            final ManagedObject actionHolder,
            final ObjectAction objectAction) {

        this(   ActionMemento.forAction(objectAction),
                ObjectAction.Util.nameFor(objectAction),
                getDescription(objectAction).orElse(ObjectAction.Util.descriptionOf(objectAction)),
                ObjectAction.Util.returnsBlobOrClob(objectAction),
                objectAction.isPrototype(),
                ObjectAction.Util.actionIdentifierFor(objectAction),
                ObjectAction.Util.cssClassFor(objectAction, actionHolder),
                FontAwesomeUiModel.of(ObjectAction.Util.cssClassFaFacetFor(objectAction)),
                ObjectAction.Util.actionLayoutPositionOf(objectAction),
                objectAction.getSemantics(),
                ObjectAction.Util.promptStyleFor(objectAction),
                Parameters.fromParameterCount(objectAction.getParameterCount()),
                disabledUiModelFor(actionHolder, objectAction),
                ObjectAction.Util.isAreYouSureSemantics(objectAction)
                && ObjectAction.Util.isNoParameters(objectAction)
                );
    }

    public static <R> Predicate<R> positioned(
            final ActionLayout.Position position,
            final Function<R, ActionUiMetaModel> posAccessor) {
        return x -> posAccessor.apply(x).getPosition() == position;
    }

    public ObjectAction getObjectAction(final SpecificationLoader specLoader) {
        return actionMemento.getAction(specLoader);
    }

    // -- PARAMETERS

    public enum Parameters {
        NO_PARAMETERS,
        TAKES_PARAMETERS;

        public static Parameters fromParameterCount(final int parameterCount) {
            return parameterCount > 0? TAKES_PARAMETERS: NO_PARAMETERS;
        }

        public boolean isNoParameters() {
            return this == NO_PARAMETERS;
        }
    }

    // -- USABILITY

    private static Optional<DisablingUiModel> disabledUiModelFor(
            @NonNull final ManagedObject actionHolder,
            @NonNull final ObjectAction objectAction) {

        // check usability
        final Consent usability = objectAction.isUsable(
                actionHolder,
                InteractionInitiatedBy.USER,
                Where.ANYWHERE
                );

        val enabled = usability.getReason() == null;
        return DisablingUiModel.of(!enabled, usability.getReason()) ;
    }

    // -- DESCRIBED AS

    private static Optional<String> getDescription(
            @NonNull final ObjectAction objectAction) {

        val describedAsFacet = objectAction.getFacet(DescribedAsFacet.class);
        return Optional.ofNullable(describedAsFacet)
                .map(DescribedAsFacet::value);
    }

}
