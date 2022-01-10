/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.core.metamodel.spec.feature;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.CanVector;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.ChoicesFromFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFactory;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ActionScope;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.memento.ActionMemento;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionDefault;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.NonNull;
import lombok.val;

public interface ObjectAction extends ObjectMember {

    /**
     * The semantics of this action.
     */
    SemanticsOf getSemantics();

    /**
     * An action with no parameters AND <i>are-you-sure</i> semantics
     * does require an immediate confirmation dialog.
     */
    default boolean isImmediateConfirmationRequired() {
        return getSemantics().isAreYouSure()
        && ObjectAction.Util.isNoParameters(this);
    }

    ActionScope getScope();

    boolean isPrototype();

    /**
     * Whether this {@link ObjectAction} instance represents a mixin main method,
     * usually of type {@link ObjectActionDefault}, peered by an {@link ObjectActionMixedIn}.
     * <p>
     * Such instances are used for populating the meta-model.
     */
    boolean isDeclaredOnMixin();

    /**
     * Returns the specifications for the return type.
     */
    ObjectSpecification getReturnType();

    /**
     * Returns <tt>true</tt> if the represented action returns a non-void object,
     * else returns false.
     */
    boolean hasReturn();

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters, checking the visibility, usability and validity first.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.  When a mixin action invokes its underlying mixedIn action, then will be populated (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    ManagedObject executeWithRuleChecking(
            InteractionHead head,
            Can<ManagedObject> parameters,
            InteractionInitiatedBy interactionInitiatedBy,
            Where where) throws AuthorizationException;

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.
     * When a mixin action invokes its underlying mixedIn action, then will be populated
     * (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    ManagedObject execute(
            InteractionHead head,
            Can<ManagedObject> parameters,
            InteractionInitiatedBy interactionInitiatedBy);


    // -- isArgumentSetValid, isArgumentSetValidForParameters, isArgumentSetValidForAction

    /**
     * Whether the provided argument set is valid, represented as a {@link Consent}.
     *
     * <p>
     *     Basically just calls (the helper methods also called by) first
     *     {@link #isArgumentSetValidForParameters(InteractionHead, Can, InteractionInitiatedBy)}
     *     and then
     *     {@link #isArgumentSetValidForAction(InteractionHead, Can, InteractionInitiatedBy)}
     *     Those methods are
     *     separated out so that viewers have more fine-grained control.
     * </p>
     */
    Consent isArgumentSetValid(
            InteractionHead head,
            Can<ManagedObject> proposedArguments,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Normally action validation is all performed by
     * {@link #isArgumentSetValid(InteractionHead, Can, InteractionInitiatedBy)}, which calls
     * {@link #isArgumentSetValidForParameters(InteractionHead, Can, InteractionInitiatedBy) this method} to
     * validate arguments individually, and then
     * {@link #isArgumentSetValidForAction(InteractionHead, Can, InteractionInitiatedBy) validate argument set}
     * afterwards.
     *
     * <p>
     * This method is in the API to allow viewers (eg the RO viewer) to call the different phases of validation
     * individually.
     * </p>
     */
    InteractionResultSet isArgumentSetValidForParameters(
            InteractionHead head,
            Can<ManagedObject> proposedArguments,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Normally action validation is all performed by
     * {@link #isArgumentSetValid(InteractionHead, Can, InteractionInitiatedBy)}, which calls
     * {@link #isArgumentSetValidForParameters(InteractionHead, Can, InteractionInitiatedBy)} to
     * validate arguments individually, and then
     * {@link #isArgumentSetValidForAction(InteractionHead, Can, InteractionInitiatedBy) this method} to
     * validate the entire argument set afterwards.
     *
     * <p>
     * This method is in the API to allow viewers (eg the RO viewer) to call the different phases of validation
     * individually.
     * </p>
     */
    Consent isArgumentSetValidForAction(
            InteractionHead head,
            Can<ManagedObject> proposedArguments,
            InteractionInitiatedBy interactionInitiatedBy);


    // -- INTERACTION HEAD

    ActionInteractionHead interactionHead(@NonNull ManagedObject actionOwner);

    // -- Parameters (declarative)

    /**
     * Returns the number of parameters used by this method.
     */
    int getParameterCount();

    /**
     * Returns set of parameter information.
     *
     * <p>
     * Implementations may build this array lazily or eagerly as required.
     */
    Can<ObjectActionParameter> getParameters();

    /**
     * Returns a {@link Stream} of {@link ObjectActionParameter} as per
     * {@link #getParameters()}.
     */
    default Stream<ObjectActionParameter> streamParameters() {
        return getParameters().stream();
    }

    /**
     * Returns the {@link ObjectSpecification type} of each of the {@link #getParameters() parameters}.
     */
    Can<ObjectSpecification> getParameterTypes();

    /**
     * Returns set of parameter information matching the supplied filter.
     */
    Can<ObjectActionParameter> getParameters(Predicate<ObjectActionParameter> predicate);

    /**
     * Returns the parameter with provided id.
     */
    ObjectActionParameter getParameterById(String paramId);

    /**
     * Returns the parameter with provided name.
     */
    ObjectActionParameter getParameterByName(String paramName);


    /**
     * The actual target to invoke actions upon.
     *
     * <p>
     *     For regular actions, returns same argument, but for mixin actions, will be an instance of the mixin.
     * </p>
     */
    ManagedObject realTargetAdapter(ManagedObject targetAdapter);

    // -- Parameters (per instance)

    /**
     * Returns a list of possible references/values for each parameter, which
     * the user can choose from.
     */
    CanVector<ManagedObject> getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy);

    default String getCssClass(final String prefix) {
        final String ownerId = getDeclaringType().getLogicalTypeName().replace(".", "-");
        return prefix + ownerId + "-" + getId();
    }

    /**
     * Returns a serializable representation of this action.
     */
    default ActionMemento getMemento() {
        return ActionMemento.forAction(this);
    }

    // -- UTIL

    public static final class Util {

        public static boolean isNoParameters(final ObjectAction objectAction) {
            return objectAction.getParameterCount()==0;
        }

        public static boolean returnsBlobOrClob(final ObjectAction objectAction) {
            final ObjectSpecification returnType = objectAction.getReturnType();
            if (returnType != null) {
                Class<?> cls = returnType.getCorrespondingClass();
                if (Blob.class.isAssignableFrom(cls)
                        || Clob.class.isAssignableFrom(cls)) {
                    return true;
                }
            }
            return false;
        }

        public static ActionLayout.Position actionLayoutPositionOf(final ObjectAction action) {
            return action.lookupFacet(ActionPositionFacet.class)
            .map(ActionPositionFacet::position)
            .orElse(ActionLayout.Position.BELOW);
        }

        public static Optional<CssClassFaFactory> cssClassFaFactoryFor(
                final ObjectAction action,
                final ManagedObject domainObject) {

            return Optional.ofNullable(action.getFacet(CssClassFaFacet.class))
            .map(CssClassFaFacet::getSpecialization)
            .map(specialization->specialization
                    .fold(
                            hasStaticFaIcon->hasStaticFaIcon, // identity operator
                            hasImperativeFaIcon->
                                ManagedObjects.isNullOrUnspecifiedOrEmpty(domainObject)
                                    ? null
                                    : hasImperativeFaIcon.getCssClassFaFactory(domainObject.asProvider())))
            .filter(_NullSafe::isPresent);
        }

        /**
         * Returns a Stream of those to be rendered with the entity header panel.
         */
        public static Stream<ObjectAction> streamTopBarActions(
                final ManagedObject adapter) {

            val spec = adapter.getSpecification();

            return spec.streamRuntimeActions(MixedIn.INCLUDED)
            .filter(Predicates
                    .isSharingAnyLayoutGroupOf(spec.streamAssociations(MixedIn.INCLUDED))
                    .negate())
            .filter(Predicates
                    .dynamicallyVisible(adapter, InteractionInitiatedBy.USER, Where.ANYWHERE));
        }

        public static Stream<ObjectAction> findForAssociation(
                final ManagedObject adapter,
                final ObjectAssociation association) {

            val spec = adapter.getSpecification();

            return spec.streamRuntimeActions(MixedIn.INCLUDED)
            .filter(Predicates.isSameLayoutGroupAs(association))
            .sorted(Comparators.byMemberOrderSequence(false));
        }

        public static PromptStyle promptStyleFor(final ObjectAction objectAction) {
            PromptStyleFacet facet = objectAction.getFacet(PromptStyleFacet.class);
            if(facet == null) {
                // don't think this can occur, see PromptStyleFallback
                return PromptStyle.INLINE;
            }
            final PromptStyle promptStyle = facet.value();
            if(promptStyle == PromptStyle.AS_CONFIGURED) {
                // don't think this can occur, see PromptStyleConfiguration
                return PromptStyle.INLINE;
            }
            return promptStyle;
        }

        public static String friendlyNameFor(
                final @NonNull ObjectAction action,
                final @NonNull InteractionHead head) {

            val mixeeAdapter = head.getMixedIn().orElse(null);

            if(mixeeAdapter != null) {
                val mixinSpec = action.getDeclaringType();
                val ownerSpec = mixeeAdapter.getSpecification();
                return ownerSpec.lookupMixedInMember(mixinSpec)
                        .map(mixedInMember->mixedInMember.getFriendlyName(mixeeAdapter.asProvider()))
                        .orElseThrow(_Exceptions::unexpectedCodeReach);
            }
            return action.getFriendlyName(head::getOwner);
        }
    }


    // -- Predicates

    public static final class Predicates {

        public static Predicate<ObjectAction> ofActionType(final ActionScope scope) {
            return (final ObjectAction oa) -> oa.getScope() == scope;
        }

        public static Predicate<ObjectAction> isPositioned(
                final ActionLayout.Position position) {
            return (final ObjectAction oa) -> ObjectAction.Util.actionLayoutPositionOf(oa) == position;
        }

        public static Predicate<ObjectAction> isSameLayoutGroupAs(
                final @NonNull ObjectAssociation association) {

            final String assocIdLower = association.getId();

            return (final ObjectAction objectAction) -> {

                val layoutGroupFacet = objectAction.getFacet(LayoutGroupFacet.class);
                if (layoutGroupFacet == null) {
                    return false;
                }
                val layoutGroupId = layoutGroupFacet.getGroupId();
                if (_Strings.isNullOrEmpty(layoutGroupId)) {
                    return false;
                }
                return layoutGroupId.equals(assocIdLower);
            };
        }

        private static Predicate<? super ObjectAction> isSharingAnyLayoutGroupOf(
                final @NonNull Stream<ObjectAssociation> streamOfAssociations) {

            final Set<String> associationIds = streamOfAssociations
                    .map(ObjectAssociation::getId)
                    .collect(Collectors.toCollection(HashSet::new));

            return (final ObjectAction objectAction) -> {

                val layoutGroupFacet = objectAction.getFacet(LayoutGroupFacet.class);
                if (layoutGroupFacet == null) {
                    return false;
                }
                val layoutGroupId = layoutGroupFacet.getGroupId();
                if (_Strings.isNullOrEmpty(layoutGroupId)) {
                    return false;
                }
                return associationIds.contains(layoutGroupId);
            };
        }

        public static Predicate<ObjectAction> choicesFromAndHavingCollectionParameterFor(
                final @NonNull OneToManyAssociation collection) {

            val elementType = collection.getElementType();

            return new ChoicesFrom(collection)
                    .and(new HasParameterMatching(
                            new ObjectActionParameter.Predicates.CollectionParameter(elementType)
                            ));
        }

        // -- HELPER

        private static class ChoicesFrom implements Predicate<ObjectAction> {
            private final @NonNull String memberId;
//            private final @NonNull String memberName;

            public ChoicesFrom(final @NonNull ObjectAssociation objectAssociation) {

                this.memberId = _Strings.nullToEmpty(objectAssociation.getId()).toLowerCase();
//                this.memberName = _Strings.nullToEmpty(objectAssociation.getName()).toLowerCase();
            }

            @Override
            public boolean test(final ObjectAction objectAction) {
                val choicesFromFacet = objectAction.getFacet(ChoicesFromFacet.class);
                if(choicesFromFacet == null) {
                    return false;
                }
                val choicesFromMemberName = choicesFromFacet.value();
                if (choicesFromMemberName == null) {
                    return false;
                }
                val memberNameLowerCase = choicesFromMemberName.toLowerCase();
//                return Objects.equals(memberName, memberNameLowerCase)
//                        || Objects.equals(memberId, memberNameLowerCase);
                return Objects.equals(memberId, memberNameLowerCase);
            }

        }

        private static class HasParameterMatching implements Predicate<ObjectAction> {
            private final Predicate<ObjectActionParameter> parameterPredicate;
            public HasParameterMatching(final Predicate<ObjectActionParameter> parameterPredicate) {
                this.parameterPredicate = parameterPredicate;
            }

            @Override
            public boolean test(final ObjectAction objectAction) {
                return stream(objectAction.getParameters())
                        .anyMatch(parameterPredicate);
            }
        }

        private static Predicate<ObjectAction> dynamicallyVisible(
                final ManagedObject target,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {

            return (final ObjectAction objectAction) -> {
                final Consent visible = objectAction.isVisible(target, interactionInitiatedBy, where);
                return visible.isAllowed();
            };
        }

    }



}
