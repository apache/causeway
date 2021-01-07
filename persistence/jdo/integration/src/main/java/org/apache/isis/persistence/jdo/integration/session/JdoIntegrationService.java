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
package org.apache.isis.persistence.jdo.integration.session;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.events.AppLifecycleEvent;
import org.apache.isis.persistence.jdo.datanucleus.config.DnSettings;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisJdoIntegration.JdoIntegrationService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class JdoIntegrationService {

    @Inject MetaModelContext metaModelContext;
    @Inject TransactionAwarePersistenceManagerFactoryProxy txAwarePmfProxy;
    
    @Named("jdo-platform-transaction-manager")
    @Inject PlatformTransactionManager txManager;
    
    @Inject IsisBeanTypeRegistry isisBeanTypeRegistry;
    @Inject DnSettings dnSettings;

    @PostConstruct
    public void init() {
        if(log.isDebugEnabled()) {
            log.debug("init entity types {}", 
                    isisBeanTypeRegistry.getEntityTypesJdo());
        }
    }

    @EventListener(AppLifecycleEvent.class)
    public void onAppLifecycleEvent(AppLifecycleEvent event) {

        log.debug("received app lifecycle event {}", event);

        switch (event) {
        case PRE_METAMODEL:
            break;
        case POST_METAMODEL:
            new DnApplication(metaModelContext, dnSettings); // creates schema
            break;

        default:
            throw _Exceptions.unmatchedCase(event);
        }

    }


}
