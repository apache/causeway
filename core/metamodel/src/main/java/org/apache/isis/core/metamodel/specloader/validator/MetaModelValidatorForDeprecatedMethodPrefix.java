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
package org.apache.isis.core.metamodel.specloader.validator;

import org.apache.isis.applib.id.FeatureIdentifier;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.FacetFactory;

@Deprecated //not used any more?
class MetaModelValidatorForDeprecatedMethodPrefix extends MetaModelValidatorForDeprecatedAbstract {

    private final String methodPrefix;

    public MetaModelValidatorForDeprecatedMethodPrefix(final String methodPrefix) {
        this.methodPrefix = methodPrefix;
    }

    @Override
    protected String failureMessageFor(
            final Facet facet, 
            final FacetFactory.AbstractProcessWithMethodContext<?> processMethodContext) {

        final boolean inherited = isInherited(processMethodContext);

        final IdentifiedHolder identifiedHolder = (IdentifiedHolder) facet.getFacetHolder();
        final FeatureIdentifier identifier = identifiedHolder.getIdentifier();
        final String id = identifier.getFullIdentityString();
        return String.format(
                "%s%s: method prefix '%s' is deprecated",
                id,
                (inherited?" (inherited)":""),
                methodPrefix);
    }

}
