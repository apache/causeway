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

/**
 * Provides reflective access to a field on a domain object that is used to
 * reference another domain object.
 */
public interface OneToOneAssociation extends ObjectAssociation, OneToOneFeature, MutableCurrentHolder {

    /**
     * Initialise this field in the specified object with the specified
     * reference - this call should only affect the specified object, and not
     * any related objects. It should also not be distributed. This is strictly
     * for re-initialising the object and not specifying an association, which
     * is only done once.
     */
    void initAssociation(ManagedObject inObject, ManagedObject associate);



    /**
     * Determines if the specified reference is valid for setting this field in
     * the specified object, represented as a {@link Consent}.
     */
    Consent isAssociationValid(
            final ManagedObject targetAdapter,
            final ManagedObject proposedAdapter,
            final InteractionInitiatedBy interactionInitiatedBy);


}
