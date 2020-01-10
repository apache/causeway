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

package org.apache.isis.core.metamodel.facets.object.navparent.method;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Map;

import org.apache.isis.core.commons.internal.reflection._Reflect;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.navparent.NavigableParentFacetAbstract;

/**
 *
 * @since 2.0
 *
 */
public class NavigableParentFacetMethod extends NavigableParentFacetAbstract {

    private final MethodHandle methodHandle;

    public NavigableParentFacetMethod(final Method method, final FacetHolder holder) throws IllegalAccessException {
        super(holder);
        this.methodHandle = _Reflect.handleOf(method);
    }

    @Override
    public Object navigableParent(Object object) {
        try {
            return methodHandle.invoke(object);
        } catch (final Throwable ex) {
            return null;
        }
    }


    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("methodHandle", methodHandle);
    }

}
