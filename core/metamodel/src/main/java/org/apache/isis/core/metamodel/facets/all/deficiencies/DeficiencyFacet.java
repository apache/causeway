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

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailure;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.val;

/**
 * Collects meta-model validation failures (deficiencies) directly with the holder that is involved.
 * @since 2.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeficiencyFacet {

    /**
     * Create a new DeficiencyFacet for the facetHolder (if it not already has one), 
     * then adds given deficiency information to the facet. 
     */
    public static void appendTo(
            @NonNull FacetHolder facetHolder, 
            @NonNull Identifier deficiencyOrigin, 
            @NonNull String deficiencyMessage) {
        
        val validationFailure = ValidationFailure.of(deficiencyOrigin, deficiencyMessage);
        facetHolder.getSpecificationLoader().addValidationFailure(validationFailure);
    }
    
    /**
     * Create a new DeficiencyFacet for the facetHolder (if it not already has one), 
     * then adds given deficiency information to the facet. 
     */
    public static void appendTo(
            @NonNull IdentifiedHolder facetHolder, 
            @NonNull String deficiencyMessage) {
        appendTo(facetHolder, facetHolder.getIdentifier(), deficiencyMessage);
    }
    
    /**
     * Create a new DeficiencyFacet for the facetHolder (if it not already has one), 
     * then adds given deficiency information to the facet. 
     */
    public static void appendToWithFormat(
            @NonNull IdentifiedHolder facetHolder, 
            @NonNull String messageFormat, 
            final Object ...args) {
        appendTo(facetHolder, String.format(messageFormat, args));
    }

}
