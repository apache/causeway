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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.config.IsisPresets;
import org.apache.isis.integtestsupport.validate.ValidateDomainModel;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.good.Configuration_usingValidDomain;

import lombok.val;

@SpringBootTest(
        classes = { 
                Configuration_headless.class,
                Configuration_usingValidDomain.class
        }, 
        properties = {
                "isis.reflector.introspector.mode=FULL",
                "isis.reflector.validator.explicitObjectType=FALSE", // does not override any of the imports
        })
@TestPropertySource({
    IsisPresets.DebugValidation,
    IsisPresets.DebugProgrammingModel,
    
})
class DomainModelTest_usingGoodDomain {
    
//    @Inject private MetaModelService metaModelService;
//    @Inject private JaxbService jaxbService;

    @Test
    void reservedPrefix_shouldBeAllowedForMembers() {
           
        
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
        
        val validateDomainModel = new ValidateDomainModel();
        validateDomainModel.run(); // should not throw
        
    }
    

}
