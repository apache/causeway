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

package org.apache.isis.applib;

import org.apache.isis.applib.services.exceprecog.TranslatableException;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.functions._Functions;

/**
 * Indicates that an unexpected, non-recoverable (fatal) exception has occurred within
 * the application logic.
 *
 * <p>
 * Throwing this exception will (dependent on the viewer) result in some sort of an error page being displayed to the user.
 *
 * <p>
 * Note that this exception has identical semantics to {@link FatalException} (of which it is the immediate
 * superclass) and can be considered a synonym.
 *
 * @see RecoverableException
 * @see ApplicationException
 * @see FatalException
 */
public class NonRecoverableException extends RuntimeException implements TranslatableException {

    private static final long serialVersionUID = 1L;

    private final TranslatableString translatableMessage;
    private final String translationContext;

    public NonRecoverableException(final String msg) {
        this(msg, null, null, null, null);
    }

    public NonRecoverableException(
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod) {
        this(null, translatableMessage, translationContextClass, translationContextMethod, null);
    }

    public NonRecoverableException(final Throwable cause) {
        this(null, null, null, null, cause);
    }

    public NonRecoverableException(final String msg, final Throwable cause) {
        this(msg, null, null, null, cause);
    }

    public NonRecoverableException(
            final TranslatableString translatableMessage,
            final Class<?> translationContextClass,
            final String translationContextMethod,
            final Throwable cause) {
        this(null, translatableMessage, translationContextClass, translationContextMethod, cause);
    }

    private NonRecoverableException(
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

    // -- SHORTCUTS

    /**
     * <p><pre>
     * Path path = ...
     *
     * ## OLD
     *
     * try {
     *     Files.createDirectories(path);
     * } catch (IOException e) {
     *     throw new NonRecoverableException(e);
     * }
     *
     * ## NEW
     *
     * NonRecoverableException.tryRun(()->Files.createDirectories(path));
     *
     * </pre></p>
     *
     * @param checkedRunnable
     */
    public static void tryRun(_Functions.CheckedRunnable checkedRunnable) {
        try {
            checkedRunnable.run();
        } catch (Exception cause) {
            throw new NonRecoverableException(cause);
        }
    }


}
