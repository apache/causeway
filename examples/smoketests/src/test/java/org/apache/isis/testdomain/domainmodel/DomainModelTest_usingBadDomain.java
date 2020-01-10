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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.integtestsupport.validate.ValidateDomainModel;
import org.apache.isis.metamodel.spec.DomainModelException;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_headless;
import org.apache.isis.testdomain.model.bad.AmbiguousTitle;
import org.apache.isis.testdomain.model.bad.Configuration_usingInvalidDomain;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedActionSupport;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedCollectionSupport;
import org.apache.isis.testdomain.model.bad.InvalidOrphanedPropertySupport;
import org.apache.isis.testdomain.model.bad.InvalidPropertyAnnotationOnAction;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_headless.class,
                Configuration_usingInvalidDomain.class
        }, 
        properties = {
                "isis.reflector.introspector.mode=FULL"
        })
@TestPropertySource({
    //IsisPresets.DebugMetaModel,
    //IsisPresets.DebugProgrammingModel,
    IsisPresets.SilenceMetaModel,
    IsisPresets.SilenceProgrammingModel
})
//@Incubating("does not work, when executed in sequence with other smoketests")
class DomainModelTest_usingBadDomain {
    
    @Inject private IsisConfiguration configuration;
    @Inject private IsisSystemEnvironment isisSystemEnvironment;
    @Inject private SpecificationLoader specificationLoader;
    //@Inject private ApplicationContext applicationContext;
    
    
    @Test
    void fullIntrospection_shouldBeEnabledByThisTestClass() {
        assertTrue(IntrospectionMode.isFullIntrospect(configuration, isisSystemEnvironment));
    }
    
    @Test
    void ambiguousTitle_shouldFail() {
           
        val validateDomainModel = new ValidateDomainModel(specificationLoader);
        
        assertThrows(DomainModelException.class, validateDomainModel::run);
        assertTrue(validateDomainModel.anyMatchesContaining(
                AmbiguousTitle.class, 
                "conflict for determining a strategy for retrieval of title"));
    }
    
    @Test
    void orphanedActionSupport_shouldFail() {
           
        val validateDomainModel = new ValidateDomainModel(specificationLoader);
        
        assertThrows(DomainModelException.class, validateDomainModel::run);
        assertTrue(validateDomainModel.anyMatchesContaining(
                InvalidOrphanedActionSupport.class, 
                "is assumed to support"));
    }
    
//    @Test
//    void orphanedActionSupportNotEnforced_shouldFail() {
//           
//        val validateDomainModel = new ValidateDomainModel();
//        
//        assertThrows(DomainModelException.class, validateDomainModel::run);
//        assertTrue(validateDomainModel.anyMatchesContaining(
//                OrphanedPrefixedAction.class, 
//                "is assumed to support"));
//    }

    @Test
    void orphanedPropertySupport_shouldFail() {
           
        val validateDomainModel = new ValidateDomainModel(specificationLoader);
        
        assertThrows(DomainModelException.class, validateDomainModel::run);
        assertTrue(validateDomainModel.anyMatchesContaining(
                InvalidOrphanedPropertySupport.class, 
                "is assumed to support"));
    }
    
    @Test
    void orphanedCollectionSupport_shouldFail() {
           
        val validateDomainModel = new ValidateDomainModel(specificationLoader);
        
        assertThrows(DomainModelException.class, validateDomainModel::run);
        assertTrue(validateDomainModel.anyMatchesContaining(
                InvalidOrphanedCollectionSupport.class, 
                "is assumed to support"));
    }
    
    @Test @Disabled("this case has no vaildation refiner yet")
    void invalidPropertyAnnotationOnAction_shouldFail() {
        
        val validateDomainModel = new ValidateDomainModel(specificationLoader);
        
        assertThrows(DomainModelException.class, validateDomainModel::run);
        assertTrue(validateDomainModel.anyMatchesContaining(
                InvalidPropertyAnnotationOnAction.class, 
                "TODO"));
    }
    

    

}
