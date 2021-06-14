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

import org.apache.isis.core.metamodel.facetapi.FacetHolderAbstract;
import org.apache.isis.core.metamodel.facetapi.FeatureType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class TypedHolderAbstract
extends FacetHolderAbstract
implements TypedHolder {

    @Getter(onMethod_ = {@Override}) private final FeatureType featureType;

    /**
     * For {@link FeatureType#COLLECTION collection}s and for
     * {@link FeatureType#ACTION_PARAMETER_COLLECTION}s, represents the element type.
     * <p>
     * For example, the accessor might return a raw type such as
     * <tt>java.util.List</tt>, rather than a generic one such as
     * <tt>java.util.List&lt;Customer&gt;</tt>.
     */
    @Getter(onMethod_ = {@Override}) private final Class<?> type;

    @Override // as used for logging, not strictly required
    public String toString() {
        return type.getSimpleName();
    }

}
