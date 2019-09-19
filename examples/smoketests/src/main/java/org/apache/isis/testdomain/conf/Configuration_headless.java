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
import org.apache.isis.config.IsisPresets;
import org.apache.isis.runtime.spring.IsisBoot;
import org.apache.isis.security.IsisBootSecurityBypass;

@Configuration
@Import({
    IsisBoot.class,
    IsisBootSecurityBypass.class,
})
@PropertySources({
    @PropertySource("classpath:/org/apache/isis/testdomain/conf/isis-non-changing.properties"),
    @PropertySource(IsisPresets.NoTranslations),
})
public class Configuration_headless {

    @Bean @Singleton
    public TransactionService transactionService() {
        return new TransactionService() {

            @Override
            public TransactionId currentTransactionId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void flushTransaction() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public TransactionState currentTransactionState() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void executeWithinTransaction(Runnable task) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public <T> T executeWithinTransaction(Supplier<T> task) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void executeWithinNewTransaction(Runnable task) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public <T> T executeWithinNewTransaction(Supplier<T> task) {
                // TODO Auto-generated method stub
                return null;
            }

        };
    }
    
    @Bean @Singleton
    public PlatformTransactionManager platformTransactionManager() {
        return new PlatformTransactionManager() {
            
            @Override
            public void rollback(TransactionStatus status) throws TransactionException {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
                // TODO Auto-generated method stub
                return null;
            }
            
            @Override
            public void commit(TransactionStatus status) throws TransactionException {
                // TODO Auto-generated method stub
                
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