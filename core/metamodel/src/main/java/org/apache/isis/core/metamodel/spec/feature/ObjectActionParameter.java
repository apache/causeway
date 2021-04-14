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

import java.util.function.Predicate;

import javax.annotation.Nullable;
import javax.enterprise.inject.Vetoed;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ParameterNegotiationModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

/**
 * Analogous to {@link ObjectAssociation}.
 */
public interface ObjectActionParameter extends ObjectFeature, CurrentHolder {

    /**
     * Owning {@link ObjectAction}.
     */
    ObjectAction getAction();

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
            InteractionHead head,
            Can<ManagedObject> args,
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
    Can<ManagedObject> getAutoComplete(
            ParameterNegotiationModel pendingArgs,
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
    Can<ManagedObject> getChoices(
            ParameterNegotiationModel pendingArgs,
            InteractionInitiatedBy interactionInitiatedBy);

    /** 
     * Needs to account for 2 scenarios,
     * <ul>
     * <li>there is no default providing facet associated with this parameter, 
     * we return the corresponding value from the given {@link ParameterNegotiationModel}</li>
     * <li>there is a default providing facet associated with this parameter, 
     * it may return {@code null}, but in any case the value is wrapped in a 
     * non-null {@link ManagedObject}</li>
     * </ul>
     * @return a {@link ManagedObject}, {@code null} is represented by an empty 
     * but non-null {@link ManagedObject}
     */
    @NonNull ManagedObject getDefault(ParameterNegotiationModel pendingArgs);
    
    @NonNull default ManagedObject getEmpty() {
        return ManagedObject.of(getSpecification(), null);
    }

    /** default value as result of a initial param value fixed point search */
    default ManagedObject getDefault(ManagedObject actionOnwer) {
        return getAction()
                .interactionHead(actionOnwer).defaults()
                .getParamValues()
                .getElseFail(getNumber());
    }
    

    /**
     * Whether this parameter is visible given the entered previous arguments
     * @param head
     * @param pendingArgs
     * @param interactionInitiatedBy
     */
    Consent isVisible(
            InteractionHead head,
            Can<ManagedObject> pendingArgs,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Whether this parameter is disabled given the entered previous arguments
     * @param head
     * @param pendingArgs
     * @param interactionInitiatedBy
     */
    Consent isUsable(
            InteractionHead head,
            Can<ManagedObject> pendingArgs,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Whether proposed value for this parameter is valid.
     *
     * @param head
     * @param pendingArgs
     * @param interactionInitiatedBy
     */
    Consent isValid(
            InteractionHead head,
            Can<ManagedObject> pendingArgs,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Whether proposed value for this parameter is valid.
     *
     * @param adapter
     * @param proposedValue
     * @param interactionInitiatedBy
     * @deprecated use variant with all pendingArgs instead
     */
    @Deprecated
    String isValid(
            InteractionHead head,
            ManagedObject proposedValue,
            InteractionInitiatedBy interactionInitiatedBy);

    
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

    default String getCssClass(String prefix) {
        return getAction().getCssClass(prefix) + "-" + getId();
    }
}
