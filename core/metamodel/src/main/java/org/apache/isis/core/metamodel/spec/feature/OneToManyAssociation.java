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

import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public interface OneToManyAssociation extends ObjectAssociation, OneToManyFeature {

    // /////////////////////////////////////////////////////////////
    // add
    // /////////////////////////////////////////////////////////////


    /**
     * Determines if the specified element can be added to the collection field,
     * represented as a {@link Consent}.
     *
     * <p>
     * If allowed the {@link #addElement(ManagedObject, ManagedObject, InteractionInitiatedBy) add}
     * method can be called with the same parameters.
     *
     * @see #addElement(ManagedObject, ManagedObject, InteractionInitiatedBy)
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
     *     Should be preceded by call to {@link #isValidToAdd(ManagedObject, ManagedObject)}.
     * </p>
     *
     * @see #isValidToAdd(ManagedObject, ManagedObject)
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
     * If allowed the {@link #removeElement(ManagedObject, ManagedObject, InteractionInitiatedBy)
     * remove} method can be called with the same parameters.
     *
     * @see #removeElement(ManagedObject, ManagedObject, InteractionInitiatedBy)
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
     *     Should be preceded by call to {@link #isValidToRemove(ManagedObject, ManagedObject)}.
     * </p>
     *
     * @see #isValidToRemove(ManagedObject, ManagedObject)
     */
    void removeElement(
            ManagedObject owningObjectAdapter,
            ManagedObject oObjectToRemove,
            InteractionInitiatedBy interactionInitiatedBy);



}
