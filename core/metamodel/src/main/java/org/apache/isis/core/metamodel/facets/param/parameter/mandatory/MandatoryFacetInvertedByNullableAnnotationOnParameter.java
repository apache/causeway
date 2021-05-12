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

package org.apache.isis.core.metamodel.facets.param.parameter.mandatory;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;

/**
 * Derived by presence of an {@link Nullable} annotation on parameter.
 *
 * <p>
 * This implementation indicates that the {@link FacetHolder} is <i>not</i>
 * mandatory, as per {@link #isInvertedSemantics()}.
 */
public class MandatoryFacetInvertedByNullableAnnotationOnParameter extends MandatoryFacetAbstract {

    public static MandatoryFacet create(
            final Optional<Nullable> nullableIfAny,
            final Class<?> parameterType,
            final FacetedMethodParameter holder) {

        if(!nullableIfAny.isPresent()) {
            return null;
        }
        if(parameterType.isPrimitive()) {
            return null;
        }
        return new MandatoryFacetInvertedByNullableAnnotationOnParameter(holder);
    }

    private MandatoryFacetInvertedByNullableAnnotationOnParameter(final FacetHolder holder) {
        super(holder, Semantics.OPTIONAL);
    }


}
