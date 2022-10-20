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
package org.apache.causeway.core.metamodel.facets;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.TypeOfAnyCardinality;

import lombok.Getter;
import lombok.NonNull;

public abstract class TypedHolderAbstract
implements TypedHolder {

    @Getter(onMethod_ = {@Override}) private FacetHolder facetHolder;
    @Getter(onMethod_ = {@Override}) private final FeatureType featureType;
    @Getter(onMethod_ = {@Override}) protected TypeOfAnyCardinality type;

    protected TypedHolderAbstract(
            final MetaModelContext mmc,
            final FeatureType featureType,
            final @NonNull TypeOfAnyCardinality type,
            final Identifier featureIdentifier) {
        this.facetHolder = FacetHolder.simple(mmc, featureIdentifier);
        this.featureType = featureType;
        this.type = type;
    }

    @Override // as used for logging, not strictly required
    public String toString() {
        return type.toString();
    }

    public ObjectSpecification getElementSpecification() {
        return getSpecificationLoader().specForTypeElseFail(type.getElementType());
    }

}
