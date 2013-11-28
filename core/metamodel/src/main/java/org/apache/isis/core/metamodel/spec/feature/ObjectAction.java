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

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetFilters;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.named.NamedFacet;
import org.apache.isis.core.metamodel.interactions.AccessContext;
import org.apache.isis.core.metamodel.interactions.ActionInvocationContext;
import org.apache.isis.core.metamodel.interactions.ValidatingInteractionAdvisor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.progmodel.facets.actions.bulk.BulkFacet;

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
    // execute
    // //////////////////////////////////////////////////////////////////

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
    List<ObjectActionParameter> getParameters(Filter<ObjectActionParameter> filter);

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

        public static String nameFor(final ObjectAction noAction) {
            final String actionName = noAction.getName();
            if (actionName != null) {
                return actionName;
            }
            final NamedFacet namedFacet = noAction.getFacet(NamedFacet.class);
            if (namedFacet != null) {
                return namedFacet.value();
            }
            return "(no name)";
        }
    }


    // //////////////////////////////////////////////////////
    // Filters
    // //////////////////////////////////////////////////////

    
    public static final class Filters {
        
        private Filters(){}
        
        public static final Filter<ObjectAction> VISIBLE_AT_LEAST_SOMETIMES = new Filter<ObjectAction>() {
            @Override
            public boolean accept(final ObjectAction action) {
                final HiddenFacet hiddenFacet = action.getFacet(HiddenFacet.class);
                return hiddenFacet == null || hiddenFacet.when() != When.ALWAYS || hiddenFacet.where() != Where.ANYWHERE;
            }
        };

        public static Filter<ObjectAction> dynamicallyVisible(final AuthenticationSession session, final ObjectAdapter target, final Where where) {
            return new Filter<ObjectAction>() {
                @Override
                public boolean accept(final ObjectAction objectAction) {
                    final Consent visible = objectAction.isVisible(session, target, where);
                    return visible.isAllowed();
                }
            };
        }

        public static Filter<ObjectAction> withId(final String actionId) {
            return new Filter<ObjectAction>(){
                @Override
                public boolean accept(ObjectAction objectAction) {
                    return objectAction.getId().equals(actionId);
                }
            };
        }

        public static Filter<ObjectAction> withNoValidationRules() {
            return new Filter<ObjectAction>(){
                @Override
                public boolean accept(final ObjectAction objectAction) {
                    final List<Facet> validatingFacets = objectAction.getFacets(FacetFilters.isA(ValidatingInteractionAdvisor.class));
                    return validatingFacets.isEmpty();
                }};
        }

        public static Filter<ObjectAction> ofType(final ActionType type) {
            return new Filter<ObjectAction>(){
                @Override
                public boolean accept(ObjectAction oa) {
                    return oa.getType() == type;
                }
            };
        }

        public static Filter<ObjectAction> bulk() {
            return new Filter<ObjectAction>(){

                @Override
                public boolean accept(ObjectAction oa) {
                    return oa.containsDoOpFacet(BulkFacet.class);
                }};
        }
        
        public static Filter<ObjectAction> memberOrderOf(ObjectAssociation association) {
            final String assocName = association.getName();
            final String assocId = association.getId();
            return new Filter<ObjectAction>() {
        
                @Override
                public boolean accept(ObjectAction t) {
                    final MemberOrderFacet memberOrderFacet = t.getFacet(MemberOrderFacet.class);
                    if(memberOrderFacet == null) {
                        return false; 
                    }
                    final String memberOrderName = memberOrderFacet.name();
                    if(Strings.isNullOrEmpty(memberOrderName)) {
                        return false;
                    }
                    return memberOrderName.equalsIgnoreCase(assocName) || memberOrderName.equalsIgnoreCase(assocId);
                }
            };
        }

    }
}
