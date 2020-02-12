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
package org.apache.isis.testdomain.layout;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertNotNull;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.model.layout.Configuration_usingLayout;
import org.apache.isis.testdomain.model.layout.LayoutDemo;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.viewer.resources.DomainObjectResourceServerside;
import org.apache.isis.viewer.restfulobjects.viewer.resources.ResourceDescriptor;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                Configuration_usingLayout.class,
                DomainObjectLayoutTest.TestSetup.class
        }, 
        properties = {
                "isis.core.meta-model.introspector.mode=FULL",
                "isis.applib.annotation.domain-object.editing=TRUE",
                "isis.core.meta-model.validator.explicit-object-type=FALSE", // does not override any of the imports
                "logging.level.DependentArgUtils=DEBUG"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
class DomainObjectLayoutTest {
    
    @Inject private FactoryService factoryService;
    @Inject private IsisSystemEnvironment isisSystemEnvironment; 
    
    @Inject private MetaModelService metaModelService;
    @Inject private ObjectManager objectManager;
    @Inject private SpecificationLoader specificationLoader;
    @Inject private DomainObjectResourceServerside domainObjectResourceServerside;

    @Configuration
    @Import({
        DomainObjectResourceServerside.class
    })
    static class TestSetup {
        
    }
    
    @Test
    void grid_shouldContainMultiline() {
        
        assertNotNull(domainObjectResourceServerside);
        
        val layoutDemo = factoryService.viewModel(LayoutDemo.class);
        val adapter = objectManager.adapt(layoutDemo);
        val spec = adapter.getSpecification();
        val domainType = spec.getSpecId().asString();
        val instanceId = objectManager.identifyObject(adapter).getIdentifier();
        
        val layoutResourceDescriptor = 
                ResourceDescriptor
                .of(RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);
        
        val resourceContext = domainObjectResourceServerside.resourceContextForTesting(layoutResourceDescriptor, /*params*/null);
        
        val grid = (BS3Grid) domainObjectResourceServerside.layoutAsGrid(resourceContext, domainType, instanceId)
                .orElseThrow(_Exceptions::noSuchElement);
        
        System.out.println(grid.toString());
        
        //TODO implement
    }

}
