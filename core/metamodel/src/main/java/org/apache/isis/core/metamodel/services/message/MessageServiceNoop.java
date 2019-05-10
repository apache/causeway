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

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.ioc.PriorityConstants;

@Singleton @Priority(PriorityConstants.PRIORITY_LOWEST)
public class MessageServiceNoop implements MessageService {

    @Override
    public void informUser(final String message) {
        throw new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }

    @Override
    public String informUser(
            final TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod) {
        throw notSupported();
    }
    
    @Override
    public String informUser(TranslatableString message, String translationContext) {
        throw notSupported();
    }

    @Override
    public void warnUser(final String message) {
        throw notSupported();
    }

    @Override public String warnUser(
            final TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod) {
        throw notSupported();
    }
    
    @Override
    public String warnUser(TranslatableString message, String translationContext) {
        throw notSupported();
    }

    @Override
    public void raiseError(final String message) {
        throw notSupported();
    }

    @Override public String raiseError(
            final TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod) {
        throw notSupported();
    }
    
    @Override
    public String raiseError(TranslatableString message, String translationContext) {
        throw notSupported();
    }
    
    // -- HELPER
    
    private static UnsupportedOperationException notSupported() {
        return new UnsupportedOperationException("Not supported by this implementation of RuntimeContext");
    }
    

}
