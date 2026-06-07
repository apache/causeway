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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;
import org.apache.causeway.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.causeway.core.metamodel.interactions.UsabilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.val;

/**
 * Disables a synthetic navigation action when its associated scalar reference is null.
 */
public class DisabledFacetForNullScalarReferenceNavigation
        extends DisabledFacetAbstract {

    public static final String REASON = "No referenced object to navigate to.";

    private final @NonNull OneToOneAssociation reference;

    public DisabledFacetForNullScalarReferenceNavigation(
            final @NonNull OneToOneAssociation reference,
            final @NonNull org.apache.causeway.core.metamodel.facetapi.FacetHolder holder) {
        super(Where.ANYWHERE, VetoReason.explicit(REASON), holder);
        this.reference = reference;
    }

    @Override
    public Optional<VetoReason> disables(final UsabilityContext ic) {
        val target = ic.getTarget();
        if (ManagedObjects.isNullOrUnspecifiedOrEmpty(target)) {
            return Optional.empty();
        }
        val referencedObject = reference.get(target, ic.getInitiatedBy());
        return isNullReference(referencedObject)
                ? Optional.of(VetoReason.explicit(REASON))
                : Optional.empty();
    }

    static boolean isNullReference(final org.apache.causeway.core.metamodel.object.ManagedObject referencedObject) {
        return referencedObject == null
                || referencedObject == org.apache.causeway.core.metamodel.object.ManagedObject.unspecified()
                || referencedObject.getPojo() == null;
    }

}
