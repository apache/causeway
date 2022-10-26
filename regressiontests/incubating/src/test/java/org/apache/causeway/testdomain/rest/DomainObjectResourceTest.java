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
package org.apache.causeway.testdomain.rest;

import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.actnsemantics.BlobDemo;
import org.apache.causeway.testdomain.model.actnsemantics.Configuration_usingActionSemantics;
import org.apache.causeway.testdomain.model.layout.Configuration_usingLayout;
import org.apache.causeway.testdomain.model.layout.LayoutDemo;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.DomainObjectResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.ResourceDescriptor;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.serialization.SerializationStrategy;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingLayout.class,
                Configuration_usingActionSemantics.class,
                DomainObjectResourceTest.TestSetup.class
        },
        properties = {
                "causeway.core.meta-model.introspector.mode=FULL",
                "causeway.applib.annotation.domain-object.editing=TRUE",
                "causeway.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    CausewayPresets.SilenceMetaModel,
    CausewayPresets.SilenceProgrammingModel
})
class DomainObjectResourceTest {

    @Inject private FactoryService factoryService;
    @Inject private ObjectManager objectManager;
    @Inject private DomainObjectResourceServerside domainObjectResourceServerside;

    @Configuration
    @Import({
        DomainObjectResourceServerside.class
    })
    static class TestSetup {

    }

    @Test
    void grid_fromDomainObjectResourceServerside_shouldContainMultiline() {

        assertNotNull(domainObjectResourceServerside);

        val layoutDemo = factoryService.viewModel(LayoutDemo.class);
        val objectAdapter = objectManager.adapt(layoutDemo);
        val spec = objectAdapter.getSpecification();
        val domainType = spec.getLogicalTypeName();
        val instanceId = objectManager.bookmarkObjectElseFail(objectAdapter).getIdentifier(); //TODO also needs URL encoding

        val layoutResourceDescriptor =
                ResourceDescriptor
                .of(RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val resourceContext = domainObjectResourceServerside.resourceContextForTesting(layoutResourceDescriptor, /*params*/null);

        val grid = (BSGrid) spec.getFacet(GridFacet.class).getGrid(objectAdapter);

        DomainObjectResourceServerside.addLinks(resourceContext, domainType, instanceId, grid);

        assertNotNull(grid);

        val jaxbEntity = SerializationStrategy.JSON_INDENTED.entity(grid);

        assertNotNull(jaxbEntity);

        val filteredResult = _Strings.grep(jaxbEntity.toString(), "multiLine")
                .map(String::trim)
                .collect(Collectors.joining());

        assertTrue(filteredResult.contains(" 3,"),
                String.format("multiLine is expected to be populated, got '%s'", filteredResult));

    }

    @Test
    void blobProperty_fromDomainObjectResourceServerside_shouldHaveActionSemantics_GET() {

        assertNotNull(domainObjectResourceServerside);

        val blobDemo = factoryService.viewModel(BlobDemo.class);
        val objectAdapter = objectManager.adapt(blobDemo);
        val spec = objectAdapter.getSpecification();
        val domainType = spec.getLogicalTypeName();
        val instanceId = objectManager.bookmarkObjectElseFail(objectAdapter).getIdentifier(); //TODO also needs URL encoding

        val layoutResourceDescriptor =
                ResourceDescriptor
                .of(RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val resourceContext = domainObjectResourceServerside.resourceContextForTesting(layoutResourceDescriptor, /*params*/null);

        val grid = (BSGrid) spec.getFacet(GridFacet.class).getGrid(objectAdapter);

        DomainObjectResourceServerside.addLinks(resourceContext, domainType, instanceId, grid);

        assertNotNull(grid);

        val logoProperty = grid.getAllPropertiesById().get("logo");

        assertNotNull(logoProperty);

        val jaxbEntity = SerializationStrategy.JSON_INDENTED.entity(logoProperty);
        //System.out.println(jaxbEntity);

        assertNotNull(jaxbEntity);

        val linkCountHavingGET = _Strings.grep(jaxbEntity.toString(), "\"method\"")
                .map(String::trim)
                .filter(s->s.contains("GET"))
                .count();

        assertEquals(1L, linkCountHavingGET);

    }


}
