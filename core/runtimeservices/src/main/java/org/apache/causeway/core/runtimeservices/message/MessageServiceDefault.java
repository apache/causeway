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
package org.apache.causeway.core.runtimeservices.message;

import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.services.i18n.TranslatableString;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.core.metamodel.services.message.MessageBroker;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".MessageServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class MessageServiceDefault implements MessageService {

    @Inject private TranslationService translationService;

    @Autowired(required = false)
    private Provider<MessageBroker> sessionScopedMessageBroker;

    @Override
    public void informUser(final String message) {
        currentMessageBroker()
        .ifPresent(broker->broker.addMessage(message));
    }

    @Override
    public String informUser(
            final TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod) {
        return informUser(message, context(contextClass, contextMethod));
    }

    @Override
    public String informUser(
            final TranslatableString message,
            final TranslationContext translationContext) {
        String translatedMessage = message.translate(translationService, translationContext);
        informUser(translatedMessage);
        return translatedMessage;
    }

    @Override
    public void warnUser(final String message) {
        currentMessageBroker()
        .ifPresent(broker->broker.addWarning(message));
    }

    @Override
    public String warnUser(
            final TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod) {
        return warnUser(message, context(contextClass, contextMethod));
    }

    @Override
    public String warnUser(
            final TranslatableString message,
            final TranslationContext translationContext) {
        String translatedMessage = message.translate(translationService, translationContext);
        warnUser(translatedMessage);
        return translatedMessage;
    }

    @Override
    public void raiseError(final String message) {
        throw new RecoverableException(message);
    }

    @Override
    public void setError(final String message) {
        currentMessageBroker()
        .ifPresent(broker->broker.setApplicationError(message));
    }

    @Override
    public String raiseError(
            final TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod) {
        return raiseError(message, context(contextClass, contextMethod));
    }

    @Override
    public String raiseError(
            final TranslatableString message,
            final TranslationContext translationContext) {
        final String translatedMessage = message.translate(translationService, translationContext);
        raiseError(translatedMessage);
        return translatedMessage;
    }

    // -- HELPER

    private static TranslationContext context(final Class<?> contextClass, final String contextMethodName) {
        return TranslationContext.forMethod(contextClass, contextMethodName);
    }

    private Optional<MessageBroker> currentMessageBroker() {
        return Optional.ofNullable(sessionScopedMessageBroker) // only available with web contexts (Spring)
        .map(Provider::get);
    }

}
