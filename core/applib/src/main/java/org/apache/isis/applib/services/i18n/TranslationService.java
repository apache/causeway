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
import org.apache.isis.applib.annotation.Programmatic;

public interface TranslationService {

    /**
     * Return a translation of the text, for the specified locale.
     *
     * @param context
     * @param text
     * @param targetLocale
     * @return
     */
    @Programmatic
    public String translate(final String context, final String text, final Locale targetLocale);

    /**
     * Return a translation of either the singular or the plural text, dependent on the <tt>num</tt> parameter, for the specified locale.
     *
     * @param context
     * @param singularText
     * @param pluralText
     * @param num - whether to return the translation of the singular (if =1) or of the plural (if != 1)
     * @param targetLocale
     * @return
     */
    @Programmatic
    public String translate(final String context, final String singularText, final String pluralText, int num, final Locale targetLocale);

}
