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
package org.apache.isis.core.runtime.services.message;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.ioc.PriorityConstants;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.MessageBroker;

@Singleton @Priority(PriorityConstants.PRIORITY_BELOW_DEFAULT)
public class MessageServiceDefault implements MessageService {

    @Override
    public void informUser(final String message) {
        getMessageBroker().addMessage(message);
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
            final String translationContext) {
        String translatedMessage = message.translate(translationService, translationContext);
        informUser(translatedMessage);
        return translatedMessage;
    }

    @Override
    public void warnUser(final String message) {
        getMessageBroker().addWarning(message);
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
            final String translationContext) {
        String translatedMessage = message.translate(translationService, translationContext);
        warnUser(translatedMessage);
        return translatedMessage;
    }

    @Override
    public void raiseError(final String message) {
        throw new RecoverableException(message);
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
            final String translationContext) {
        final String translatedMessage = message.translate(translationService, translationContext);
        raiseError(translatedMessage);
        return translatedMessage;
    }

    private static String context(final Class<?> contextClass, final String contextMethod) {
        return contextClass.getName()+"#"+contextMethod;
    }

    private MessageBroker getMessageBroker() {
        return isisSessionFactory.getCurrentSession().getAuthenticationSession().getMessageBroker();
    }

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

    @javax.inject.Inject
    TranslationService translationService;

}
