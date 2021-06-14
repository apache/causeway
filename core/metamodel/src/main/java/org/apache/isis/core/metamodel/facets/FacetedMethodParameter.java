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
package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.facetapi.FeatureType;

import lombok.val;

public class FacetedMethodParameter
extends TypedHolderAbstract {

    public FacetedMethodParameter(
            final FeatureType featureType,
            final Class<?> declaringType,
            final Method method,
            final Class<?> type) {

        super(featureType, type);

        val logicalType = LogicalType.lazy(
                declaringType,
                ()->getSpecificationLoader().loadSpecification(declaringType).getLogicalTypeName());

        // best we can do...
        super.identifier = FeatureType.ACTION.identifierFor(logicalType, method);
    }

    public FacetedMethodParameter(
            final FeatureType featureType,
            final Class<?> type,
            final Identifier identifier) {

        super(featureType, type);
        super.identifier = identifier;
    }


    /**
     * Returns a new instance with {@code type} replaced by given {@code elementType}.
     * @param elementType
     */
    public FacetedMethodParameter withType(Class<?> elementType) {
        return new FacetedMethodParameter(getFeatureType(), elementType, super.identifier);
    }

}
