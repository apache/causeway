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
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.base._Strings;

/**
 * Indicates that an exceptional condition/problem has occurred within the application's domain logic.
 * <p>
 * Throwing this exception is equivalent to calling {@link MessageService#raiseError(String)}.
 * The framework will trap the error and display the exception message as a warning.
 * <p>
 * This exception should only be thrown for &quot;recoverable&quot; exceptions, that is, those which
 * could be anticipated by the application. It should not be thrown for fatal, unanticipated exceptions.
 * <p>
 * The framework attempts to apply some heuristics; if the underlying Isis transaction has been aborted
 * (for example as the result of a problem persisting some data) but then the application attempts to
 * throw this exception, the exception will be promoted to a fatal exception.
 *
 * @see UnrecoverableException
 * @since 1.x {@index}
 */
public class RecoverableException 
extends RuntimeException 
implements TranslatableException {

    private static final long serialVersionUID = 1L;

    private final TranslatableString translatableMessage;
    private final String translationContext;

    public RecoverableException(final String msg) {
        this(msg, null, null, null, null);
    }

    public RecoverableException(
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod) {
        this(null, translatableMessage, translationContextClass, translationContextMethod, null);
    }

    public RecoverableException(final Throwable cause) {
        this(null, null, null, null, cause);
    }

    public RecoverableException(final String msg, final Throwable cause) {
        this(msg, null, null, null, cause);
    }

    public RecoverableException(
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod,
            final Throwable cause) {
        this(null, translatableMessage, translationContextClass, translationContextMethod, cause);
    }

    private RecoverableException(
            final String message,
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod,
            final Throwable cause) {
        super(message, cause);
        this.translatableMessage = translatableMessage;
        this.translationContext =
                translationContextClass != null
                ? (translationContextClass.getName() +
                        (!_Strings.isNullOrEmpty(translationContextMethod)
                                ? "#" + translationContextMethod
                                        : "")
                        )
                        : null;
    }

    @Override
    public String getMessage() {
        return getTranslatableMessage() != null
                ? getTranslatableMessage().getPattern()
                        : super.getMessage();
    }

    @Override
    public TranslatableString getTranslatableMessage() {
        return translatableMessage;
    }

    @Override
    public String getTranslationContext() {
        return translationContext;
    }


}
