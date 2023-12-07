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
package org.apache.causeway.regressiontests.layouts.integtest;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import org.apache.causeway.applib.CausewayModuleApplibMixins;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.regressiontests.layouts.integtest.model.LayoutTestDomainModel;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testdomain.util.interaction.DomainObjectTesterFactory;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;
import org.apache.causeway.viewer.wicket.applib.CausewayModuleViewerWicketApplibMixins;

abstract class LayoutTestAbstract extends CausewayIntegrationTestAbstract {

    @Inject protected DomainObjectTesterFactory testerFactory;

    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleApplibMixins.class,
            CausewayModuleViewerWicketApplibMixins.class,
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            DomainObjectTesterFactory.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.UseLog4j2Test)
    })
    @ComponentScan(basePackageClasses = {AppManifest.class, LayoutTestDomainModel.class})
    public static class AppManifest {

        @Bean
        @Singleton
        public PlatformTransactionManager platformTransactionManager() {
            return new PlatformTransactionManager() {

                @Override
                public void rollback(final TransactionStatus status) throws TransactionException {
                }

                @Override
                public TransactionStatus getTransaction(final TransactionDefinition definition) throws TransactionException {
                    return null;
                }

                @Override
                public void commit(final TransactionStatus status) throws TransactionException {
                }
            };
        }

    }

}
