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

package org.apache.isis.core.metamodel.facets.object.callbacks;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

/**
 * @deprecated - cannot touch a pojo once deleted
 */
@Deprecated
public class RemovedCallbackFacetViaMethod extends RemovedCallbackFacetAbstract implements ImperativeFacet {

    private final List<Method> methods = new ArrayList<Method>();

    public RemovedCallbackFacetViaMethod(final Method method, final FacetHolder holder) {
        super(holder);
        addMethod(method);
    }

    @Override
    public void addMethod(final Method method) {
        methods.add(method);
    }

    @Override
    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.LIFECYCLE;
    }

    @Override
    public void invoke(final ManagedObject adapter) {
        ManagedObject.InvokeUtil.invokeAll(methods, adapter);
    }

    @Override
    protected String toStringValues() {
        return "methods=" + methods;
    }

    @Override public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        ImperativeFacet.Util.appendAttributesTo(this, attributeMap);
    }

}
