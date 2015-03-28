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

package org.apache.isis.viewer.scimpi.dispatcher.view.simple;

import java.util.Locale;
import java.util.TimeZone;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

/**
 * Displays the localization data for the current user.
 */
public class SetLocalization extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        request.closeEmpty();
        
        final String localeCode = request.getRequiredProperty("locale");
        final String timeZone = request.getRequiredProperty("time-zone");
        
        String country;
        String language;
        int pos = localeCode.indexOf('_');
        if (pos == 1) {
            language = localeCode;
            country = "";
        } else {
            language = localeCode.substring(0, pos);
            country = localeCode.substring(pos + 1);
        }
        
        Locale l = new Locale(language, country);
        TimeZone t = TimeZone.getTimeZone(timeZone);
        // IsisContext.getUserProfile().setLocalization(new UserLocalization(l, t));

    }

    @Override
    public String getName() {
        return "alpha-set-localization";
    }

}
