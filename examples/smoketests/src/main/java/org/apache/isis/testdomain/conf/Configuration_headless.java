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
package org.apache.isis.testdomain.conf;

import java.util.function.Supplier;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.incubator.model.metamodel.IsisModuleIncModelMetaModel;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;

@Configuration
@Import({
    IsisModuleCoreRuntimeServices.class,
    IsisModuleSecurityBypass.class,
    IsisModuleIncModelMetaModel.class // @Model support
})
@PropertySources({
    @PropertySource(IsisPresets.NoTranslations),
})
public class Configuration_headless {

    @Bean @Singleton
    public TransactionService transactionService() {
        return new TransactionService() {

            @Override
            public TransactionId currentTransactionId() {
                return null;
            }

            @Override
            public void flushTransaction() {
            }

            @Override
            public TransactionState currentTransactionState() {
                return null;
            }

            @Override
            public void executeWithinTransaction(Runnable task) {
            }

            @Override
            public <T> T executeWithinTransaction(Supplier<T> task) {
                return null;
            }

            @Override
            public void executeWithinNewTransaction(Runnable task) {
            }

            @Override
            public <T> T executeWithinNewTransaction(Supplier<T> task) {
                return null;
            }

        };
    }
    
    @Bean @Singleton
    public PlatformTransactionManager platformTransactionManager() {
        return new PlatformTransactionManager() {
            
            @Override
            public void rollback(TransactionStatus status) throws TransactionException {
            }
            
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
                return null;
            }
            
            @Override
            public void commit(TransactionStatus status) throws TransactionException {
            }
        };
    }
    
    
    @Bean @Singleton
    public MetricsService metricsService() {
        return new MetricsService() {
            
            @Override
            public int numberObjectsLoaded() {
                return 0;
            }
            
            @Override
            public int numberObjectsDirtied() {
                return 0;
            }
        };
    }


}