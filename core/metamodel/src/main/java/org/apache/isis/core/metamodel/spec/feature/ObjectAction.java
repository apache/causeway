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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.collections.CanVector;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.AssociatedWithFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.wizard.WizardFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.MixedInMember;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.NonNull;
import lombok.val;

public interface ObjectAction extends ObjectMember {

    // -- getSemantics, getOnType
    /**
     * The semantics of this action.
     */
    SemanticsOf getSemantics();

    /**
     * Returns the specification for the type of object that this action can be
     * invoked upon.
     */
    ObjectSpecification getOnType();


    // -- getType, isPrototype

    ActionType getType();

    boolean isPrototype();



    // -- ReturnType
    /**
     * Returns the specifications for the return type.
     */
    ObjectSpecification getReturnType();

    /**
     * Returns <tt>true</tt> if the represented action returns a non-void object,
     * else returns false.
     */
    boolean hasReturn();



    // -- execute, executeWithRuleChecking

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


    // -- Model for Parameter Negotiation

    ActionInteractionHead interactionHead(
            @NonNull ManagedObject actionOwner);

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
     *
     * @return
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
     * Returns the defaults references/values to be used for the action.
     */
    Can<ManagedObject> getDefaults(ManagedObject target);

    /**
     * Returns a list of possible references/values for each parameter, which
     * the user can choose from.
     */
    CanVector<ManagedObject> getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy);

    default String getCssClass(String prefix) {
        final String ownerId = getOnType().getLogicalTypeName().replace(".", "-");
        return prefix + ownerId + "-" + getId();
    }

    // -- UTIL

    public static final class Util {

        private Util() {
        }

        private static boolean isPrototyping(ManagedObject adapter) {
            return MetaModelContext.from(adapter).getSystemEnvironment().isPrototyping();
        }

        public static String nameFor(final ObjectAction objAction) {
            final String actionName = objAction.getName();
            if (actionName != null) {
                return actionName;
            }
            final NamedFacet namedFacet = objAction.getFacet(NamedFacet.class);
            if (namedFacet != null) {
                return namedFacet.value();
            }
            return "(no name)";
        }

        public static SemanticsOf semanticsOf(final ObjectAction objectAction) {
            return objectAction.getSemantics();
        }

        public static boolean isAreYouSureSemantics(final ObjectAction objectAction) {
            return semanticsOf(objectAction).isAreYouSure();
        }

        public static boolean isIdempotentOrCachable(ObjectAction objectAction) {
            final SemanticsOf semantics = semanticsOf(objectAction);
            return semantics.isIdempotentInNature() || semantics.isSafeAndRequestCacheable();
        }

        public static boolean isNoParameters(ObjectAction objectAction) {
            return objectAction.getParameterCount()==0;
        }

        public static boolean returnsBlobOrClob(final ObjectAction objectAction) {
            final ObjectSpecification returnType = objectAction.getReturnType();
            if (returnType != null) {
                Class<?> cls = returnType.getCorrespondingClass();
                if (Blob.class.isAssignableFrom(cls) || Clob.class.isAssignableFrom(cls)) {
                    return true;
                }
            }
            return false;
        }

        public static String actionIdentifierFor(final ObjectAction action) {
            @SuppressWarnings("unused")
            final Identifier identifier = action.getIdentifier();

            final String className = action.getOnType().getLogicalTypeName().replace(".","-");
            final String actionId = action.getId();
            return className + "-" + actionId;
        }

        public static String descriptionOf(ObjectAction action) {
            return action.getDescription();
        }

        public static ActionLayout.Position actionLayoutPositionOf(ObjectAction action) {
            final ActionPositionFacet layoutFacet = action.getFacet(ActionPositionFacet.class);
            return layoutFacet != null ? layoutFacet.position() : ActionLayout.Position.BELOW;
        }

        public static Optional<CssClassFaFacet> cssClassFaFacetFor(final ObjectAction action) {
            return Optional.ofNullable(action)
            .map(a->a.getFacet(CssClassFaFacet.class));
        }

        public static String cssClassFor(final ObjectAction action, final ManagedObject objectAdapter) {
            final CssClassFacet cssClassFacet = action.getFacet(CssClassFacet.class);
            return cssClassFacet != null ? cssClassFacet.cssClass(objectAdapter) : null;
        }

        public static List<ObjectAction> findTopLevel(
                final ManagedObject adapter) {

            val topLevelActions = _Lists.<ObjectAction>newArrayList();

            addTopLevelActions(adapter, ActionType.USER, topLevelActions);
            if(isPrototyping(adapter)) {
                addTopLevelActions(adapter, ActionType.PROTOTYPE, topLevelActions);
            }
            return topLevelActions;
        }

        static void addTopLevelActions(
                final ManagedObject adapter,
                final ActionType actionType,
                final List<ObjectAction> topLevelActions) {

            val spec = adapter.getSpecification();

            spec.streamDeclaredActions(actionType, MixedIn.INCLUDED)
            .filter(ObjectAction.Predicates.memberOrderNotAssociationOf(spec))
            .filter(ObjectAction.Predicates.dynamicallyVisible(adapter,
                    InteractionInitiatedBy.USER, Where.ANYWHERE))
            .filter(ObjectAction.Predicates.excludeWizardActions(spec))
            .forEach(topLevelActions::add);

        }

        public static List<ObjectAction> findForAssociation(
                final ManagedObject adapter,
                final ObjectAssociation association) {

            val associatedActions = _Lists.<ObjectAction>newArrayList();

            addActions(adapter, ActionType.USER, association, associatedActions);
            if(isPrototyping(adapter)) {
                addActions(adapter, ActionType.PROTOTYPE, association, associatedActions);
            }

            Collections.sort(associatedActions, Comparators.byMemberOrderSequence(false));
            return associatedActions;
        }

        static void addActions(
                final ManagedObject adapter,
                final ActionType type,
                final ObjectAssociation association, final List<ObjectAction> associatedActions) {

            val objectSpecification = adapter.getSpecification();

            objectSpecification.streamDeclaredActions(type, MixedIn.INCLUDED)
            .filter(ObjectAction.Predicates.actionIsAssociatedWith(association))
            .filter(ObjectAction.Predicates.excludeWizardActions(objectSpecification))
            .forEach(associatedActions::add);
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

        public static Optional<String> targetNameFor(
                final ObjectAction owningAction,
                final @Nullable ManagedObject mixedInAdapter) {

            if(mixedInAdapter != null) {
                final ObjectSpecification onType = owningAction.getOnType();
                final ObjectSpecification mixedInSpec = mixedInAdapter.getSpecification();
                final Optional<String> mixinName = mixedInSpec.getMixedInMember(onType)
                        .map(MixedInMember::getName);

                return mixinName;
            }
            return Optional.empty();
        }
    }


    // -- Predicates

    public static final class Predicates {

        private Predicates() {
        }

        public static Predicate<ObjectAction> associatedWith(final ObjectAssociation objectAssociation) {
            return new AssociatedWith(objectAssociation);
        }

        public static Predicate<ObjectAction> associatedWithAndWithCollectionParameterFor(
                final OneToManyAssociation collection) {

            final ObjectSpecification collectionTypeOfSpec = collection.getSpecification();

            return new AssociatedWith(collection)
                    .and(new HasParameterMatching(
                            new ObjectActionParameter.Predicates.CollectionParameter(collectionTypeOfSpec)
                            ));
        }

        public static class AssociatedWith implements Predicate<ObjectAction> {
            private final String memberId;
            private final String memberName;

            public AssociatedWith(final ObjectAssociation objectAssociation) {
                this.memberId = objectAssociation.getId();
                this.memberName = objectAssociation.getName();
            }

            @Override
            public boolean test(final ObjectAction objectAction) {
                val associatedWithFacet = objectAction.getFacet(AssociatedWithFacet.class);
                if(associatedWithFacet == null) {
                    return false;
                }
                val associatedMemberName = associatedWithFacet.value();
                if (associatedMemberName == null) {
                    return false;
                }
                val memberOrderNameLowerCase = associatedMemberName.toLowerCase();
                return equalWhenLowerCase(memberName, memberOrderNameLowerCase)
                        || equalWhenLowerCase(memberId, memberOrderNameLowerCase);
            }

            private boolean equalWhenLowerCase(@Nullable String string, String lowerCaseString) {
                return string != null && Objects.equals(string.toLowerCase(), lowerCaseString);
            }

        }

        public static class HasParameterMatching implements Predicate<ObjectAction> {
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

        public static Predicate<ObjectAction> ofType(final ActionType type) {
            return (ObjectAction oa) -> oa.getType() == type;
        }

        public static Predicate<ObjectAction> dynamicallyVisible(
                final ManagedObject target,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {

            return (ObjectAction objectAction) -> {
                final Consent visible = objectAction.isVisible(target, interactionInitiatedBy, where);
                return visible.isAllowed();
            };
        }

        public static Predicate<ObjectAction> excludeWizardActions(final ObjectSpecification objectSpecification) {
            return wizardActions(objectSpecification).negate();
        }

        private static Predicate<ObjectAction> wizardActions(final ObjectSpecification objectSpecification) {
            return (ObjectAction input) -> {
                if (objectSpecification == null) {
                    return false;
                }
                val wizardFacet = objectSpecification.getFacet(WizardFacet.class);
                return wizardFacet != null && wizardFacet.isWizardAction(input);
            };
        }

        public static Predicate<ObjectAction> actionIsAssociatedWith(ObjectAssociation association) {
            final String assocName = association.getName();
            final String assocId = association.getId();
            return (ObjectAction objectAction) -> {

                val layoutGroupFacet = objectAction.getFacet(LayoutGroupFacet.class);
                if (layoutGroupFacet == null) {
                    return false;
                }
                val fieldSetId = layoutGroupFacet.getGroupId();
                if (_Strings.isNullOrEmpty(fieldSetId)) {
                    return false;
                }
                return fieldSetId.equalsIgnoreCase(assocName)
                        || fieldSetId.equalsIgnoreCase(assocId);
            };
        }

        public static Predicate<ObjectAction> memberOrderNotAssociationOf(final ObjectSpecification adapterSpec) {

            final Set<String> associationNamesAndIds = _Sets.newHashSet();

            adapterSpec.streamAssociations(MixedIn.INCLUDED)
            .forEach(ass->{
                associationNamesAndIds.add(_Strings.lower(ass.getName()));
                associationNamesAndIds.add(_Strings.lower(ass.getId()));
            });

            return (ObjectAction objectAction) -> {

                val layoutGroupFacet = objectAction.getFacet(LayoutGroupFacet.class);
                if (layoutGroupFacet == null) {
                    return true;
                }
                val fieldSetId = layoutGroupFacet.getGroupId();
                if (_Strings.isNullOrEmpty(fieldSetId)) {
                    return true;
                }
                return !associationNamesAndIds.contains(fieldSetId.toLowerCase());
            };
        }
    }



}
