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

package org.apache.isis.core.metamodel.facets.all.deficiencies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.isis.applib.id.FeatureIdentifier;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import static org.apache.isis.commons.internal.base._With.computeIfAbsent;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;

/**
 * Collects meta-model validation failures (deficiencies) directly on the holder that is involved.
 * @since 2.0
 */
@RequiredArgsConstructor(staticName = "of")
public final class DeficiencyFacet implements Facet {

    @NonNull @Getter private final FacetHolder facetHolder;
    @NonNull @Getter private final List<Deficiency> deficiencies;
    
    @Value(staticConstructor = "of")
    public static final class Deficiency {
        @NonNull @Getter private FeatureIdentifier deficiencyOrigin;
        @NonNull @Getter private String deficiencyMessage;
    }
    
    /**
     * Create a new DeficiencyFacet for the facetHolder (if it not already has one), 
     * then adds given deficiency information to the facet. 
     * @param facetHolder
     * @param deficiencyOrigin
     * @param deficiencyMessage
     */
    public static DeficiencyFacet appendTo(
            @NonNull FacetHolder facetHolder, 
            @NonNull FeatureIdentifier deficiencyOrigin, 
            @NonNull String deficiencyMessage) {
        
        val deficiencyFacet = computeIfAbsent(facetHolder.getFacet(DeficiencyFacet.class), 
                ()->DeficiencyFacet.of(facetHolder, new ArrayList<>()));
        
        deficiencyFacet.getDeficiencies().add(Deficiency.of(deficiencyOrigin, deficiencyMessage));
        return deficiencyFacet;
    }
    
    // -- FACET IMPLEMENTATION
    
    @Override
    public void setFacetHolder(FacetHolder facetHolder) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Facet getUnderlyingFacet() {
        return null;
    }

    @Override
    public void setUnderlyingFacet(Facet underlyingFacet) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Class<? extends Facet> facetType() {
        return DeficiencyFacet.class;
    }

    @Override
    public boolean isDerived() {
        return false;
    }

    @Override
    public boolean isFallback() {
        return true;
    }

    @Override
    public boolean alwaysReplace() {
        return false;
    }

    @Override
    public void appendAttributesTo(Map<String, Object> attributeMap) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void addContributedFacet(Facet contributedFacet) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void forEachContributedFacet(Consumer<Facet> onContributedFacet) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Class<? extends Facet> facetAliasType() {
        return null;
    }


}
