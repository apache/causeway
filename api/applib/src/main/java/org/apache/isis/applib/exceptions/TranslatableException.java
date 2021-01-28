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

/**
 * Exceptions that provide translations should implement this interface.
 *
 * <p>
 *     The {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer} will automatically
 *     detect and use the translation.  In addition, the integration testing support provides a
 *     rule (<code>ExceptionRecognizerTranslations</code>) that will exercise any thrown exceptions,
 *     ensuring tha the message is captured in the <code>translations.po</code> file so that it can be translated.
 * </p>
 * @since 1.x {@index}
 */
public interface TranslatableException {

    /**
     * In the spirit of {@link Exception#getLocalizedMessage()}, but using {@link org.apache.isis.applib.services.i18n.TranslatableString} instead.
     *
     * <p>
     *     If returns <code>null</code>, then {@link Exception#getMessage()} will be used as a fallback.
     *     This design allows the Isis-provided {@link org.apache.isis.applib.exceptions.RecoverableException} and
     *     {@link org.apache.isis.applib.exceptions.UnrecoverableException} to provide constructors that
     *     accept a {@link org.apache.isis.applib.services.i18n.TranslatableString}, but can be left as null for any existing code.
     * </p>
     */
    TranslatableString getTranslatableMessage();

    /**
     * The context to use when translating the {@link #getTranslatableMessage() translatable message},
     * as ultimately passed through to {@link org.apache.isis.applib.services.i18n.TranslationService#translate(String, String)}.
     */
    String getTranslationContext();

}
