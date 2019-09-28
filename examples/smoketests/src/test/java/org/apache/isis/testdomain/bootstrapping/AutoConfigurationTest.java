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
package org.apache.isis.testdomain.bootstrapping;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.commons.internal.ioc.spring._Spring;
import org.apache.isis.config.IsisPresets;
import org.apache.isis.config.beans.IsisBeanFactoryPostProcessorForSpring;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.testdomain.Incubating;
//import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingStereotypes;
import org.apache.isis.testdomain.model.stereotypes.MyObject;
import org.apache.isis.testdomain.model.stereotypes.MyObject_mixin;
import org.apache.isis.testdomain.model.stereotypes.MyService;
import org.apache.isis.testdomain.model.stereotypes.MyView;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                //AutoConfigurationTest.BeanPostProcessor_forTesting.class,
                IsisBeanFactoryPostProcessorForSpring.class,
                Configuration_usingStereotypes.class
        },
        properties = {
                "logging.config=log4j2-test.xml",
                // "isis.reflector.introspector.parallelize=false",
                // "logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
        })
@TestPropertySource({
    IsisPresets.DebugDiscovery
})
@Incubating("under construction, not tested with surefire yet")
class AutoConfigurationTest {
    
    @Inject private ApplicationContext applicationContext;

    //XXX for debugging and experimenting
    @Component
    static class BeanPostProcessor_forTesting implements BeanPostProcessor {

        // simply return the instantiated bean as-is
        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean; // we could potentially return any object reference here...
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            System.out.println("Bean '" + beanName + "' created : " + bean.toString());
            return bean;
        }
    }
    
    @BeforeEach
    void beforeEach() {
        assertNotNull(applicationContext);
        if(!_Spring.isContextAvailable()) {
            _Spring.init(applicationContext);
        }
    }
    
    @Test
    void domainObjects_shouldBeDiscovered() {

        //TODO  bad test, relying on implementation details
        // clearly IsisBeanTypeRegistry needs refactoring!
        val registry = IsisBeanTypeRegistry.current();
        
        val discoveredTypes = registry.streamAndClearInbox()
        .map(Map.Entry::getKey)
        .collect(Collectors.toSet());
        discoveredTypes.addAll((Set)registry.getEntityTypes());
        discoveredTypes.addAll((Set)registry.getBeanTypes());
        
        for(val cls : nonManaged()) {
            assertTrue(discoveredTypes.contains(cls));
        }
        
        //TODO @DomainObject with NATURE_NOT_SPECIFIED is getting ignored, is this correct behavior?
        
    }

    @Test
    void domainObjects_shouldNotBeManaged() {
        
        for(val cls : nonManaged()) {
            assertThrows(NoSuchBeanDefinitionException.class, ()->{
                applicationContext.getBean(cls);
            });    
        }
        
    }
    
    @Test
    void domainServices_shouldBeManaged() {
        
        val myService = applicationContext.getBean(MyService.class);
        assertNotNull(myService);
        assertNotNull(_Spring.getSingletonElseFail(MyService.class));
        
    }
    
    
    // we don't want those managed by Spring
    private static Class<?>[] nonManaged() {
        val nonManaged = new Class<?>[] {MyObject.class, MyObject_mixin.class, MyView.class};
        return nonManaged;
    }

}
