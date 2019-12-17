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

package org.apache.isis.metamodel.spec.feature;

import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.enterprise.inject.Vetoed;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

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
            ManagedObject targetObject,
            ManagedObject[] args,
            int position,
            InteractionInitiatedBy interactionInitiatedBy);


    /**
     * Whether there is an autoComplete provided (eg <tt>autoCompleteXxx</tt> supporting
     * method) for the parameter.
     */
    boolean hasAutoComplete();

    /**
     * Returns a list of possible references/values for this parameter, which the
     * user can choose from, based on the input search argument.
     */
    ManagedObject[] getAutoComplete(
            ManagedObject adapter,
            Can<ManagedObject> dependentArgs,
            String searchArg,
            InteractionInitiatedBy interactionInitiatedBy);



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
    ManagedObject[] getChoices(
            ManagedObject adapter,
            Can<ManagedObject> dependentArgs,
            InteractionInitiatedBy interactionInitiatedBy);


    ManagedObject getDefault(
            ManagedObject adapter,
            Can<ManagedObject> dependentArgs,
            Integer paramNumUpdated);


    /**
     * Whether this parameter is visible given the entered previous arguments
     * @param targetAdapter
     * @param dependentArgs
     * @param interactionInitiatedBy
     * @return
     */
    Consent isVisible(
            ManagedObject targetAdapter,
            Can<ManagedObject> dependentArgs,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Whether this parameter is disabled given the entered previous arguments
     * @param targetAdapter
     * @param dependentArgs
     * @param interactionInitiatedBy
     * @return
     */
    Consent isUsable(
            ManagedObject targetAdapter,
            Can<ManagedObject> dependentArgs,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Whether proposed value for this parameter is valid.
     *
     * @param adapter
     * @param proposedValue
     * @param interactionInitiatedBy
     * @return
     */
    String isValid(
            final ManagedObject adapter,
            final Object proposedValue,
            final InteractionInitiatedBy interactionInitiatedBy);

    @Vetoed
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
            public boolean test(@Nullable final ObjectActionParameter objectActionParameter) {
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
            public boolean test(@Nullable final ObjectActionParameter objectActionParameter) {
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
