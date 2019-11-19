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

import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;

/**
 * Mix-in interface for {@link ObjectAction}s that reference or otherwise
 * contain a 'current' value that moreover can be changed.
 *
 * <p>
 * Examples include {@link OneToOneAssociation properties} and
 * {@link OneToOneActionParameter action parameter}s (but not
 * {@link ObjectAction action}s themselves) nor {@link OneToManyAssociation
 * collection}s.
 */
public interface MutableCurrentHolder extends CurrentHolder {

    /**
     * Updates the referenced {@link ManagedObject} for the owning
     * {@link ManagedObject} with the new value provided, or clears the
     * reference if null.
     *
     * <p>
     * For example, if this is a {@link OneToOneAssociation}, then updates the
     * object referenced .
     * @param owner
     * @param newValue
     * @param interactionInitiatedBy
     */
    void set(
            final ManagedObject owner,
            final ManagedObject newValue,
            final InteractionInitiatedBy interactionInitiatedBy);

    /**
     * As per {@link #set(ManagedObject, ManagedObject, InteractionInitiatedBy)}, 
     * with {@link InteractionInitiatedBy#USER}.
     * @param owner
     * @param newValue
     */
    void set(
            final ManagedObject owner,
            final ManagedObject newValue);

}
