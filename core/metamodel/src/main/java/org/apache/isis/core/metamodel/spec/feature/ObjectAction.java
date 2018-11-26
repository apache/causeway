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
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.commons.lang.StringFunctions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetFilters;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.AssociatedWithFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.isis.core.metamodel.facets.object.wizard.WizardFacet;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderFacetComparator;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface ObjectAction extends ObjectMember {

    //region > getSemantics, getOnType
    /**
     * The semantics of this action.
     */
    ActionSemantics.Of getSemantics();

    /**
     * Returns the specification for the type of object that this action can be
     * invoked upon.
     */
    ObjectSpecification getOnType();
    //endregion

    //region > getType, isPrototype

    ActionType getType();

    boolean isPrototype();

    //endregion

    //region > ReturnType
    /**
     * Returns the specifications for the return type.
     */
    ObjectSpecification getReturnType();

    /**
     * Returns <tt>true</tt> if the represented action returns a non-void object,
     * else returns false.
     */
    boolean hasReturn();

    //endregion

    //region > execute, executeWithRuleChecking

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters, checking the visibility, usability and validity first.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.  When a mixin action invokes its underlying mixedIn action, then will be populated (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    ObjectAdapter executeWithRuleChecking(
            final ObjectAdapter target,
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] parameters,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) throws AuthorizationException;

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters.
     *
     * @param mixedInAdapter - will be null for regular actions, and for mixin actions.  When a mixin action invokes its underlying mixedIn action, then will be populated (so that the ActionDomainEvent can correctly provide the underlying mixin)
     */
    ObjectAdapter execute(
            ObjectAdapter targetAdapter,
            ObjectAdapter mixedInAdapter,
            ObjectAdapter[] parameters,
            final InteractionInitiatedBy interactionInitiatedBy);

    //endregion

    //region > isProposedArgumentSetValid, isEachIndividualArgumentValid, isArgumentSetValid

    /**
     * Whether the provided argument set is valid, represented as a {@link Consent}.
     *
     * <p>
     *     Basically just calls (the helper methods also called by) first
     *     {@link #isEachIndividualArgumentValid(ObjectAdapter, ObjectAdapter[], InteractionInitiatedBy)} and then
     *     {@link #isArgumentSetValid(ObjectAdapter, ObjectAdapter[], InteractionInitiatedBy)}.  Those methods are
     *     separated out so that viewers have more fine-grained control.
     * </p>
     */
    Consent isProposedArgumentSetValid(
            ObjectAdapter object,
            ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy);

    Consent isEachIndividualArgumentValid(
            ObjectAdapter objectAdapter,
            ObjectAdapter[] proposedArguments,
            InteractionInitiatedBy interactionInitiatedBy);

    Consent isArgumentSetValid(
            ObjectAdapter objectAdapter,
            ObjectAdapter[] proposedArguments,
            InteractionInitiatedBy interactionInitiatedBy);


    //endregion

    //region > Parameters (declarative)

    /**
     * Returns the number of parameters used by this method.
     */
    int getParameterCount();

    /**
     * Returns set of parameter information.
     *
     * <p>
     * Implementations may build this array lazily or eagerly as required.
     *
     * @return
     */
    List<ObjectActionParameter> getParameters();

    /**
     * Returns the {@link ObjectSpecification type} of each of the {@link #getParameters() parameters}.
     */
    List<ObjectSpecification> getParameterTypes();

    /**
     * Returns set of parameter information matching the supplied filter.
     *
     * @return
     */
    List<ObjectActionParameter> getParameters(
            @SuppressWarnings("deprecation") Filter<ObjectActionParameter> filter);

    /**
     * Returns the parameter with provided id.
     */
    ObjectActionParameter getParameterById(String paramId);

    /**
     * Returns the parameter with provided name.
     */
    ObjectActionParameter getParameterByName(String paramName);

    //endregion

    //region > Parameters (per instance)

    /**
     * Returns the defaults references/values to be used for the action.
     */
    ObjectAdapter[] getDefaults(ObjectAdapter target);

    /**
     * Returns a list of possible references/values for each parameter, which
     * the user can choose from.
     */
    ObjectAdapter[][] getChoices(
            final ObjectAdapter target,
            final InteractionInitiatedBy interactionInitiatedBy);

    //endregion

    //region > setupBulkActionInvocationContext
    /**
     * internal API, called by {@link ActionInvocationFacet} if the action is actually executed (ie in the foreground).
     */
    void setupBulkActionInvocationContext(
            final ObjectAdapter targetAdapter);


    //endregion

    //region > Util
    public static final class Util {

        final static MemberOrderFacetComparator memberOrderFacetComparator = new MemberOrderFacetComparator(false);

        private Util() {
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
        	return SemanticsOf.from(objectAction.getSemantics());
        }
        
        public static boolean isAreYouSureSemantics(final ObjectAction objectAction) {
        	return semanticsOf(objectAction).isAreYouSure();
        }
        
        public static boolean isNonIdempotent(ObjectAction objectAction) {
        	return !semanticsOf(objectAction).isIdempotentInNature();
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

            final String className = action.getOnType().getSpecId().asString().replace(".","-");
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

        public static String cssClassFaFor(final ObjectAction action) {
            final CssClassFaFacet cssClassFaFacet = action.getFacet(CssClassFaFacet.class);
            return cssClassFaFacet != null ? cssClassFaFacet.value() : null;
        }

        public static CssClassFaPosition cssClassFaPositionFor(final ObjectAction action) {
            CssClassFaFacet facet = action.getFacet(CssClassFaFacet.class);
            return facet != null ? facet.getPosition() : CssClassFaPosition.LEFT;
        }

        public static String cssClassFor(final ObjectAction action, final ObjectAdapter objectAdapter) {
            final CssClassFacet cssClassFacet = action.getFacet(CssClassFacet.class);
            return cssClassFacet != null ? cssClassFacet.cssClass(objectAdapter) : null;
        }


        public static List<ObjectAction> findTopLevel(
                final ObjectAdapter adapter,
                final DeploymentCategory deploymentCategory) {
            final List<ObjectAction> topLevelActions = Lists.newArrayList();

            addTopLevelActions(adapter, ActionType.USER, topLevelActions);
            if(deploymentCategory.isPrototyping()) {
                addTopLevelActions(adapter, ActionType.PROTOTYPE, topLevelActions);
            }
            return topLevelActions;
        }

        static void addTopLevelActions(
                final ObjectAdapter adapter,
                final ActionType actionType,
                final List<ObjectAction> topLevelActions) {

            final ObjectSpecification adapterSpec = adapter.getSpecification();

            @SuppressWarnings({ "unchecked", "deprecation" })
            Filter<ObjectAction> filter = org.apache.isis.applib.filter.Filters.and(
                    Filters.memberOrderNotAssociationOf(adapterSpec),
                    Filters.dynamicallyVisible(adapter, InteractionInitiatedBy.USER, Where.ANYWHERE),
                    Filters.notBulkOnly(),
                    Filters.excludeWizardActions(adapterSpec));

            final List<ObjectAction> userActions = adapterSpec.getObjectActions(actionType, Contributed.INCLUDED, filter);
            topLevelActions.addAll(userActions);
        }


        public static List<ObjectAction> findForAssociation(
                final ObjectAdapter adapter,
                final ObjectAssociation association, final DeploymentCategory deploymentCategory) {
            final List<ObjectAction> associatedActions = Lists.newArrayList();

            addActions(adapter, ActionType.USER, association, associatedActions);
            if(deploymentCategory.isPrototyping()) {
                addActions(adapter, ActionType.PROTOTYPE, association, associatedActions);
            }

            Collections.sort(associatedActions, new Comparator<ObjectAction>() {

                @Override
                public int compare(ObjectAction o1, ObjectAction o2) {
                    final MemberOrderFacet m1 = o1.getFacet(MemberOrderFacet.class);
                    final MemberOrderFacet m2 = o2.getFacet(MemberOrderFacet.class);
                    return memberOrderFacetComparator.compare(m1, m2);
                }
            });
            return associatedActions;
        }

        static List<ObjectAction> addActions(
                final ObjectAdapter adapter,
                final ActionType type,
                final ObjectAssociation association, final List<ObjectAction> associatedActions) {
            final ObjectSpecification objectSpecification = adapter.getSpecification();

            @SuppressWarnings({ "unchecked", "deprecation" })
            Filter<ObjectAction> filter = org.apache.isis.applib.filter.Filters.and(
                    Filters.memberOrderOf(association),
                    // visibility needs to be determined at point of rendering, by ActionLink itself
                    // Filters.dynamicallyVisible(adapter, InteractionInitiatedBy.USER, Where.ANYWHERE),
                    Filters.notBulkOnly(),
                    Filters.excludeWizardActions(objectSpecification));

            final List<ObjectAction> userActions = objectSpecification.getObjectActions(type, Contributed.INCLUDED, filter);
            associatedActions.addAll(userActions);
            return userActions;
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
    }

    //endregion

    //region > Predicates

    public static final class Predicates {

        private Predicates() {
        }

        public static Predicate<ObjectAction> dynamicallyVisible(
                final ObjectAdapter target,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {
            return org.apache.isis.applib.filter.Filters
                    .asPredicate(Filters.dynamicallyVisible(target, interactionInitiatedBy, where));
        }

        public static Predicate<ObjectAction> withId(final String actionId) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.withId(actionId));
        }

        public static Predicate<ObjectAction> withNoValidationRules() {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.withNoValidationRules());
        }

        public static Predicate<ObjectAction> ofType(final ActionType type) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.ofType(type));
        }

        public static Predicate<ObjectAction> bulk() {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.bulk());
        }

        // UNUSED?
        public static Predicate<ObjectAction> notBulkOnly() {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.notBulkOnly());
        }

        public static Predicate<ObjectAction> memberOrderOf(ObjectAssociation association) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.memberOrderOf(association));
        }

        public static Predicate<ObjectAction> associatedWith(final ObjectAssociation objectAssociation) {
            return new AssociatedWith(objectAssociation);
        }

        public static Predicate<ObjectAction> associatedWithAndWithCollectionParameterFor(
                final OneToManyAssociation collection) {

            final ObjectSpecification collectionTypeOfSpec = collection.getSpecification();

            return com.google.common.base.Predicates.and(
                    new AssociatedWith(collection),
                    new HasParameterMatching(
                        new ObjectActionParameter.Predicates.CollectionParameter(collectionTypeOfSpec)
                    )
            );
        }

        public static class AssociatedWith implements Predicate<ObjectAction> {
            private final String memberId;
            private final String memberName;

            public AssociatedWith(final ObjectAssociation objectAssociation) {
                this.memberId = objectAssociation.getId();
                this.memberName = objectAssociation.getName();
            }

            @Override
            public boolean apply(final ObjectAction objectAction) {
                final AssociatedWithFacet associatedWithFacet = objectAction.getFacet(AssociatedWithFacet.class);
                if(associatedWithFacet == null) {
                    return false;
                }
                final String associatedMemberName = associatedWithFacet.value();
                if (associatedMemberName == null) {
                    return false;
                }
                final String memberOrderNameLowerCase = associatedMemberName.toLowerCase();
                return memberName != null && Objects.equal(memberName.toLowerCase(), memberOrderNameLowerCase) ||
                       memberId   != null && Objects.equal(memberId.toLowerCase(), memberOrderNameLowerCase);
            }
        }

        public static class HasParameterMatching implements Predicate<ObjectAction> {
            private final Predicate<ObjectActionParameter> parameterPredicate;
            public HasParameterMatching(final Predicate<ObjectActionParameter> parameterPredicate) {
                this.parameterPredicate = parameterPredicate;
            }

            @Override
            public boolean apply(@Nullable final ObjectAction objectAction) {
                return FluentIterable
                        .from(objectAction.getParameters())
                        .anyMatch(parameterPredicate);
            }
        }
    }

    //endregion

    //region > Filters

    public static final class Filters {

        private Filters() {
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> dynamicallyVisible(
                final ObjectAdapter target,
                final InteractionInitiatedBy interactionInitiatedBy,
                final Where where) {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(final ObjectAction objectAction) {
                    final Consent visible = objectAction.isVisible(target, interactionInitiatedBy, where);
                    return visible.isAllowed();
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> withId(final String actionId) {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(ObjectAction objectAction) {
                    return objectAction.getId().equals(actionId);
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> withNoValidationRules() {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(final ObjectAction objectAction) {
                    final List<Facet> validatingFacets = objectAction.getFacets(FacetFilters
                            .isA(ValidatingInteractionAdvisor.class));
                    return validatingFacets.isEmpty();
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> ofType(final ActionType type) {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(ObjectAction oa) {
                    return oa.getType() == type;
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> bulk() {
            return new Filter<ObjectAction>() {

                @Override
                public boolean accept(ObjectAction oa) {

                    final BulkFacet bulkFacet = oa.getFacet(BulkFacet.class);
                    if(bulkFacet == null || bulkFacet.isNoop() || bulkFacet.value() == Bulk.AppliesTo.REGULAR_ONLY) {
                        return false;
                    }
                    if (oa.getParameterCount() != 0) {
                        return false;
                    }

                    // currently don't support returning Blobs or Clobs
                    // (because haven't figured out how to rerender the current page, but also to do a download)
                    ObjectSpecification returnSpec = oa.getReturnType();
                    if (returnSpec != null) {
                        Class<?> returnType = returnSpec.getCorrespondingClass();
                        if (returnType == Blob.class || returnType == Clob.class) {
                            return false;
                        }
                    }
                    return true;
                }
            };
        }

        @Deprecated
        public static Filter<ObjectAction> notBulkOnly() {
            return new Filter<ObjectAction>() {

                @Override
                public boolean accept(ObjectAction t) {
                    BulkFacet facet = t.getFacet(BulkFacet.class);
                    return facet == null || facet.value() != Bulk.AppliesTo.BULK_ONLY;
                }
            };
        }

        public static Filter<ObjectAction> excludeWizardActions(final ObjectSpecification objectSpecification) {
            return org.apache.isis.applib.filter.Filters.not(wizardActions(objectSpecification));
            // return wizardActions(objectSpecification);
        }

        private static Filter<ObjectAction> wizardActions(final ObjectSpecification objectSpecification) {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(ObjectAction input) {
                    if (objectSpecification == null) {
                        return false;
                    }
                    final WizardFacet wizardFacet = objectSpecification.getFacet(WizardFacet.class);
                    return wizardFacet != null && wizardFacet.isWizardAction(input);
                }
            };
        }

        @SuppressWarnings("deprecation")
        public static Filter<ObjectAction> memberOrderOf(ObjectAssociation association) {
            final String assocName = association.getName();
            final String assocId = association.getId();
            return new Filter<ObjectAction>() {

                @Override
                public boolean accept(ObjectAction t) {
                    final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                    if (memberOrderFacet == null || Strings.isNullOrEmpty(memberOrderFacet.name())) {
                        return false;
                    }
                    final String memberOrderName = memberOrderFacet.name().toLowerCase();
                    if (Strings.isNullOrEmpty(memberOrderName)) {
                        return false;
                    }
                    return memberOrderName.equalsIgnoreCase(assocName) || memberOrderName.equalsIgnoreCase(assocId);
                }
            };
        }

        public static Filter<ObjectAction> memberOrderNotAssociationOf(final ObjectSpecification adapterSpec) {

            final List<ObjectAssociation> associations = adapterSpec.getAssociations(Contributed.INCLUDED);
            final List<String> associationNames = Lists.transform(associations,
                    com.google.common.base.Functions.compose(StringFunctions.toLowerCase(), ObjectAssociation.Functions.toName()));
            final List<String> associationIds = Lists.transform(associations,
                    com.google.common.base.Functions.compose(StringFunctions.toLowerCase(), ObjectAssociation.Functions.toId()));

            return new Filter<ObjectAction>() {

                @Override
                public boolean accept(ObjectAction t) {
                    final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                    if (memberOrderFacet == null || Strings.isNullOrEmpty(memberOrderFacet.name())) {
                        return true;
                    }
                    String memberOrderName = memberOrderFacet.name().toLowerCase();
                    if (Strings.isNullOrEmpty(memberOrderName)) {
                        return false;
                    }
                    return !associationNames.contains(memberOrderName) && !associationIds.contains(memberOrderName);
                }
            };
        }
    }

    //endregion

}
