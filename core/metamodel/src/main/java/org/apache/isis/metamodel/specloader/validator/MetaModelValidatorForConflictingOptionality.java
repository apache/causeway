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
package org.apache.isis.metamodel.specloader.validator;

import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.metamodel.facets.objectvalue.mandatory.MandatoryFacet;
import org.apache.isis.metamodel.facets.objectvalue.mandatory.MandatoryFacetDefault;

import lombok.val;

public class MetaModelValidatorForConflictingOptionality extends MetaModelValidatorAbstract {

    public Facet flagIfConflict(final MandatoryFacet facet, final String message) {
        if(conflictingOptionality(facet)) {
            addFailure(facet, message);
        }
        return facet;
    }

    private Facet addFailure(final Facet facet, final String message) {
        if(facet != null) {
            val holder = (IdentifiedHolder) facet.getFacetHolder();
            val identifier = holder.getIdentifier();
            super.onFailure(holder, identifier, "%s : %s", message, identifier.toFullIdentityString());
        }
        return facet;
    }

    private static boolean conflictingOptionality(final MandatoryFacet facet) {
        if (facet == null) {
            return false;
        }
        final MandatoryFacet underlyingFacet = (MandatoryFacet) facet.getUnderlyingFacet();
        if(underlyingFacet instanceof MandatoryFacetDefault) {
            return false;
        }
        final boolean conflicting =
                underlyingFacet != null && facet.isInvertedSemantics() != underlyingFacet.isInvertedSemantics();
        return conflicting;
    }




}
