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
package org.apache.isis.core.metamodel.facets.properties.update.modify;

import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

/**
 * The mechanism by which the value of the property can be set.
 * @see PropertySetterFacet
 * @since 2.0
 */
public interface PropertySettingAccessor {

    /**
     * Sets the value of this property.
     *
     * <p>
     *     If this is a view model, then the target will be cloned and effectively replaced.
     * </p>
     */
    ManagedObject setProperty(
            final OneToOneAssociation owningAssociation,
            final ManagedObject inObject,
            final ManagedObject value,
            final InteractionInitiatedBy interactionInitiatedBy);
}
