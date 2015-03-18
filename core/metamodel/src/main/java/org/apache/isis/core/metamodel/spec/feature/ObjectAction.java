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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.List;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.lang.StringFunctions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetFilters;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.wizard.WizardFacet;
import org.apache.isis.core.metamodel.interactions.AccessContext;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface ObjectAction extends ObjectMember {


    // //////////////////////////////////////////////////////
    // semantics, realTarget, getOnType
    // //////////////////////////////////////////////////////

    /**
     * The semantics of this action.
     */
    ActionSemantics.Of getSemantics();

    /**
     * Returns the specification for the type of object that this action can be
     * invoked upon.
     */
    ObjectSpecification getOnType();


    boolean promptForParameters(ObjectAdapter target);

    // //////////////////////////////////////////////////////////////////
    // Type
    // //////////////////////////////////////////////////////////////////

    ActionType getType();

    // //////////////////////////////////////////////////////////////////
    // ReturnType
    // //////////////////////////////////////////////////////////////////

    /**
     * Returns the specifications for the return type.
     */
    ObjectSpecification getReturnType();

    /**
     * Returns <tt>true</tt> if the represented action returns a non-void object, 
     * else returns false.
     */
    boolean hasReturn();

    // //////////////////////////////////////////////////////////////////
    // execute, executeWithRuleChecking
    // //////////////////////////////////////////////////////////////////

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters, checking the visibility, usability and validity first.
     */
    ObjectAdapter executeWithRuleChecking(
            final ObjectAdapter target,
            final ObjectAdapter[] parameters,
            final AuthenticationSession authenticationSession,
            final Where where) throws AuthorizationException;

    /**
     * Invokes the action's method on the target object given the specified set
     * of parameters.
     */
    ObjectAdapter execute(ObjectAdapter target, ObjectAdapter[] parameters);

    // //////////////////////////////////////////////////////////////////
    // valid
    // //////////////////////////////////////////////////////////////////

    /**
     * Creates an {@link ActionInvocationContext interaction context}
     * representing an attempt to invoke this action.
     * 
     * <p>
     * Typically it is easier to just call
     * {@link #isProposedArgumentSetValid(ObjectAdapter, ObjectAdapter[])
     * 
     * @link #isProposedArgumentSetValidResultSet(ObjectAdapter,
     *       ObjectAdapter[])}; this is provided as API for symmetry with
     *       interactions (such as {@link AccessContext} accesses) have no
     *       corresponding vetoing methods.
     */
    public ActionInvocationContext createActionInvocationInteractionContext(AuthenticationSession session, InteractionInvocationMethod invocationMethod, ObjectAdapter targetObject, ObjectAdapter[] proposedArguments);

    /**
     * Whether the provided argument set is valid, represented as a
     * {@link Consent}.
     */
    Consent isProposedArgumentSetValid(ObjectAdapter object, ObjectAdapter[] proposedArguments);

    // //////////////////////////////////////////////////////
    // Parameters (declarative)
    // //////////////////////////////////////////////////////

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
     * Returns the {@link ObjectSpecification type} of each of the
     * {@link #getParameters() parameters}.
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

    // //////////////////////////////////////////////////////
    // Parameters (per instance)
    // //////////////////////////////////////////////////////

    /**
     * Returns the defaults references/values to be used for the action.
     */
    ObjectAdapter[] getDefaults(ObjectAdapter target);

    /**
     * Returns a list of possible references/values for each parameter, which
     * the user can choose from.
     */
    ObjectAdapter[][] getChoices(ObjectAdapter target);


    // //////////////////////////////////////////////////////
    // Utils
    // //////////////////////////////////////////////////////

    public static final class Utils {

        private Utils() {
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

        public static boolean returnsBlobOrClob(final ObjectAction objectAction) {
            boolean blobOrClob = false;
            final ObjectSpecification returnType = objectAction.getReturnType();
            if(returnType != null) {
                Class<?> cls = returnType.getCorrespondingClass();
                if (Blob.class.isAssignableFrom(cls) || Clob.class.isAssignableFrom(cls)) {
                    blobOrClob = true;
                }
            }
            return blobOrClob;
        }

        public static boolean isExplorationOrPrototype(final ObjectAction action) {
            return action.getType().isExploration() || action.getType().isPrototype();
        }

        public static String actionIdentifierFor(final ObjectAction action) {
            @SuppressWarnings("unused")
            final Identifier identifier = action.getIdentifier();

            final String className = action.getOnType().getShortIdentifier();
            final String actionId = action.getId();
            return className + "-" + actionId;
        }

        public static String descriptionOf(ObjectAction action) {
            return action.getDescription();
        }

        public static ActionLayout.Position actionLayoutPositionOf(ObjectAction action) {
            final ActionPositionFacet layoutFacet = action.getFacet(ActionPositionFacet.class);
            return layoutFacet != null? layoutFacet.position(): ActionLayout.Position.BELOW;
        }

        public static String cssClassFaFor(final ObjectAction action) {
            final CssClassFaFacet cssClassFaFacet = action.getFacet(CssClassFaFacet.class);
            return cssClassFaFacet != null ? cssClassFaFacet.value() : null;
        }

        public static ActionLayout.CssClassFaPosition cssClassFaPositionFor(final ObjectAction action) {
            CssClassFaFacet facet = action.getFacet(CssClassFaFacet.class);
            return facet != null ? facet.getPosition() : ActionLayout.CssClassFaPosition.LEFT;
        }

        public static String cssClassFor(final ObjectAction action, final ObjectAdapter objectAdapter) {
            final CssClassFacet cssClassFacet = action.getFacet(CssClassFacet.class);
            return cssClassFacet != null ? cssClassFacet.cssClass(objectAdapter) : null;
        }

    }


    // //////////////////////////////////////////////////////
    // Predicates
    // //////////////////////////////////////////////////////

    public static final class Predicates {

        private Predicates(){}

        public static Predicate<ObjectAction> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.dynamicallyVisible(session, target, where));
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

        public static Predicate<ObjectAction> notBulkOnly() {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.notBulkOnly());
        }

        public static Predicate<ObjectAction> memberOrderOf(ObjectAssociation association) {
            return org.apache.isis.applib.filter.Filters.asPredicate(Filters.memberOrderOf(association));
        }
    }


    // //////////////////////////////////////////////////////
    // Filters
    // //////////////////////////////////////////////////////

    
    public static final class Filters {
        
        private Filters(){}

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(final ObjectAction objectAction) {
                    final Consent visible = objectAction.isVisible(session, target, where);
                    return visible.isAllowed();
                }
            };
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> withId(final String actionId) {
            return new Filter<ObjectAction>(){
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
            return new Filter<ObjectAction>(){
                @Override
                public boolean accept(final ObjectAction objectAction) {
                    final List<Facet> validatingFacets = objectAction.getFacets(FacetFilters.isA(ValidatingInteractionAdvisor.class));
                    return validatingFacets.isEmpty();
                }};
        }

        /**
         * @deprecated -use {@link com.google.common.base.Predicate equivalent}
         */
        @Deprecated
        public static Filter<ObjectAction> ofType(final ActionType type) {
            return new Filter<ObjectAction>(){
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
            return new Filter<ObjectAction>(){

                @Override
                public boolean accept(ObjectAction oa) {
                    if( !oa.containsDoOpFacet(BulkFacet.class)) {
                        return false;
                    }
                    if (oa.getParameterCount() != 0) {
                        return false;
                    } 
                    
                    // currently don't support returning Blobs or Clobs
                    // (because haven't figured out how to rerender the current page, but also to do a download)
                    ObjectSpecification returnSpec = oa.getReturnType();
                    if(returnSpec != null) {
                        Class<?> returnType = returnSpec.getCorrespondingClass();
                        if(returnType == Blob.class || returnType == Clob.class) {
                            return false;
                        }
                    }
                    return true;
                }};
        }

        @Deprecated
        public static Filter<ObjectAction> notBulkOnly() {
            return new Filter<ObjectAction>(){

                @Override
                public boolean accept(ObjectAction t) {
                    BulkFacet facet = t.getFacet(BulkFacet.class);
                    return facet == null || facet.value() != Bulk.AppliesTo.BULK_ONLY;
                }};
        }

        public static Filter<ObjectAction> excludeWizardActions(final ObjectSpecification objectSpecification) {
            return org.apache.isis.applib.filter.Filters.not(wizardActions(objectSpecification));
            //return wizardActions(objectSpecification);
        }

        private static Filter<ObjectAction> wizardActions(final ObjectSpecification objectSpecification) {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(ObjectAction input) {
                    if(objectSpecification == null) {
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
                    if(memberOrderFacet == null || Strings.isNullOrEmpty(memberOrderFacet.name())) {
                        return false;
                    }
                    final String memberOrderName = memberOrderFacet.name().toLowerCase();
                    if(Strings.isNullOrEmpty(memberOrderName)) {
                        return false;
                    }
                    return memberOrderName.equalsIgnoreCase(assocName) || memberOrderName.equalsIgnoreCase(assocId);
                }
            };
        }

        public static Filter<ObjectAction> memberOrderNotAssociationOf(final ObjectSpecification adapterSpec) {

            final List<ObjectAssociation> associations = adapterSpec.getAssociations(Contributed.INCLUDED);
            final List<String> associationNames = Lists.transform(associations, Functions.compose(StringFunctions.toLowerCase(), ObjectAssociation.Functions.toName()));
            final List<String> associationIds = Lists.transform(associations, Functions.compose(StringFunctions.toLowerCase(), ObjectAssociation.Functions.toId()));

            return new Filter<ObjectAction>() {

                @Override
                public boolean accept(ObjectAction t) {
                    final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                    if(memberOrderFacet == null || Strings.isNullOrEmpty(memberOrderFacet.name())) {
                        return true;
                    }
                    String memberOrderName = memberOrderFacet.name().toLowerCase();
                    if(Strings.isNullOrEmpty(memberOrderName)) {
                        return false;
                    }
                    return !associationNames.contains(memberOrderName) && !associationIds.contains(memberOrderName);
                }
            };
        }
    }
}
