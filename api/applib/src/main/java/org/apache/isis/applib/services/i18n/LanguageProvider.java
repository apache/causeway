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
package org.apache.isis.applib.services.i18n;

import java.util.Locale;
import java.util.Optional;

/**
 * Provides the preferred language {@link Locale} of the current user.
 * <p>
 * One of a number of services that work together to provide support for i18n.
 *
 * @see TranslationService
 * @see TranslationsResolver
 *
 * @since 2.x {@index}
 */
public interface LanguageProvider {

    /**
     * Optionally returns the preferred language {@link Locale} of the current user,
     * based on whether there is a context with a current user object.
     */
    Optional<Locale> getPreferredLanguage();

}
