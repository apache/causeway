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

package org.apache.isis.viewer.scimpi.dispatcher.debug;

import java.io.IOException;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.ForbiddenException;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;

public class DebugUserAction implements Action {

    private final DebugUsers debugUsers;

    public DebugUserAction(final DebugUsers debugUsers) {
        this.debugUsers = debugUsers;
    }

    @Override
    public String getName() {
        return "debug-user";
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }

    @Override
    public void process(final RequestContext context) throws IOException {
        if (context.isDebugDisabled()) {
            throw new ForbiddenException("Can't access debug action when debug is disabled");
        }

        final String method = context.getParameter(METHOD);
        final String name = context.getParameter(NAME);
        final String view = context.getParameter(VIEW);

        if (method != null && method.equals("add")) {
            debugUsers.add(name);
        } else if (method != null && method.equals("remove")) {
            debugUsers.remove(name);
        } else {
            throw new ScimpiException("Invalid debug-user action");
        }

        context.setRequestPath(view);
    }

    @Override
    public void init() {
    }
}
