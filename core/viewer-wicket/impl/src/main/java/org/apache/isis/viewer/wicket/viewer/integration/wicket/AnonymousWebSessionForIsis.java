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

package org.apache.isis.viewer.wicket.viewer.integration.wicket;

import org.apache.wicket.Session;
import org.apache.wicket.request.Request;

import org.apache.isis.core.runtime.authentication.AuthenticationManager;

public class AnonymousWebSessionForIsis extends AuthenticatedWebSessionForIsis {

    private static final long serialVersionUID = 1L;

    public static AnonymousWebSessionForIsis get() {
        return (AnonymousWebSessionForIsis) Session.get();
    }

    private final AuthenticationManager authenticationManager;

    public AnonymousWebSessionForIsis(final Request request, final AuthenticationManager authenticationManager) {
        super(request);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
}
