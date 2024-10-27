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
package org.apache.causeway.testdomain.conf;

import javax.inject.Singleton;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.commons.CausewayModulePersistenceCommons;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testdomain.util.interaction.DomainObjectTesterFactory;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;

@Configuration
@Import({
    CausewayModuleCoreRuntimeServices.class,
    CausewayModulePersistenceCommons.class,
    CausewayModuleSecurityBypass.class,
    KVStoreForTesting.class, // Helper for JUnit Tests
    DomainObjectTesterFactory.class // Helper for JUnit Tests
})
@PropertySources({
    @PropertySource(CausewayPresets.NoTranslations),
})
public class Configuration_headless {

    @Bean @Singleton
    public PlatformTransactionManager platformTransactionManager() {
        return new AbstractPlatformTransactionManager() {

            @Override
            protected Object doGetTransaction() throws TransactionException {
                return null;
            }

            @Override
            protected void doBegin(final Object transaction, final TransactionDefinition definition) throws TransactionException {

            }

            @Override
            protected void doCommit(final DefaultTransactionStatus status) throws TransactionException {

            }

            @Override
            protected void doRollback(final DefaultTransactionStatus status) throws TransactionException {

            }
        };
    }

}
