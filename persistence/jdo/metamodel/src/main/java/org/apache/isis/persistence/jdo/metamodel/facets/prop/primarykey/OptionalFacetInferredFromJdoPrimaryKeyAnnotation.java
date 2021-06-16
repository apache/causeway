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
package org.apache.isis.persistence.jdo.metamodel.facets.prop.primarykey;

import javax.jdo.annotations.PrimaryKey;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.objectvalue.mandatory.MandatoryFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;

/**
 * Inferred from presence of {@link PrimaryKey}.
 *
 * <p>
 * By default mandatory properties are initialized using the
 * {@link PropertyDefaultFacet} facet. We don't want this, so this facet marks
 * the property as optional, meaning that the {@link PrimaryKey} property is left
 * untouched by Isis.
 */
public class OptionalFacetInferredFromJdoPrimaryKeyAnnotation
extends MandatoryFacetAbstract {

    public OptionalFacetInferredFromJdoPrimaryKeyAnnotation(final FacetHolder holder) {
        super(holder, Semantics.OPTIONAL, Precedence.INFERRED);
    }

}
