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
package org.apache.causeway.core.metamodel.spec.feature;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.CanVector;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResultSet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.VisibilityConstraint;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * Introduced to allow for proxies of {@link ObjectAction}.
 */
@FunctionalInterface
public interface HasObjectAction extends ObjectAction {

    ObjectAction getObjectAction();

    @Override default ObjectSpecification getDeclaringType() {
        return getObjectAction().getDeclaringType();
    }
    @Override default String getHelp() {
        return getObjectAction().getHelp();
    }
    @Override default boolean isAlwaysHidden() {
        return getObjectAction().isAlwaysHidden();
    }
    @Override default Consent isVisible(final ManagedObject target, final InteractionInitiatedBy interactionInitiatedBy, final VisibilityConstraint visConstraint) {
        return getObjectAction().isVisible(target, interactionInitiatedBy, visConstraint);
    }
    @Override default Consent isUsable(final ManagedObject target, final InteractionInitiatedBy interactionInitiatedBy, final VisibilityConstraint visConstraint) {
        return getObjectAction().isUsable(target, interactionInitiatedBy, visConstraint);
    }
    @Override default boolean isPropertyOrCollection() {
        return getObjectAction().isPropertyOrCollection();
    }
    @Override default boolean isOneToManyAssociation() {
        return getObjectAction().isOneToManyAssociation();
    }
    @Override default boolean isOneToOneAssociation() {
        return getObjectAction().isOneToOneAssociation();
    }
    @Override default boolean isAction() {
        return getObjectAction().isAction();
    }
    @Override default boolean isExplicitlyAnnotated() {
        return getObjectAction().isExplicitlyAnnotated();
    }
    @Override default String getId() {
        return getObjectAction().getId();
    }
    @Override default String getFriendlyName(final Supplier<ManagedObject> domainObjectProvider) {
        return getObjectAction().getFriendlyName(domainObjectProvider);
    }
    @Override default Optional<String> getStaticFriendlyName() {
        return getObjectAction().getStaticFriendlyName();
    }
    @Override default String getCanonicalFriendlyName() {
        return getObjectAction().getCanonicalFriendlyName();
    }
    @Override default Optional<String> getDescription(final Supplier<ManagedObject> domainObjectProvider) {
        return getObjectAction().getDescription(domainObjectProvider);
    }
    @Override default Optional<String> getStaticDescription() {
        return getObjectAction().getStaticDescription();
    }
    @Override default Optional<String> getCanonicalDescription() {
        return getObjectAction().getCanonicalDescription();
    }
    @Override default ObjectSpecification getElementType() {
        return getObjectAction().getElementType();
    }
    @Override default String asciiId() {
        return getObjectAction().asciiId();
    }
    @Override default FeatureType getFeatureType() {
        return getObjectAction().getFeatureType();
    }
    @Override
    default FacetHolder getFacetHolder() {
        return getObjectAction().getFacetHolder();
    }
    @Override default SemanticsOf getSemantics() {
        return getObjectAction().getSemantics();
    }
    @Override default ActionScope getScope() {
        return getObjectAction().getScope();
    }
    @Override default boolean isPrototype() {
        return getObjectAction().isPrototype();
    }
    @Override default boolean isDeclaredOnMixin() {
        return getObjectAction().isDeclaredOnMixin();
    }
    @Override default ObjectSpecification getReturnType() {
        return getObjectAction().getReturnType();
    }
    @Override default boolean hasReturn() {
        return getObjectAction().hasReturn();
    }
    @Override default ManagedObject executeWithRuleChecking(final InteractionHead head, final Can<ManagedObject> parameters,
        final InteractionInitiatedBy interactionInitiatedBy, final Where where) throws AuthorizationException {
        return getObjectAction().executeWithRuleChecking(head, parameters, interactionInitiatedBy, where);
    }
    @Override default ManagedObject execute(final InteractionHead head, final Can<ManagedObject> parameters,
        final InteractionInitiatedBy interactionInitiatedBy) {
        return getObjectAction().execute(head, parameters, interactionInitiatedBy);
    }
    @Override default Consent isArgumentSetValid(final InteractionHead head, final Can<ManagedObject> proposedArguments,
        final InteractionInitiatedBy interactionInitiatedBy) {
        return getObjectAction().isArgumentSetValid(head, proposedArguments, interactionInitiatedBy);
    }
    @Override default InteractionResultSet isArgumentSetValidForParameters(final InteractionHead head,
        final Can<ManagedObject> proposedArguments, final InteractionInitiatedBy interactionInitiatedBy) {
        return getObjectAction().isArgumentSetValidForParameters(head, proposedArguments, interactionInitiatedBy);
    }
    @Override default Consent isArgumentSetValidForAction(final InteractionHead head, final Can<ManagedObject> proposedArguments,
        final InteractionInitiatedBy interactionInitiatedBy) {
        return getObjectAction().isArgumentSetValidForAction(head, proposedArguments, interactionInitiatedBy);
    }
    @Override default InteractionHead interactionHead(@NonNull final ManagedObject actionOwner) {
        return getObjectAction().interactionHead(actionOwner);
    }
    @Override default int getParameterCount() {
        return getObjectAction().getParameterCount();
    }
    @Override default Can<ObjectActionParameter> getParameters() {
        return getObjectAction().getParameters();
    }
    @Override default Can<ObjectSpecification> getParameterTypes() {
        return getObjectAction().getParameterTypes();
    }
    @Override default Can<ObjectActionParameter> getParameters(final Predicate<ObjectActionParameter> predicate) {
        return getObjectAction().getParameters();
    }
    @Override default ObjectActionParameter getParameterById(final String paramId) {
        return getObjectAction().getParameterById(paramId);
    }
    @Override default ObjectActionParameter getParameterByName(final String paramName) {
        return getObjectAction().getParameterByName(paramName);
    }
    @Override default ManagedObject realTargetAdapter(final ManagedObject targetAdapter) {
        return getObjectAction().realTargetAdapter(targetAdapter);
    }
    @Override default CanVector<ManagedObject> getChoices(final ManagedObject target, final InteractionInitiatedBy interactionInitiatedBy) {
        return getObjectAction().getChoices(target, interactionInitiatedBy);
    }

}