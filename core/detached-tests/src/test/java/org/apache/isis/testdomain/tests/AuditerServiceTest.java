/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.testdomain.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.audit.AuditerService;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.testdomain.jdo.Book;
import org.apache.isis.testdomain.jdo.JdoTestDomainIntegTest;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;

import lombok.val;

/**
 * Depends on {@link JdoBootstrappingTest} to succeed.
 */
@Disabled //TODO[2112] activate tests
class AuditerServiceTest extends JdoTestDomainIntegTest {

    @BeforeEach
    void setUp() {
        
        // cleanup
        fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.PurgeAll.builder());
        
        // given
        fixtureScripts.runBuilderScript(
                JdoTestDomainPersona.InventoryWith1Book.builder());
    }
    
    @Test
    void auditerServiceShouldBeAwareOfInventoryChanges() {
        
        val auditerServiceAny = IsisContext.getServiceRegistry()
                .lookupServiceElseFail(AuditerService.class);
        
        // priority based service lookup (could be a test on its own)
        assertEquals(AuditerServiceStub.class, auditerServiceAny.getClass());
        

        // given
        val auditerService = (AuditerServiceStub) auditerServiceAny; 
        val repository = IsisContext.getServiceRegistry()
                .lookupServiceElseFail(RepositoryService.class);
        val txMan = IsisContext.getPersistenceSession().get().getTransactionManager();
        val book = repository.allInstances(Book.class).listIterator().next();
        
        // when
        auditerService.clearHistory();
        book.setName("Book #2");
        repository.persist(book);
        txMan.endTransaction();
        
        // then 
        assertEquals(
                "targetClassName=Book,propertyName=name,preValue=Sample Book,postValue=Book #2;", 
                auditerService.getHistory());
    }
    
    // -- HELPER
        
    @DomainService(menuOrder="1.0", nature=NatureOfService.DOMAIN)
    @DomainServiceLayout()
    public static class AuditerServiceStub implements AuditerService {

        private StringBuilder history = new StringBuilder();
        
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void audit(
                UUID interactionId, int sequence, String targetClassName, Bookmark target,
                String memberIdentifier, String propertyName, 
                String preValue, String postValue, String user,
                Timestamp timestamp) {
            
            history
            .append("targetClassName=").append(targetClassName).append(",")
            .append("propertyName=").append(propertyName).append(",")
            .append("preValue=").append(preValue).append(",")
            .append("postValue=").append(postValue).append(";")
            ;
        }
        
        void clearHistory() {
            history = new StringBuilder();
        }
        
        String getHistory() {
            return history.toString();
        }

    }
    
}
