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

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.services.command.CommandServiceInternal;
import org.apache.isis.core.runtime.events.iactn.IsisInteractionLifecycleEvent;
import org.apache.isis.extensions.cors.impl.IsisModuleExtCorsImpl;
import org.apache.isis.extensions.modelannotation.metamodel.IsisModuleExtModelAnnotation;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.springframework.context.annotation.*;

import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationService;
import org.apache.isis.extensions.secman.api.permission.PermissionsEvaluationServiceAllowBeatsVeto;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import java.util.Optional;

@Configuration
@Import({
        IsisModuleCoreRuntimeServices.class,
        //IsisModuleCoreSecurity.class,
        IsisModuleSecurityBypass.class,
        IsisModuleExtModelAnnotation.class, // @Model support
        IsisModuleExtCorsImpl.class,
        Configuration_headlessButSecure.HeadlessCommandSupport.class,
        KVStoreForTesting.class, // Helper for JUnit Tests
})
public class Configuration_headlessButSecure {

    @Service
    @Order(OrderPrecedence.MIDPOINT)
    @RequiredArgsConstructor(onConstructor_ = {@Inject})
    public static class HeadlessCommandSupport {

        private final Provider<InteractionContext> interactionContextProvider;
        private final CommandServiceInternal commandService;

        @EventListener(IsisInteractionLifecycleEvent.class)
        public void onIsisInteractionLifecycleEvent(IsisInteractionLifecycleEvent event) {
            switch (event.getEventType()) {
                case HAS_STARTED:
                    _Probe.errOut("Interaction HAS_STARTED conversationId=%s", event.getConversationId());
                    setupCommandCreateIfMissing();
                    break;
                case IS_ENDING:
                    _Probe.errOut("Interaction IS_ENDING conversationId=%s", event.getConversationId());
                    break;
                default:
                    break;
            }
        }

        public void setupCommandCreateIfMissing() {
            val interactionContext = interactionContextProvider.get();
            @SuppressWarnings("unused") final Interaction interaction = Optional.ofNullable(interactionContext.getInteraction())
                    .orElseGet(() -> {
                        val newCommand = new Command();
                        val newInteraction = new Interaction(newCommand);
                        interactionContext.setInteraction(newInteraction);
                        return newInteraction;
                    });
        }

    }

    @Bean
    @Singleton
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

    @Bean
    @Singleton
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

    @Bean
    @Singleton
    public SecurityModuleConfig securityModuleConfigBean() {
        return SecurityModuleConfig.builder()
                .build();
    }

    @Bean
    @Singleton
    public PermissionsEvaluationService permissionsEvaluationService() {
        return new PermissionsEvaluationServiceAllowBeatsVeto();
    }

}