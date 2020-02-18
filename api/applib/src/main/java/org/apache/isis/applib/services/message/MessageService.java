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

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslatableString;

// tag::refguide[]
public interface MessageService {

    /**
     * Make the specified message available to the user. Note this will probably
     * be displayed in transitory fashion, so is only suitable for useful but
     * optional information.
     *
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #warnUser(String)
     * @see #raiseError(String)
     */
    void informUser(String message);

    /**
     * Make the specified message available to the user, translated (if possible) to user's locale.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LocaleProvider} service.  This will most commonly be the
     *     locale of the current request (ie the current user's locale).
     * </p>
     *
     * @see #informUser(java.lang.String)
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    String informUser(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    /**
     * Override of {@link MessageService#informUser(TranslatableString, Class, String)}, but with last two parameters combined into a context string.
     */
    String informUser(TranslatableString message, final String translationContext);

    /**
     * Warn the user about a situation with the specified message. The container
     * should guarantee to display this warning to the user, and will typically
     * require acknowledgement.
     *
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(String)
     * @see #informUser(String)
     */
    void warnUser(String message);

    /**
     * Warn the user about a situation with the specified message, translated (if possible) to user's locale.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LocaleProvider} service.  This will most commonly be the
     *     locale of the current request (ie the current user's locale).
     * </p>
     *
     * @see #warnUser(String)
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    String warnUser(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    /**
     * Override of {@link MessageService#warnUser(TranslatableString, Class, String)}, but with last two parameters combined into a context string.
     */
    String warnUser(TranslatableString message, final String translationContext);

    /**
     * Notify the user of an application error with the specified message. Note
     * this will probably be displayed in an alarming fashion, so is only
     * suitable for errors. The user will typically be required to perform
     * additional steps after the error (eg to inform the helpdesk).
     *
     * @see #warnUser(String)
     * @see #informUser(String)
     */
    void raiseError(String message);

    /**
     * Notify the user of an application error with the specified message, translated (if possible) to user's locale.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LocaleProvider} service.  This will most commonly be the
     *     locale of the current request (ie the current user's locale).
     * </p>
     *
     * @see #raiseError(String)
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    String raiseError(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    /**
     * Override of {@link MessageService#raiseError(TranslatableString, Class, String)}, but with last two parameters combined into a context string.
     */
    String raiseError(TranslatableString message, final String translationContext);

}
// end::refguide[]
