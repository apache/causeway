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
package org.apache.isis.viewer.wicket.viewer.services;

import java.util.Locale;
import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.LocaleProvider;


/**
 * An implementation that provides the locale of the current session.
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
public class LocaleProviderWicket implements LocaleProvider {

    public static Logger LOG = LoggerFactory.getLogger(LocaleProviderWicket.class);

    @Programmatic
    @Override
    public Locale getLocale() {
        if(!Application.exists()) {
            // eg if request from RO viewer
            return null;
        }
        return RequestCycle.get().getRequest().getLocale();
    }

    protected Session getSession() {
        return Session.get();
    }
}
