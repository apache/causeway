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
package org.apache.causeway.extensions.pdfjs.metamodel;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.approvaltests.Approvals;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.metamodel.Config;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.commons.CausewayModulePersistenceCommons;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.integtestsupport.applib.ApprovalsOptions;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

public abstract class PdfjsViewer_Abstract_IntegTest extends CausewayIntegrationTestAbstract {

    public abstract Class<?> getDomainModuleClass();

    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModuleCoreRuntimeServices.class,
            CausewayModulePersistenceCommons.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.UseLog4j2Test)
    })
    public static class AppManifestBase {

        @Bean
        @Singleton
        public PlatformTransactionManager platformTransactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() throws TransactionException {
                    return new Object();
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

    void dump_facets() {
        var metamodelDto = metaModelService.exportMetaModel(
                Config.builder()
                        .namespacePrefixes(Collections.singleton(getDomainModuleClass().getPackageName()))
                        .ignoreMixins(false)
                        .build());
        var xml = jaxbService.toXml(metamodelDto);

        Approvals.verifyXml(xml, ApprovalsOptions.xmlOptions());
    }

    @Inject MetaModelService metaModelService;
    @Inject JaxbService jaxbService;

}
