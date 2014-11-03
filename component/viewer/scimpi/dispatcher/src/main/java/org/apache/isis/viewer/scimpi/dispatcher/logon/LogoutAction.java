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

package org.apache.isis.viewer.scimpi.dispatcher.logon;

import java.io.IOException;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.UserManager;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;

public class LogoutAction implements Action {

    public static void logoutUser(final RequestContext context) {
        if (context.isUserAuthenticated()) {
            final AuthenticationSession session = context.getSession();
            if (session != null) {
                UserManager.logoffUser(session);
            }
            context.endHttpSession();
            context.setUserAuthenticated(false);
        }
        context.clearVariables(Scope.SESSION);
    }

    @Override
    public String getName() {
        return "logout";
    }

    @Override
    public void init() {
    }

    @Override
    public void process(final RequestContext context) throws IOException {
        logoutUser(context);
        
        String view = context.getParameter("view");
        if (view == null) {
            view = context.getContextPath();
        }
        context.redirectTo(view);
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }

}
