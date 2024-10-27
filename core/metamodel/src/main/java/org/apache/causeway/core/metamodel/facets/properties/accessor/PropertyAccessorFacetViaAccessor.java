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
package org.apache.causeway.core.metamodel.facets.properties.accessor;

import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.NonNull;

public class PropertyAccessorFacetViaAccessor
extends PropertyOrCollectionAccessorFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;

    public PropertyAccessorFacetViaAccessor(
            final ObjectSpecification declaringType,
            final ResolvedMethod method,
            final FacetHolder holder) {

        super(declaringType, holder);
        this.methods = ImperativeFacet.singleRegularMethod(method);
    }

    @Override
    public Intent getIntent() {
        return Intent.ACCESSOR;
    }

    @Override
    public Object getProperty(
            final ManagedObject owningAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular
        final Object referencedObject = MmInvokeUtils.invokeNoArg(method.method(), owningAdapter);

        if(referencedObject == null) {
            return null;
        }

        boolean filterForVisibility = getConfiguration().getCore().getMetaModel().isFilterVisibility();
        if(filterForVisibility) {
            final ManagedObject referencedAdapter = getObjectManager().adapt(referencedObject);
            final boolean visible = MmVisibilityUtils
                    .isVisible(referencedAdapter, interactionInitiatedBy);
            if (!visible) {
                return null;
            }
        }
        return referencedObject;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }

}
