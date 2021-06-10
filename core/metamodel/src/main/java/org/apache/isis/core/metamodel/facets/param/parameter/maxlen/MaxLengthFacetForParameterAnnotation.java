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

package org.apache.isis.core.metamodel.facets.param.parameter.maxlen;

import java.util.Optional;

import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacetAbstract;

public class MaxLengthFacetForParameterAnnotation
extends MaxLengthFacetAbstract {

    public static MaxLengthFacet create(
            final Optional<Parameter> parameterIfAny,
            final FacetHolder holder) {

        return parameterIfAny
                .map(Parameter::maxLength)
                .filter(maxLength -> maxLength != -1)
                .map(maxLength -> new MaxLengthFacetForParameterAnnotation(maxLength, holder))
                .orElse(null);
    }

    private MaxLengthFacetForParameterAnnotation(final int value, final FacetHolder holder) {
        super(value, holder);
    }

}
