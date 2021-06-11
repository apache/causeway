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

package org.apache.isis.core.metamodel.facets.properties.update.init;

import java.lang.reflect.Method;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class PropertyInitializationFacetViaSetterMethod
extends PropertyInitializationFacetAbstract
implements ImperativeFacet {

    @Getter(onMethod_ = {@Override}) private final @NonNull Can<Method> methods;

    public PropertyInitializationFacetViaSetterMethod(final Method method, final FacetHolder holder) {
        super(holder);
        this.methods = Can.ofSingleton(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        // LIMITATION: we cannot distinguish between setXxx being called for a modify or for an initialization
        // so we just assume its a setter.
        return Intent.MODIFY_PROPERTY;
    }

    @Override
    public void initProperty(final ManagedObject owningAdapter, final ManagedObject initialAdapter) {
        val method = methods.getFirstOrFail();
        ManagedObjects.InvokeUtil.invoke(method, owningAdapter, initialAdapter);
    }

    @Override
    protected String toStringValues() {
        val method = methods.getFirstOrFail();
        return "method=" + method;
    }

}
