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
package org.apache.causeway.core.metamodel.facets.object.navchild;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel._testing.MetaModelContext_forTesting;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.FacetFactoryTestAbstract;
import org.apache.causeway.core.metamodel.facets.collections.layout.CollectionLayoutFacetFactory;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

class NavigableSubtreeFacetFactoryTest extends FacetFactoryTestAbstract {

    CollectionLayoutFacetFactory facetFactory;
    NavigableSubtreeFacetPostProcessor postProcessor;
    SpecificationLoader specLoader;

    @BeforeEach
    void setUp() {
        var mmc = MetaModelContext_forTesting.buildDefault();
        assertNotNull(MetaModelContext.instanceNullable());
        facetFactory = new CollectionLayoutFacetFactory(mmc);
        postProcessor = new NavigableSubtreeFacetPostProcessor(mmc);
        specLoader = mmc.getSpecificationLoader();
    }

    @AfterEach
    protected void tearDown() {
        facetFactory = null;
    }

    @Test
    void treeNodeFacetShouldBeInstalledWhenNodeHasAnnotations() {
        
        collectionScenario(_TreeSample.A.class, "childrenB", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);
            assertNotNull(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
            // copy over facets to spec for testing later
            var spec = specLoader.specForType(_TreeSample.A.class).orElseThrow();
            spec.getAssociationElseFail("childrenB")
                .addFacet(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
        });
        
        collectionScenario(_TreeSample.A.class, "childrenC", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);
            assertNotNull(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
            // copy over facets to spec for testing later
            var spec = specLoader.specForType(_TreeSample.A.class).orElseThrow();
            spec.getAssociationElseFail("childrenC")
                .addFacet(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
        });
        
        collectionScenario(_TreeSample.B.class, "childrenD", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);
            assertNotNull(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
            // copy over facets to spec for testing later
            var spec = specLoader.specForType(_TreeSample.B.class).orElseThrow();
            spec.getAssociationElseFail("childrenD")
                .addFacet(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
        });
        
        collectionScenario(_TreeSample.C.class, "childrenD", (processMethodContext, facetHolder, facetedMethod)->{
            facetFactory.process(processMethodContext);
            assertNotNull(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
            // copy over facets to spec for testing later
            var spec = specLoader.specForType(_TreeSample.C.class).orElseThrow();
            spec.getAssociationElseFail("childrenD")
                .addFacet(facetedMethod.getFacet(NavigableSubtreeSequenceFacet.class));
        });
        
        var specs = Can.of(_TreeSample.A.class, _TreeSample.B.class, _TreeSample.C.class, _TreeSample.D.class)
            .map(specLoader::specForType)
            .map(opt->opt.orElse(null));
        // now run the post-processor
        specs.forEach(postProcessor::postProcessObject);
        
        specs.forEach(spec->{
            switch(spec.getCorrespondingClass().getSimpleName()) {
                case "A" -> assertNotNull(spec.getFacet(NavigableSubtreeFacet.class));
                case "B" -> assertNotNull(spec.getFacet(NavigableSubtreeFacet.class));
                case "C" -> assertNotNull(spec.getFacet(NavigableSubtreeFacet.class));
                case "D" -> assertNull(spec.getFacet(NavigableSubtreeFacet.class));
                default -> fail("unexpected case");
            }
        });

    }
}
