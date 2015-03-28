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

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

/**
 * Displays the localization data for the current user.
 */
public class Localization extends AbstractElementProcessor {

    @Override
    public void process(final Request request) {
        request.closeEmpty();

        org.apache.isis.applib.profiles.Localization localization = IsisContext.getLocalization();
        if (localization != null) {
            Locale locale = localization.getLocale();
            String country = locale.getDisplayName();
         //   country = locale.toString();
            String timeZone = localization.getTimeZone().getDisplayName();
            
            request.appendAsHtmlEncoded(country + ", " + timeZone);
        } else {
            request.appendAsHtmlEncoded("No localization data!");
        }
        
        Locale locale = Locale.getDefault();
        String country = locale.getDisplayName();
      //  country = locale.toString();
        String timeZone = TimeZone.getDefault().getDisplayName();
        
        request.appendAsHtmlEncoded("\n (Default " + country + ", " + timeZone +")");

    }

    @Override
    public String getName() {
        return "alpha-localization";
    }

}
