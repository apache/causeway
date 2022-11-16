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

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_headless;
import org.apache.causeway.testdomain.model.actnsemantics.Configuration_usingActionSemantics;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.MenuBarsResourceServerside;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.ResourceDescriptor;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.serialization.SerializationStrategy;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_headless.class,
                Configuration_usingActionSemantics.class,
                MenuBarsResourceTest.TestSetup.class
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
class MenuBarsResourceTest {

    @Inject private MenuBarsResourceServerside menuBarsResourceServerside;
    @Inject private MenuBarsService menuBarsService;

    @Configuration
    @Import({
        MenuBarsResourceServerside.class
    })
    static class TestSetup {

    }

    @Test
    void blobDemoMenu_fromMenuBarsResourceServerside_shouldBe_GET() {

        assertNotNull(menuBarsResourceServerside);

        val layoutResourceDescriptor =
                ResourceDescriptor
                .of(RepresentationType.MENUBARS, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val resourceContext = menuBarsResourceServerside.resourceContextForTesting(layoutResourceDescriptor, /*params*/null);
        val linksForServiceActionsAddingVisitor = MenuBarsResourceServerside.linksForServiceActionsAddingVisitor(resourceContext);

        val menuBars = menuBarsService.menuBars();

        menuBars.visit(linksForServiceActionsAddingVisitor);

        assertNotNull(menuBars);

        val blobDemoMenuRef = _Refs.<ServiceActionLayoutData>objectRef(null);

        // find service action by object-type
        menuBars.visit(actionLayoutData->{
            if("regressiontests.BlobDemoMenu".equals(actionLayoutData.getObjectType())) {
                blobDemoMenuRef.setValue(actionLayoutData);
            }
        });

        val blobDemoMenu = blobDemoMenuRef.getValue().orElse(null);

        assertNotNull(blobDemoMenu);

        val jaxbEntity = SerializationStrategy.JSON_INDENTED.entity(blobDemoMenu);

        assertNotNull(jaxbEntity);

        final long methodCount = _Strings.grep(jaxbEntity.toString(), "\"method\"")
        .filter(line->line.contains("GET"))
        .count();

        assertEquals(1L, methodCount);

    }

}
