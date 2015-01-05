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

package org.apache.isis.core.metamodel.facets.object.cssclass.method;

import java.lang.reflect.Method;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacetAbstract;

public class CssClassFacetMethod extends CssClassFacetAbstract {

    private final Method method;

    public CssClassFacetMethod(final Method method, final FacetHolder holder) {
        super(null, holder);
        this.method = method;
    }

    @Override
    public String value() {
        throw new UnsupportedOperationException("This method shouldn't be called. Call #value(ObjectAdapter) instead.");
    }

//    @Override
    public String value(final ObjectAdapter owningAdapter) {
        try {
            return (String) ObjectAdapter.InvokeUtils.invoke(method, owningAdapter);
        } catch (final RuntimeException ex) {
            return null;
        }
    }

}
