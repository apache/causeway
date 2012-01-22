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

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.viewer.scimpi.dispatcher.Action;
import org.apache.isis.viewer.scimpi.dispatcher.NotLoggedInException;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;

public class LogAction implements Action {

    private static final Logger LOG = Logger.getLogger(LogAction.class);

    @Override
    public void process(final RequestContext context) throws IOException {

        final AuthenticationSession session = context.getSession();
        if (session == null) {
            throw new NotLoggedInException();
        }

        final String levelName = (String) context.getVariable("level");

        final Level level = Level.toLevel(levelName);
        boolean changeLogged = false;
        if (Level.INFO.isGreaterOrEqual(LogManager.getRootLogger().getLevel())) {
            LOG.info("log level changed to " + level);
            changeLogged = true;
        }
        LogManager.getRootLogger().setLevel(level);
        if (!changeLogged) {
            LOG.info("log level changed to " + level);
        }
        final String view = (String) context.getVariable("view");
        context.setRequestPath(view);

    }

    @Override
    public String getName() {
        return "log";
    }

    @Override
    public void init() {
    }

    @Override
    public void debug(final DebugBuilder debug) {
    }

}
