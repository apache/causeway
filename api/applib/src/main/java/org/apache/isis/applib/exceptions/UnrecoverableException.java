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

package org.apache.isis.applib.exceptions;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;

/**
 * Indicates that an unexpected, non-recoverable (fatal) exception has occurred within
 * the application logic.
 * <p>
 * Throwing this exception will (dependent on the viewer) result in some sort of an error 
 * page being displayed to the user.
 *
 * @see RecoverableException
 * @since 1.x {@index}
 */
public class UnrecoverableException 
extends RuntimeException 
implements TranslatableException {

    private static final long serialVersionUID = 1L;

    @Getter(onMethod_ = {@Override})
    private final TranslatableString translatableMessage;
    
    @Getter(onMethod_ = {@Override}) 
    private final TranslationContext translationContext;
    
    public UnrecoverableException(final String msg) {
        this(msg, null, null, null, null);
    }

    public UnrecoverableException(
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod) {
        this(null, translatableMessage, translationContextClass, translationContextMethod, null);
    }

    public UnrecoverableException(final Throwable cause) {
        this(null, null, null, null, cause);
    }

    public UnrecoverableException(final String msg, final Throwable cause) {
        this(msg, null, null, null, cause);
    }

    public UnrecoverableException(
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod,
            final Throwable cause) {
        this(null, translatableMessage, translationContextClass, translationContextMethod, cause);
    }

    private UnrecoverableException(
            final String message,
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod,
            final Throwable cause) {
        super(message, cause);
        this.translatableMessage = translatableMessage;
        this.translationContext = translationContextClass != null
                ? TranslationContext.ofName(
                        translationContextClass.getName() 
                        + (_Strings.isNotEmpty(translationContextMethod)
                                ? "#" + translationContextMethod
                                : ""))
                : TranslationContext.empty();
    }

    @Override
    public String getMessage() {
        return getTranslatableMessage() != null
                ? getTranslatableMessage().getPattern()
                : super.getMessage();
    }

}
