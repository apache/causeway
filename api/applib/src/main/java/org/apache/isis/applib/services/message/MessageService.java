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
package org.apache.isis.applib.services.message;

import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationContext;

/**
 * Allows domain objects to raise information, warning or error messages.
 *
 * <p>
 * These messages can either be simple strings, or can be translated.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface MessageService {


    /**
     * Make the specified message available to the user, intended to be useful
     * but optional information, for a viewer to display typically in a
     * transitory manner.
     *
     * <p>
     *     In the Wicket viewer this is implemented as a &quot;toast&quot;
     *     message that automatically disappears after a period of time.
     * </p>
     *
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #warnUser(String)
     * @see #raiseError(String)
     */
    void informUser(String message);

    /**
     * As {@link #informUser(String)}, but with the message translated (if
     * possible) to user's {@link java.util.Locale}.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LanguageProvider} service.
     *     This should be the {@link java.util.Locale} of the user making the
     *     current request.
     * </p>
     *
     * @see #informUser(java.lang.String)
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     *
     */
    String informUser(
            TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod);

    /**
     * Override of
     * {@link MessageService#informUser(TranslatableString, Class, String)},
     * but with an arbitrary translation context (rather than inferred from the
     * context class and method).
     */
    String informUser(
            TranslatableString message,
            final TranslationContext translationContext);

    /**
     * Warn the user about a situation with the specified message.
     *
     * <p>
     * The viewer should guarantee to display this warning to the user, and
     * will typically require acknowledgement.
     * </p>
     *
     * <p>
     *     In the Wicket viewer this is implemented as a &quot;toast&quot;
     *     message that must be explicitly closed by the user.
     * </p>
     *
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(String)
     * @see #informUser(String)
     */
    void warnUser(String message);

    /**
     * As {@link #warnUser(String)}, but with the message translated (if
     *  possible) to user's {@link java.util.Locale}.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LanguageProvider} service.
     *     This should be the {@link java.util.Locale} of the user making the
     *     current request.
     * </p>
     *
     * @see #warnUser(String)
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    String warnUser(
            TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod);

    /**
     * Override of
     * {@link MessageService#warnUser(TranslatableString, Class, String)},
     * but with an arbitrary translation context (rather than inferred from the
     * context class and method).
     */
    String warnUser(
            TranslatableString message,
            final TranslationContext translationContext);

    /**
     * Notify the user of an application error with the specified message.
     *
     * <p>
     * Note this will probably be displayed in an prominent fashion, so is only
     * suitable for errors. The user will typically be required to perform
     * additional steps after the error..
     * </p>
     *
     * <p>
     *     In the Wicket viewer this is implemented as a toast (with a
     *     different colour) that must be closed by the end-user.
     * </p>
     *
     * @see #warnUser(String)
     * @see #informUser(String)
     */
    void raiseError(String message);

    /**
     * As {@link #raiseError(String)}, but with the message translated (if
     * possible) to user's {@link java.util.Locale}.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LanguageProvider} service.
     *     This should be the {@link java.util.Locale} of the user making the
     *     current request.
     * </p>
     *
     * @see #raiseError(String)
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    String raiseError(
            TranslatableString message,
            final Class<?> contextClass,
            final String contextMethod);

    /**
     * Override of
     * {@link MessageService#raiseError(TranslatableString, Class, String)},
     * but with an arbitrary translation context (rather than inferred from the
     * context class and method).
     */
    String raiseError(
            TranslatableString message,
            final TranslationContext translationContext);

}
