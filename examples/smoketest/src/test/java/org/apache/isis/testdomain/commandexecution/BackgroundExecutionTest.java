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
package org.apache.isis.testdomain.commandexecution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.jdo.InventoryManager;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.Product;

import lombok.val;

@SpringBootTest(
        classes = { 
                JdoTestDomainModule.class,
        }, 
        properties = {
                "logging.config=log4j2-test.xml",
                "logging.level.org.apache.isis.jdo.transaction.IsisPlatformTransactionManagerForJdo=DEBUG",
                // "isis.reflector.introspector.parallelize=false",
                //"logging.level.org.apache.isis.metamodel.specloader.specimpl.ObjectSpecificationAbstract=TRACE"
        })
class BackgroundExecutionTest {
    
    @Inject private FixtureScripts fixtureScripts;
    @Inject private RepositoryService repository;

    @BeforeEach
    void setUp() {
        // cleanup
        fixtureScripts.runBuilderScript(JdoTestDomainPersona.PurgeAll.builder());

        // given
        fixtureScripts.runBuilderScript(JdoTestDomainPersona.InventoryWith1Book.builder());
    }

    @Test
    void testBackgroundService() {
        
        val inventoryManager = facoryService.viewModel(InventoryManager.class, null);
        val product = repository.allInstances(Product.class).get(0);
        
        backgroundService.execute(inventoryManager).updateProductPrice(product, 123);
        
        assertEquals(123d, product.getPrice(), 1E-6);
        
    }
    
    //public static class
    
    @Inject BackgroundService backgroundService;
    @Inject FactoryService facoryService;

}
