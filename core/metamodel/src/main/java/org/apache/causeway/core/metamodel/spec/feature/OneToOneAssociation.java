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
package org.apache.causeway.core.metamodel.spec.feature;

import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.propcoll.memserexcl.SnapshotExcludeFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.memento.PropertyMemento;

/**
 * Provides reflective access to a field on a domain object that is used to
 * reference another domain object.
 */
public interface OneToOneAssociation
extends
    ObjectAssociation,
    OneToOneFeature,
    MutableCurrentHolder {

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

    /**
     * Returns true if calculated from other data in the object, that is, should
     * not be persisted.
     * Corresponds to {@code @Property(snapshot = Snapshot.EXCLUDED)}
     */
    default boolean isExcludedFromSnapshots() {
        return containsFacet(SnapshotExcludeFacet.class);
    }

    /**
     * Counterpart to {@link #isExcludedFromSnapshots()}.
     */
    default boolean isIncludedWithSnapshots() {
        return !isExcludedFromSnapshots();
    }

    default String getCssClass(final String prefix) {
        final String ownerObjectType = getDeclaringType().getLogicalTypeName().replace(".", "-");
        final String memberId = getFeatureIdentifier().getMemberLogicalName();
        return prefix + ownerObjectType + "-" + memberId;
    }

    /**
     * Returns a serializable representation of this property.
     */
    default PropertyMemento getMemento() {
        return PropertyMemento.forProperty(this);
    }

}
