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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;

/**
 * Mix-in interface for {@link ObjectFeature}s that reference or otherwise
 * contain a 'current' value.
 *
 * <p>
 * Examples include {@link OneToOneAssociation properties},
 * {@link OneToManyAssociation collection}s and {@link OneToOneActionParameter
 * action parameter}s (but not {@link ObjectAction action}s themselves).
 */
public interface CurrentHolder {

    /**
     * Returns the referenced {@link ObjectAdapter} for the owning
     * {@link ObjectAdapter}.
     *
     * <p>
     * For example, if this is an {@link OneToOneAssociation}, then returns the
     * referenced object.
     */
    ObjectAdapter get(final ObjectAdapter owner, final InteractionInitiatedBy interactionInitiatedBy);

}
