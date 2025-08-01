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
package org.apache.causeway.core.metamodel.context;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.ascii.AsciiIdentifierService;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Common;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Common.Application;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.message.MessageBroker;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

@Programmatic
@FunctionalInterface
public interface MetaModelContextShortcuts {

    // -- INTERFACE

    MetaModelContext mmc();

    // -- USER CONFIG

    default Optional<UserLocale> currentUserLocale() {
        return mmc().getInteractionService().currentInteractionContext()
                .map(InteractionContext::getLocale);
    }

    // -- SPEC SHORTCUTS

    default Optional<ObjectSpecification> specForType(final @Nullable Class<?> type) {
        return mmc().getSpecificationLoader().specForType(type);
    }

    default ObjectSpecification specForTypeElseFail(final @Nullable Class<?> type) {
        return mmc().getSpecificationLoader().specForTypeElseFail(type);
    }

    // -- SERVICE SHORTCUTS

    default <T> Optional<T> lookupService(final Class<T> serviceClass) {
        return mmc().getServiceRegistry().lookupService(serviceClass);
    }

    default <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return mmc().getServiceRegistry().lookupServiceElseFail(serviceClass);
    }

    default <T> T lookupServiceElseFallback(final Class<T> serviceClass, final Supplier<T> fallback) {
        return mmc().getServiceRegistry().lookupService(serviceClass)
                .orElseGet(fallback);
    }

    default <T> T loadServiceIfAbsent(final Class<T> type, final @Nullable T instanceIfAny) {
        return instanceIfAny==null
                ? lookupServiceElseFail(type)
                : instanceIfAny;
    }

    default <T> T injectServicesInto(final T pojo) {
        return mmc().getServiceInjector().injectServicesInto(pojo);
    }

    // -- ADVANCED SHORTCUTS

    default Optional<MessageBroker> getMessageBroker() {
        // session scoped!
        return mmc().getServiceRegistry().lookupService(MessageBroker.class);
    }

    default AsciiIdentifierService getAsciiIdentifierService() {
        return mmc().getServiceRegistry().lookupService(AsciiIdentifierService.class).orElse(featureId -> featureId);
    }

    default ManagedObject lookupServiceAdapterById(final String serviceId) {
        return lookupServiceAdapterById(serviceId);
    }

    default Stream<ManagedObject> streamServiceAdapters() {
        return streamServiceAdapters();
    }

    // -- CONFIG SHORTCUTS

    default Common getCommonViewerSettings() {
        return mmc().getConfiguration().viewer().common();
    }

    default Wicket getWicketViewerSettings() {
        return mmc().getConfiguration().viewer().wicket();
    }

    default Application getApplicationSettings() {
        return getCommonViewerSettings().application();
    }

    /**
     * Translate without context: Tooltips, Button-Labels, etc.
     */
    default String translate(final String input) {
        return mmc().getTranslationService().translate(TranslationContext.empty(), input);
    }

    default String translate(final TranslationContext tc, final String text) {
        return mmc().getTranslationService().translate(tc, text);
    }

    default boolean isPrototyping() {
        return mmc().getSystemEnvironment().isPrototyping();
    }

}
