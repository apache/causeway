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
package org.apache.causeway.persistence.jdbc.metamodel;

import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;

import org.springframework.data.annotation.Persistent;

class JdbcEntityFacetFactory extends FacetFactoryAbstract {

    JdbcEntityFacetFactory(final MetaModelContext mmc) {
        super(mmc, ImmutableEnumSet.of(FeatureType.OBJECT));
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        var cls = processClassContext.getCls();

        var facetHolder = processClassContext.getFacetHolder();

        var entityAnnotation = processClassContext.synthesizeOnType(Persistent.class);
        if(!entityAnnotation.isPresent()) return;

        addFacet(
                new JdbcEntityFacet(facetHolder, cls));
    }
}
