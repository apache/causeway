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

package org.apache.isis.metamodel.commons;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.TimeZone;

public class PrintLocale {
    public static void main(final String[] args) {
        if (args.length >= 2) {
            final String localeSetting = args[0];
            System.out.println("Setting Locale to " + localeSetting + "\n");
            Locale.setDefault(new Locale(localeSetting));

            final String timezoneSetting = args[1];
            System.out.println("Setting TimeZone to " + timezoneSetting + "\n");
            TimeZone.setDefault(TimeZone.getTimeZone(timezoneSetting));
        }

        final Locale locale = Locale.getDefault();
        System.out.println("Locale");
        System.out.println("Code: " + locale.toString());
        try {
            System.out.println("Country: " + locale.getISO3Country());
        } catch (final MissingResourceException e) {
            System.out.println("Country: " + e.getMessage());
        }
        try {
            System.out.println("Language: " + locale.getISO3Language());
        } catch (final MissingResourceException e) {
            System.out.println("Language: " + e.getMessage());
        }

        System.out.println("\nTimezone");
        final TimeZone timezone = TimeZone.getDefault();
        System.out.println("Code: " + timezone.getID());
        System.out.println("Name: " + timezone.getDisplayName());
        System.out.println("Offset: " + timezone.getRawOffset() / (1000 * 60 * 60));
        System.out.println("DST: " + timezone.getDSTSavings() / (1000 * 60 * 60));
    }
}
