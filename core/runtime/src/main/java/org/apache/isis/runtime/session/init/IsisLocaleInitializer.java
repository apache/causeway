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

package org.apache.isis.runtime.session.init;

import java.util.Locale;
import java.util.Optional;

import org.apache.isis.config.IsisConfiguration;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class IsisLocaleInitializer {

    public void initLocale(final IsisConfiguration configuration) {
        final Optional<String> localeSpecOpt = configuration.getLocale();
        localeSpecOpt.map(IsisLocaleInitializer::toLocale).ifPresent(IsisLocaleInitializer::setLocaleDefault);
        log.debug("locale is {}", Locale.getDefault());
    }

    private static Locale toLocale(String localeSpec) {
        final int pos = localeSpec.indexOf('_');
        Locale locale;
        if (pos == -1) {
            locale = new Locale(localeSpec, "");
        } else {
            final String language = localeSpec.substring(0, pos);
            final String country = localeSpec.substring(pos + 1);
            locale = new Locale(language, country);
        }
        return locale;
    }

    private static void setLocaleDefault(Locale locale) {
        Locale.setDefault(locale);
        log.info("locale set to {}", locale);
    }

}
