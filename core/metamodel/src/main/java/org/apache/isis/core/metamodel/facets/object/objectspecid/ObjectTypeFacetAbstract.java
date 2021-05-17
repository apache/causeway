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

package org.apache.isis.core.metamodel.facets.object.objectspecid;

import java.util.Map;

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.Getter;
import lombok.NonNull;

public abstract class ObjectTypeFacetAbstract
extends FacetAbstract
implements ObjectTypeFacet {

    public static Class<? extends Facet> type() {
        return ObjectTypeFacet.class;
    }

    @Getter(onMethod_ = {@Override})
    private final @NonNull LogicalType logicalType;

    public ObjectTypeFacetAbstract(
            final LogicalType logicalType,
            final FacetHolder holder) {
        this(logicalType, holder, Derivation.NOT_DERIVED);
    }

    protected ObjectTypeFacetAbstract(
            final LogicalType logicalType,
            final FacetHolder holder,
            final Derivation derivation) {
        super(ObjectTypeFacetAbstract.type(), holder, derivation);
        this.logicalType = logicalType;
    }

    @Override
    public void appendAttributesTo(final Map<String, Object> attributeMap) {
        super.appendAttributesTo(attributeMap);
        attributeMap.put("logicalTypeName", logicalType.getLogicalTypeName());
        attributeMap.put("logicalTypeCorrespondingClass", logicalType.getCorrespondingClass().getName());
    }
}
