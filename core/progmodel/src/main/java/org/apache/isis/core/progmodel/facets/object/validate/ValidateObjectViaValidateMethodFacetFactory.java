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


package org.apache.isis.core.progmodel.facets.object.validate;

import java.lang.reflect.Method;

import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.feature.FeatureType;
import org.apache.isis.core.progmodel.facets.MethodPrefixBasedFacetFactoryAbstract;


public class ValidateObjectViaValidateMethodFacetFactory extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String VALIDATE_PREFIX = "validate";

    private static final String[] PREFIXES = { VALIDATE_PREFIX, };

    public ValidateObjectViaValidateMethodFacetFactory() {
        super(PREFIXES, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(
    		final Class<?> type, 
    		final MethodRemover methodRemover, 
    		final FacetHolder facetHolder) {
        final Method method = findMethod(type, OBJECT, VALIDATE_PREFIX, String.class, NO_PARAMETERS_TYPES);
        if (method != null) {
            methodRemover.removeMethod(method);
            return FacetUtil.addFacet(new ValidateObjectFacetViaValidateMethod(method, facetHolder));
        } else {
            return false;
        }
    }
}
