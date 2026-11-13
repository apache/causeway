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

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;

public class ActionInvocationFacetForScalarReferenceNavigation
        extends FacetAbstract
        implements ActionInvocationFacet {

    private final @NonNull ObjectSpecification declaringType;
    private final @NonNull ObjectSpecification returnType;
    private final @NonNull OneToOneAssociation reference;

    public ActionInvocationFacetForScalarReferenceNavigation(
            final @NonNull ObjectSpecification declaringType,
            final @NonNull ObjectSpecification returnType,
            final @NonNull OneToOneAssociation reference,
            final @NonNull FacetHolder holder) {
        super(ActionInvocationFacet.class, holder);
        this.declaringType = declaringType;
        this.returnType = returnType;
        this.reference = reference;
    }

    @Override
    public ObjectSpecification getDeclaringType() {
        return declaringType;
    }

    @Override
    public ObjectSpecification getReturnType() {
        return returnType;
    }

    @Override
    public ManagedObject invoke(
            final ObjectAction owningAction,
            final InteractionHead head,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        var referencedObject = reference.get(head.target(), interactionInitiatedBy);
        if (DisabledFacetForNullScalarReferenceNavigation.isNullReference(referencedObject)) {
            throw new RecoverableException(DisabledFacetForNullScalarReferenceNavigation.REASON);
        }
        return referencedObject;
    }
}
