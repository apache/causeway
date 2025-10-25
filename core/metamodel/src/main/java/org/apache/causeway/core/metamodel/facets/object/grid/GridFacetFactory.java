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
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

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

        if(facetHolder instanceof ObjectSpecification objSpec) {
            if(objSpec.isEntityOrViewModelOrAbstract()) {
                addFacet(BSGridFacet
                        .create(facetHolder, gridService.get()));
            } else {
                /* as of time of writing, when not installing a facet here, we end up in recursive update cycle later (test code)
                 *
                java.lang.IllegalStateException: Recursive update
                at java.base/java.util.concurrent.ConcurrentHashMap.compute(ConcurrentHashMap.java:1996)
                at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.normalized(BSGridFacet.java:90)
                at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.getGrid(BSGridFacet.java:74)
                at org.apache.causeway.core.metamodel.facets.object.grid.GridFacet.getGrid(GridFacet.java:52)
                ..
                at org.apache.causeway.core.metamodel.util.Facets.gridPreload(Facets.java:203)
                at org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationDefault.introspectFully(ObjectSpecificationDefault.java:640)
                ..
                at org.apache.causeway.core.metamodel.spec.impl.SpecificationLoaderInternal.specForTypeElseFail(SpecificationLoaderInternal.java:128)
                at org.apache.causeway.core.metamodel.services.grid.bootstrap.GridSystemServiceBootstrap.validateAndNormalize(GridSystemServiceBootstrap.java:251)
                at org.apache.causeway.core.metamodel.services.grid.GridSystemServiceAbstract.normalize(GridSystemServiceAbstract.java:102)
                at org.apache.causeway.core.metamodel.services.grid.GridServiceDefault.normalize(GridServiceDefault.java:95)
                at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.load(BSGridFacet.java:150)
                ..
                at java.base/java.util.concurrent.ConcurrentHashMap.compute(ConcurrentHashMap.java:1921)
                at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.normalized(BSGridFacet.java:90)
                at org.apache.causeway.core.metamodel.facets.object.grid.BSGridFacet.getGrid(BSGridFacet.java:74)
                at org.apache.causeway.core.metamodel.facets.object.grid.GridFacet.getGrid(GridFacet.java:52)
                ..
                at org.apache.causeway.core.metamodel.util.Facets.gridPreload(Facets.java:203)
                at org.apache.causeway.core.metamodel.spec.impl.ObjectSpecificationDefault.introspectFully(ObjectSpecificationDefault.java:640)
                ..
                at org.apache.causeway.core.metamodel.spec.impl.SpecificationLoaderInternal.specForType(SpecificationLoaderInternal.java:94)
                at org.apache.causeway.core.metamodel.facets.object.navchild.NavigableSubtreeFacetFactoryTest.lambda$0(NavigableSubtreeFacetFactoryTest.java:63)
                at org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract.collectionScenario(FacetFactoryTestAbstract.java:384)
                at org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract.collectionScenario(FacetFactoryTestAbstract.java:367)
                at org.apache.causeway.core.metamodel.facets.object.navchild.NavigableSubtreeFacetFactoryTest.treeNodeFacetShouldBeInstalledWhenNodeHasAnnotations(NavigableSubtreeFacetFactoryTest.java:59)
                 */
                addFacet(new BSGridFacet.NoLayout(facetHolder, Precedence.LOW));
            }
        }

    }

    private final _Lazy<GridService> gridService = _Lazy.threadSafe(()->
        getServiceRegistry().lookupService(GridService.class).orElse(null));

}
