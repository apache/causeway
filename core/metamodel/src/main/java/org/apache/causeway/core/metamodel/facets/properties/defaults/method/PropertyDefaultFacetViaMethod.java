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
package org.apache.causeway.core.metamodel.facets.properties.defaults.method;

import java.util.function.BiConsumer;

import org.apache.causeway.applib.exceptions.unrecoverable.UnknownTypeException;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.properties.defaults.PropertyDefaultFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmInvokeUtils;

import lombok.Getter;
import lombok.NonNull;

public class PropertyDefaultFacetViaMethod
extends PropertyDefaultFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<MethodFacade> methods;

    public PropertyDefaultFacetViaMethod(
            final ResolvedMethod method,
            final FacetHolder holder) {
        super(holder);
        this.methods = ImperativeFacet.singleRegularMethod(method);
    }

    @Override
    public Intent getIntent() {
        return Intent.DEFAULTS;
    }

    @Override
    public ManagedObject getDefault(final ManagedObject owningAdapter) {
        var method = methods.getFirstElseFail().asMethodElseFail(); // expected regular
        final Object result = MmInvokeUtils.invokeNoArg(method.method(), owningAdapter);
        if (result == null) {
            return null;
        }
        return createAdapter(method.returnType(), result);
    }

    private ManagedObject createAdapter(final Class<?> type, final Object object) {
        var specification = getSpecificationLoader().loadSpecification(type);
        if (specification.isSingular()) {
            return getObjectManager().adapt(object);
        } else {
            throw new UnknownTypeException("not an object, is this a collection?");
        }
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        ImperativeFacet.visitAttributes(this, visitor);
    }

}
