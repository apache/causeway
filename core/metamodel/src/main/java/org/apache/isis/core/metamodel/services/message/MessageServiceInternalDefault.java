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

package org.apache.isis.core.metamodel.services.message;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.core.metamodel.services.msgbroker.MessageBrokerServiceInternal;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class MessageServiceInternalDefault implements MessageService {

    @Programmatic
    @Override
    public void informUser(final String message) {
        messageBrokerServiceInternal.informUser(message);
    }

    @Override
    public String informUser(final TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        return message.translate(translationService, context(contextClass, contextMethod));
    }

    @Programmatic
    @Override
    public void warnUser(final String message) {
        messageBrokerServiceInternal.warnUser(message);
    }

    @Override
    public String warnUser(final TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        return message.translate(translationService, context(contextClass, contextMethod));
    }

    @Programmatic
    @Override
    public void raiseError(final String message) {
        messageBrokerServiceInternal.raiseError(message);
    }

    @Override
    public String raiseError(final TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        final String translatedMessage = message.translate(translationService, context(contextClass, contextMethod));
        messageBrokerServiceInternal.raiseError(translatedMessage);
        return translatedMessage;
    }

    private static String context(final Class<?> contextClass, final String contextMethod) {
        return contextClass.getName()+"#"+contextMethod;
    }



    @javax.inject.Inject
    MessageBrokerServiceInternal messageBrokerServiceInternal;

    @javax.inject.Inject
    TranslationService translationService;

}
