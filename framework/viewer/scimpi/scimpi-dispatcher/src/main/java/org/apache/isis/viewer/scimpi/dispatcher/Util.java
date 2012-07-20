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

package org.apache.isis.viewer.scimpi.dispatcher;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

class Util {
    
    public static final String DEFAULT_TIME_ZONE = "Europe/London";
    public static final String DEFAULT_LANGUAGE = "English, United Kingdom (en-gb)";
    

    private Util() {}

    public static boolean hasChanged(String version1, String version2) {
        return version2 == null && version1 != null || (version2 != null && !version2.equals(version1));
    }

    public static List<String> languages() {
        Locale[] locales = DateFormat.getAvailableLocales();
        List<String> list = new ArrayList<String>(locales.length);
        for (Locale locale : locales) {
            list.add(localeName(locale));
        }
        Collections.sort(list);
        return list;
    }
    
    public static List<String> timeZones() {
        List<String> timezones = Arrays.asList(TimeZone.getAvailableIDs());
        Collections.sort(timezones);
        return timezones;
    }

    public static TimeZone timeZone(String timeZoneEntry) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneEntry);
        return timeZone;
    }

    public static Locale locale(String localeCode) {
        String substring[] = localeCode.trim().split("-");
        Locale locale;
        switch (substring.length) {
        case 1:
            locale = new Locale(substring[0]);                    
            break;
        case 2:
            locale = new Locale(substring[0], substring[1]);                    
            break;
        case 3:
            locale = new Locale(substring[0], substring[1], substring[3]);                    
            break;
        default:
            locale = Locale.getDefault();
            break;
        }
        return locale;
    }

    public static String languageName(String languageCode) {
        Locale locale = locale(languageCode);
        return localeName(locale);
    }

    public static String codeForLanguage(String language) {
        Locale[] locales = DateFormat.getAvailableLocales();
        for (Locale locale : locales) {
            String name = localeName(locale);
            if (name.equals(language)) {
                return locale.toString().toLowerCase().replace('_', '-');
            }
        }
        return null;
    }

    public static String localeName(Locale locale) {
        String language = locale.getDisplayLanguage();
        String country = locale.getDisplayCountry().length() == 0 ? "" :  ", " + (locale.getDisplayCountry());
        return language + country + " (" +  locale.toString().toLowerCase().replace('_', '-') + ")";
    }
   
}