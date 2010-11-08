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


package org.apache.isis.extensions.html.action;

import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.extensions.html.component.Page;
import org.apache.isis.extensions.html.context.Context;
import org.apache.isis.extensions.html.request.Request;
import org.apache.isis.runtime.authentication.AuthenticationManager;
import org.apache.isis.runtime.context.IsisContext;



public class LogOut implements Action {
    public void execute(final Request request, final Context context, final Page page) {
    	AuthenticationSession authSession = IsisContext.getAuthenticationSession();
    	if (authSession != null) {
    		getAuthenticationManager().closeSession(authSession);
    	}
        context.setSession(null); // setSession is probably redundant since now always available via IsisContext
                                  // can't rely on it being set because Filter may set httpSession 
                                  // (if in exploration mode) rather than ever hitting the LogonServlet 
        context.invalidate();
    }

	private static AuthenticationManager getAuthenticationManager() {
		return IsisContext.getAuthenticationManager();
	}

    public String name() {
        return "logout";
    }
}

