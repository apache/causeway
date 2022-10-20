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

package org.apache.causeway.core.metamodel.examples.facets.jsr303;

import java.lang.reflect.Method;

import org.apache.causeway.core.metamodel.facets.FacetFactory;
import org.apache.causeway.core.metamodel.facets.FacetHolder;
import org.apache.causeway.core.metamodel.facets.FacetUtil;
import org.apache.causeway.core.metamodel.facets.MethodRemover;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeatureType;


public class Jsr303FacetFactory implements FacetFactory {

    public ObjectFeatureType[] getFeatureTypes() {
        return ObjectFeatureType.OBJECTS_AND_PROPERTIES;
    }

    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        return false;
    }

    /**
     * Simply attaches
     */
    public boolean process(final Method method, final MethodRemover methodRemover, final FacetHolder holder) {
        return FacetUtil.addFacet(new Jsr303PropertyValidationFacet(holder));
    }

    public boolean processParams(final Method method, final int paramNum, final FacetHolder holder) {
        // nothing to do
        return false;
    }

}
