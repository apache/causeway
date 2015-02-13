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

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import org.apache.isis.applib.annotation.Programmatic;

public interface TranslationService {

    @Programmatic
    public String translate(final String context, final String originalText, final Locale targetLocale);

    /**
     * Returns the set of messages encountered and cached by the service (the key of the map) along with a set of
     * context strings (the value of the map)
     *
     * <p>
     *     The intention is that an implementation running in prototype mode should retain all requests to
     *     {@link #translate(String, String, java.util.Locale)}, such that they can be translated and used by the
     *     same implementation in non-prototype mode.
     * </p>
     */
    @Programmatic
    public Map<String, Collection<String>> messages();

}
