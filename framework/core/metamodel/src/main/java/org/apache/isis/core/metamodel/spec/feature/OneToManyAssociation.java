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

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.interactions.AccessContext;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;

public interface OneToManyAssociation extends ObjectAssociation, OneToManyFeature {

    // /////////////////////////////////////////////////////////////
    // add
    // /////////////////////////////////////////////////////////////

    /**
     * Creates an {@link InteractionContext} that represents validation of a
     * candidate object to be added to the collection.
     * 
     * <p>
     * Typically it is easier to just call
     * {@link #isValidToAdd(ObjectAdapter, ObjectAdapter)} or
     * {@link #isValidToAddResult(ObjectAdapter, ObjectAdapter)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    public ValidityContext<?> createValidateAddInteractionContext(AuthenticationSession session, InteractionInvocationMethod invocationMethod, ObjectAdapter owningObjectAdapter, ObjectAdapter proposedObjectToAdd);

    /**
     * Determines if the specified element can be added to the collection field,
     * represented as a {@link Consent}.
     * 
     * <p>
     * If allowed the {@link #addElement(ObjectAdapter, ObjectAdapter) add}
     * method can be called with the same parameters, .
     * @see #isValidToAddResult(ObjectAdapter, ObjectAdapter)
     */
    Consent isValidToAdd(ObjectAdapter owningObjectAdapter, ObjectAdapter proposedObjectToAdd);

    /**
     * Add the specified element to this collection field in the specified
     * object.
     */
    void addElement(ObjectAdapter owningObjectAdapter, ObjectAdapter objectToAdd);

    // /////////////////////////////////////////////////////////////
    // remove
    // /////////////////////////////////////////////////////////////

    /**
     * Creates an {@link InteractionContext} that represents validation of a
     * candidate object to be removed from the collection.
     * 
     * <p>
     * Typically it is easier to just call
     * {@link #isValidToAdd(ObjectAdapter, ObjectAdapter)} or
     * {@link #isValidToAddResult(ObjectAdapter, ObjectAdapter)}; this is
     * provided as API for symmetry with interactions (such as
     * {@link AccessContext} accesses) have no corresponding vetoing methods.
     */
    ValidityContext<?> createValidateRemoveInteractionContext(AuthenticationSession session, InteractionInvocationMethod invocationMethod, ObjectAdapter owningObjectAdapter, ObjectAdapter proposedObjectToRemove);

    /**
     * Determines if the specified element can be removed from the collection
     * field, represented as a {@link Consent}.
     * 
     * <p>
     * If allowed the {@link #removeElement(ObjectAdapter, ObjectAdapter)
     * remove} method can be called with the same parameters, .
     * 
     * @see #removeElement(ObjectAdapter, ObjectAdapter)
     * @see #isValidToAddResult(ObjectAdapter, ObjectAdapter)
     */
    Consent isValidToRemove(ObjectAdapter owningObjectAdapter, ObjectAdapter proposedObjectToRemove);

    /**
     * Remove the specified element from this collection field in the specified
     * object.
     */
    void removeElement(ObjectAdapter owningObjectAdapter, ObjectAdapter oObjectToRemove);

    // /////////////////////////////////////////////////////////////
    // clear
    // /////////////////////////////////////////////////////////////

    /**
     * Remove all elements from this collection field in the specified object.
     */
    void clearCollection(ObjectAdapter inObject);

}
