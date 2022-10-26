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
package org.apache.causeway.core.metamodel.facets.object.value;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember.AuthorizationException;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectActionMixedIn;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class CompositeValueUpdater {

    static interface X {
        Identifier getFeatureIdentifier();
        ObjectSpecification getReturnType();
        ManagedObject executeWithRuleChecking(final InteractionHead head, final Can<ManagedObject> parameters,
                final InteractionInitiatedBy interactionInitiatedBy, final Where where) throws AuthorizationException;
        ManagedObject execute(final InteractionHead head, final Can<ManagedObject> parameters,
                final InteractionInitiatedBy interactionInitiatedBy);
    }


    @Delegate(excludes=X.class)
    private final ObjectActionMixedIn delegate;

    public abstract ObjectSpecification getReturnType();
    protected abstract ManagedObject map(final ManagedObject valueType);

    public Identifier getFeatureIdentifier() {
        val id = delegate.getFeatureIdentifier();
        return Identifier
                .actionIdentifier(
                        id.getLogicalType(),
                        id.getMemberLogicalName(),
                        id.getMemberParameterClassNames());
    }

    public ManagedObject executeWithRuleChecking(
            final InteractionHead head, final Can<ManagedObject> parameters,
            final InteractionInitiatedBy interactionInitiatedBy, final Where where)
                    throws AuthorizationException {
        return map(simpleExecute(head, parameters));
    }

    public ManagedObject execute(
            final InteractionHead head, final Can<ManagedObject> parameters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        return map(simpleExecute(head, parameters));
    }

    private ManagedObject simpleExecute(
            final InteractionHead head, final Can<ManagedObject> parameters) {
        val method = delegate.getFacetedMethod().getMethod();

        final Object[] executionParameters = MmUnwrapUtil.multipleAsArray(parameters);
        final Object targetPojo = MmUnwrapUtil.single(head.getTarget());

        val resultPojo = CanonicalInvoker
                .invoke(method, targetPojo, executionParameters);

        return ManagedObject.value(delegate.getReturnType(), resultPojo);
    }

}
