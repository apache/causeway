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

import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;

public interface OneToManyAssociation extends ObjectAssociation, OneToManyFeature {

    // /////////////////////////////////////////////////////////////
    // add
    // /////////////////////////////////////////////////////////////


    /**
     * Determines if the specified element can be added to the collection field,
     * represented as a {@link Consent}.
     *
     * <p>
     * If allowed the {@link #addElement(ObjectAdapter, ObjectAdapter, InteractionInitiatedBy) add}
     * method can be called with the same parameters.
     *
     * @see #addElement(ObjectAdapter, ObjectAdapter, InteractionInitiatedBy)
     */
    Consent isValidToAdd(
            ManagedObject owningObjectAdapter,
            ManagedObject proposedObjectToAdd,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Add the specified element to this collection field in the specified
     * object.
     *
     * <p>
     *     Should be preceded by call to {@link #isValidToAdd(ObjectAdapter, ObjectAdapter)}.
     * </p>
     *
     * @see #isValidToAdd(ObjectAdapter, ObjectAdapter)
     */
    void addElement(
            ManagedObject owningObjectAdapter,
            ManagedObject objectToAdd,
            InteractionInitiatedBy interactionInitiatedBy);

    // /////////////////////////////////////////////////////////////
    // remove
    // /////////////////////////////////////////////////////////////


    /**
     * Determines if the specified element can be removed from the collection
     * field, represented as a {@link Consent}.
     *
     * <p>
     * If allowed the {@link #removeElement(ObjectAdapter, ObjectAdapter, InteractionInitiatedBy)
     * remove} method can be called with the same parameters.
     *
     * @see #removeElement(ObjectAdapter, ObjectAdapter, InteractionInitiatedBy)
     */
    Consent isValidToRemove(
            ManagedObject owningObjectAdapter,
            ManagedObject proposedObjectToRemove,
            InteractionInitiatedBy interactionInitiatedBy);

    /**
     * Remove the specified element from this collection field in the specified
     * object.
     *
     * <p>
     *     Should be preceded by call to {@link #isValidToRemove(ObjectAdapter, ObjectAdapter)}.
     * </p>
     *
     * @see #isValidToRemove(ObjectAdapter, ObjectAdapter)
     */
    void removeElement(
            ManagedObject owningObjectAdapter,
            ManagedObject oObjectToRemove,
            InteractionInitiatedBy interactionInitiatedBy);



}
