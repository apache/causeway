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

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Analogous to {@link ObjectAssociation}.
 */
public interface ObjectActionParameter extends ObjectFeature, CurrentHolder {

    /**
     * Owning {@link ObjectAction}.
     */
    ObjectAction getAction();

    /**
     * Returns a flag indicating if it can be left unset when the action can be
     * invoked.
     */
    boolean isOptional();

    /**
     * Returns the 0-based index to this parameter.
     */
    int getNumber();

    /**
     * Returns the name of this parameter.
     * 
     * <p>
     * Because Java's reflection API does not allow us to access the code name
     * of the parameter, we have to do figure out the name of the parameter
     * ourselves:
     * <ul>
     * <li>If there is a {@link NamedFacet} associated with this parameter then
     * we infer a name from this, eg "First Name" becomes "firstName".
     * <li>Otherwise we use the type, eg "string".
     * <li>If there is more than one parameter of the same type, then we use a
     * numeric suffix (eg "string1", "string2"). Wrappers and primitives are
     * considered to be the same type.
     * </ul>
     */
    @Override
    String getName();

    // internal API
    ActionArgValidityContext createProposedArgumentInteractionContext(
            final ObjectAdapter targetObject,
            final ObjectAdapter[] args,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy);


    /**
     * Whether there is an autoComplete provided (eg <tt>autoCompleteXxx</tt> supporting
     * method) for the parameter.
     */
    boolean hasAutoComplete();
    
    /**
     * Returns a list of possible references/values for this parameter, which the
     * user can choose from, based on the input search argument.
     */
    ObjectAdapter[] getAutoComplete(
            final ObjectAdapter adapter,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy);

    
    
    int getAutoCompleteMinLength();
    /**
     * Whether there are any choices provided (eg <tt>choicesXxx</tt> supporting
     * method) for the parameter.
     */
    boolean hasChoices();

    /**
     * Returns a list of possible references/values for this parameter, which the
     * user can choose from.
     */
    ObjectAdapter[] getChoices(
            final ObjectAdapter adapter,
            final ObjectAdapter[] argumentsIfAvailable,
            final InteractionInitiatedBy interactionInitiatedBy);


    ObjectAdapter getDefault(
            ObjectAdapter adapter,
            final ObjectAdapter[] argumentsIfAvailable,
            final Integer paramNumUpdated);

    
    /**
     * Whether proposed value for this parameter is valid.
     * 
     * @param adapter
     * @param proposedValue
     * @param interactionInitiatedBy
     * @return
     */
    String isValid(
            final ObjectAdapter adapter,
            final Object proposedValue,
            final InteractionInitiatedBy interactionInitiatedBy);
 

    
    public static class Filters {
        private Filters(){}
        
        /**
         * Filters only parameters that are for objects (ie 1:1 associations)
         */
        public static final Filter<ObjectActionParameter> PARAMETER_ASSOCIATIONS = new Filter<ObjectActionParameter>() {
            @Override
            public boolean accept(final ObjectActionParameter parameter) {
                return parameter.getSpecification().isNotCollection();
            }
        };
    }
    public static class Functions {
        public static final Function<ObjectActionParameter, String> GET_NAME = new Function<ObjectActionParameter, String>() {
            @Override public String apply(final ObjectActionParameter input) {
                return input.getName();
            }
        };
        public static final Function<ObjectActionParameter, Class<?>> GET_TYPE = new Function<ObjectActionParameter, Class<?>>() {
            @Override public Class<?> apply(final ObjectActionParameter input) {
                return input.getSpecification().getCorrespondingClass();
            }
        };

        private Functions(){}

    }

    public static class Predicates {
        private Predicates(){}

        /**
         * A predicate for action parameters that checks that the parameter collection element type
         * is the same as assignable (that is, the same or a supertype of, supporting co-variance) the
         * {@link ObjectSpecification element type} provided in the constructor.
         *
         * <p>
         *     For example, a parented collection of <tt>LeaseTerm</tt>s provides an elementSpec of <tt>LeaseTerm</tt>
         *     into the constructor.  An action with signature <tt>removeTerms(List&lt;LeaseTerm>)</tt> would match on
         *     its (first) parameter.
         * </p>
         *
         * <p>
         *     For example, a parented collection of <tt>LeaseTermForServiceCharge</tt>s provides an elementSpec
         *     of <tt>LeaseTermForServiceCharge</tt> into the constructor.  An action with signature
         *     <tt>removeTerms(List&lt;LeaseTerm>)</tt> would match on its (first) parameter.
         * </p>
         */
        public static class CollectionParameter implements Predicate<ObjectActionParameter> {

            private final ObjectSpecification elementSpecification;

            public CollectionParameter(final ObjectSpecification elementSpecification) {
                this.elementSpecification = elementSpecification;
            }

            @Override
            public boolean apply(@Nullable final ObjectActionParameter objectActionParameter) {
                if (!(objectActionParameter instanceof OneToManyActionParameter)) {
                    return false;
                }

                final OneToManyActionParameter otmap =
                        (OneToManyActionParameter) objectActionParameter;
                final ObjectSpecification paramElementSpecification = otmap.getSpecification();
                return this.elementSpecification.isOfType(paramElementSpecification);
            }
        }

        /**
         * A predicate for action parameters that checks that the parameter collection element type
         * is exactly the same as the {@link ObjectSpecification element type} (no co/contra-variance) provided in the constructor.
         *
         * <p>
         *     For example, a parented collection of <tt>LeaseTerm</tt>s provides an elementSpec of <tt>LeaseTerm</tt>
         *     into the constructor.  An action with signature <tt>addTerm(LeaseTerm)</tt> would match on
         *     its (first) parameter.
         * </p>
         */
        public static class ScalarParameter implements Predicate<ObjectActionParameter> {

            private final ObjectSpecification specification;

            public ScalarParameter(final ObjectSpecification specification) {
                this.specification = specification;
            }

            @Override
            public boolean apply(@Nullable final ObjectActionParameter objectActionParameter) {
                if (!(objectActionParameter instanceof OneToOneActionParameter)) {
                    return false;
                }

                final OneToOneActionParameter otoap =
                        (OneToOneActionParameter) objectActionParameter;
                final ObjectSpecification paramSecification = otoap.getSpecification();
                return paramSecification == this.specification;
            }
        }
    }
}
