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

import lombok.val;

import java.util.Collections;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.metamodel.Config;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.integtestsupport.applib.ApprovalsOptions;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;
import org.approvaltests.Approvals;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.apache.causeway.commons.internal.testing._DocumentTester;

public abstract class PdfjsViewer_Abstract_IntegTest extends CausewayIntegrationTestAbstract {

    public abstract Class<?> getDomainModuleClass();

    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModuleCoreRuntimeServices.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.UseLog4j2Test)
    })
    public static class AppManifestBase {

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

    void dump_facets() {
        val metamodelDto = metaModelService.exportMetaModel(
                Config.builder()
                        .namespacePrefixes(Collections.singleton(getDomainModuleClass().getPackageName()))
                        .ignoreMixins(false)
                        .build());
        val xml = jaxbService.toXml(metamodelDto);
        _DocumentTester.assertXmlEqualsIgnoreOrder(xml, ApprovalsOptions.xmlOptions().scrub(xml));
    }

    @Inject MetaModelService metaModelService;
    @Inject JaxbService jaxbService;

}
