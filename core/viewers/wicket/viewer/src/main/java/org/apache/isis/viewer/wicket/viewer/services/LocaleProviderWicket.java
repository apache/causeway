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

import javax.inject.Named;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.request.cycle.RequestCycle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.i18n.LocaleProvider;


/**
 * An implementation that provides the locale of the current session.
 */
@Service
@Named("isisWicketViewer.LocaleProviderWicket")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Wicket")
public class LocaleProviderWicket implements LocaleProvider {

    @Override
    public Locale getLocale() {
        // Request Cycle can be null, e.g. during the start of an application
        RequestCycle requestCycle = RequestCycle.get();

        if (!Application.exists() || requestCycle == null) {
            // eg if request from RO viewer
            return null;
        }
        return RequestCycle.get().getRequest().getLocale();
    }

    protected Session getSession() {
        return Session.get();
    }
}
