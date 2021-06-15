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
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;

public class FacetedMethodParameter
extends TypedHolderAbstract {

    public FacetedMethodParameter(
            final MetaModelContext mmc,
            final FeatureType featureType,
            final Class<?> declaringType,
            final Method method,
            final Class<?> type) {

        super(mmc,
                featureType,
                type,
                FeatureType.ACTION.identifierFor(
                        LogicalType.lazy(
                                declaringType,
                                ()->mmc.getSpecificationLoader().loadSpecification(declaringType).getLogicalTypeName()),
                        method));
    }

    public FacetedMethodParameter(
            final MetaModelContext mmc,
            final FeatureType featureType,
            final Class<?> type,
            final Identifier identifier) {

        super(mmc, featureType, type, identifier);
    }


    /**
     * Returns an instance with {@code type} replaced by given {@code elementType}.
     * @param elementType
     */
    public FacetedMethodParameter withType(final Class<?> elementType) {
        //XXX maybe future refactoring can make the type immutable, so we can remove this method
        this.type = elementType;
        return this;
    }

}
