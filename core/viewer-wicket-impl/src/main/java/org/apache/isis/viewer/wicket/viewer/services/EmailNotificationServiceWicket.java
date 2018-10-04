/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.viewer.services;

import org.apache.isis.applib.services.userreg.EmailNotificationService;
import org.apache.isis.applib.services.userreg.events.EmailRegistrationEvent;
import org.apache.isis.applib.services.userreg.events.PasswordResetEvent;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.runtime.system.context.IsisContext;

@com.google.inject.Singleton // necessary because is registered in and injected by guice
public class EmailNotificationServiceWicket implements EmailNotificationService {

    private static final long serialVersionUID = 1L;
    
    @Override
    public void init() {
        // delegate is a managed object, hence the framework takes care of initializing it
    }

    @Override
    public boolean send(EmailRegistrationEvent ev) {
        return delegate.get().send(ev);
    }

    @Override
    public boolean send(PasswordResetEvent ev) {
        return delegate.get().send(ev);
    }

    @Override
    public boolean isConfigured() {
        return delegate.get().isConfigured();
    }
    
    // -- HELPER
    
    private final transient _Lazy<EmailNotificationService> delegate = _Lazy.of(this::loadDelegate);   
    
    private EmailNotificationService loadDelegate() {
        return IsisContext.getServicesInjector().lookupServiceElseFail(EmailNotificationService.class);
    }

}
