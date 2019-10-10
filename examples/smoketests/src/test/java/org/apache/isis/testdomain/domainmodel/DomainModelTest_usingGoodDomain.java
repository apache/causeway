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
package org.apache.isis.testdomain.domainmodel;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.config.IsisPresets;
import org.apache.isis.integtestsupport.validate.ValidateDomainModel;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.Product;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;
import org.apache.isis.testdomain.model.good.ProperActionSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                Configuration_usingValidDomain.class,
                
        }, 
        properties = {
                "isis.reflector.introspector.mode=FULL",
                "isis.reflector.validator.explicitObjectType=FALSE", // does not override any of the imports
        })
@TestPropertySource({
    IsisPresets.DebugValidation,
    //IsisPresets.DebugProgrammingModel,
    
})
//@Transactional
class DomainModelTest_usingGoodDomain {
    
//    @Inject private MetaModelService metaModelService;
//    @Inject private JaxbService jaxbService;
//    @Inject private FactoryService factoryService;
    @Inject private SpecificationLoader specLoader;

    void debug() {
           
        
//        val config = new MetaModelService.Config()
////              .withIgnoreNoop()
////              .withIgnoreAbstractClasses()
////              .withIgnoreBuiltInValueTypes()
////              .withIgnoreInterfaces()
//                //.withPackagePrefix("*")
//                .withPackagePrefix("org.apache.isis.testdomain.")
//                ;
//
//        System.out.println("!!! listing MM");
//        val metamodelDto = metaModelService.exportMetaModel(config);
//        for (DomainClassDto domainClass : metamodelDto.getDomainClassDto()) {
//            System.out.println("dc: " + domainClass.getId());
//            val xmlString = jaxbService.toXml(domainClass);
//            System.out.println(xmlString);
//        }
//        System.out.println("!!! ---");
    }
    
    @Test
    void goodDomain_shouldPassValidation() {
        assertFalse(specLoader.snapshotSpecifications().isEmpty());
        
        val validateDomainModel = new ValidateDomainModel();
        validateDomainModel.run(); // should not throw
    }
    
    @Test
    void typeLevelAnnotations_shouldBeHonored_onMixins() {
        
        val holderSpec = specLoader.loadSpecification(ProperActionSupport.class);
        
        val mx_mixin = holderSpec.getObjectAction("mixin"); // proper mix-in support
        assertNotNull(mx_mixin);
        
        val mx_action = holderSpec.getObjectAction("action"); // when @Action at type level
        assertNotNull(mx_action);
        assertEquals("action", mx_action.getId());
        assertEquals("foo", mx_action.getName());
        assertEquals("bar", mx_action.getDescription());
        
        val mx_property = holderSpec.getAssociation("property"); // when @Property at type level
        assertNotNull(mx_property);
        assertEquals("property", mx_property.getId());
        assertEquals("foo", mx_property.getName());
        assertEquals("bar", mx_property.getDescription());
        
        val mx_property2 = holderSpec.getAssociation("property2"); // when @Property at method level
        assertNotNull(mx_property2);
        assertEquals("property2", mx_property2.getId());
        assertEquals("foo", mx_property2.getName());
        assertEquals("bar", mx_property2.getDescription());
        
        val mx_collection = holderSpec.getAssociation("collection"); // when @Collection at type level
        assertNotNull(mx_collection);
        assertEquals("collection", mx_collection.getId());
        assertEquals("foo", mx_collection.getName());
        assertEquals("bar", mx_collection.getDescription());
        
        val mx_collection2 = holderSpec.getAssociation("collection2"); // when @Collection at method level
        assertNotNull(mx_collection2);
        assertEquals("collection2", mx_collection2.getId());
        assertEquals("foo", mx_collection2.getName());
        assertEquals("bar", mx_collection2.getDescription());
        
    }
    
    @Test
    void memberLevelAnnotations_shouldResolveUnambiguous_onMixins() {
        
        val holderSpec = specLoader.loadSpecification(ProperActionSupport.class);
        
        val mx_openRestApi = holderSpec.getObjectAction("openRestApi"); // built-in mixin support
        assertNotNull(mx_openRestApi);
        
        assertThrows(Exception.class, ()->holderSpec.getAssociation("openRestApi")); // should not be picked up as a property
        
    }
    
    @Test
    void pluginProvidedMixins_shouldBePickedUp() {
        
        val holderSpec = specLoader.loadSpecification(Product.class);
        
        val mx_datanucleusIdLong = holderSpec.getAssociation("datanucleusIdLong"); // plugged in mixin
        assertNotNull(mx_datanucleusIdLong);
        
    }
    

}
