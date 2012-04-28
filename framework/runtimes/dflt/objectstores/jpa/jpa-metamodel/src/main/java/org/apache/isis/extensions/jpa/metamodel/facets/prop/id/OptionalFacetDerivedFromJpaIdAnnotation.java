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
package org.apache.isis.extensions.jpa.metamodel.facets.prop.id;

import javax.persistence.Id;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.mandatory.MandatoryFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.extensions.jpa.metamodel.facets.prop.version.OptionalFacetDerivedFromJpaVersionAnnotation;


/**
 * Derived by presence of {@link Id}; optional to allow Hibernate to detect
 * transient objects.
 * <p>
 * By default mandatory properties are initialized using the
 * {@link PropertyDefaultFacet} facet. We don't want this, so this facet marks
 * the property as optional, meaning that the {@link Id} property is left
 * untouched by Naked Objects.
 * 
 * @see OptionalFacetDerivedFromJpaVersionAnnotation
 */
public class OptionalFacetDerivedFromJpaIdAnnotation extends
        MandatoryFacetDefault {

    public OptionalFacetDerivedFromJpaIdAnnotation(final FacetHolder holder) {
        super(holder);
    }

    @Override
    public boolean isInvertedSemantics() {
        return true;
    }


}
