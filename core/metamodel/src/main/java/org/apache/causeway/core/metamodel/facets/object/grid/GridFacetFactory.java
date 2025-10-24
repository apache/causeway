/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.causeway.core.metamodel.facets.object.grid;

import jakarta.inject.Inject;

import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;

public class GridFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public GridFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        if(gridService.get()==null) return;

        var facetHolder = processClassContext.getFacetHolder();
        addFacet(BSGridFacet
            .create(facetHolder, gridService.get()));
    }

    private final _Lazy<GridService> gridService = _Lazy.threadSafe(()->
        getServiceRegistry().lookupService(GridService.class).orElse(null));

}
