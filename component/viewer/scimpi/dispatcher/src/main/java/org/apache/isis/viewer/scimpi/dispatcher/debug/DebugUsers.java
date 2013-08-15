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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;

public class DebugUsers {

    private static Logger LOG = LoggerFactory.getLogger(DebugUsers.class);

    private enum DebugMode {
        OFF, ON, NAMED, SYSADMIN_ONLY
    }

    private static List<String> debugUsers = new ArrayList<String>();
    private static DebugMode debugMode;

    public void initialize() {
        if (debugMode != null) {
            throw new ScimpiException("Debug mode is already set up!");
        }

        final String debugUserEntry = IsisContext.getConfiguration().getString(ConfigurationConstants.ROOT + "scimpi.debug.users", "");
        final String[] users = debugUserEntry.split("\\|");
        for (final String name : users) {
            debugUsers.add(name.trim());
        }

        final String debugModeEntry = IsisContext.getConfiguration().getString(ConfigurationConstants.ROOT + "scimpi.debug.mode");
        if (debugModeEntry != null) {
            try {
                debugMode = DebugMode.valueOf(debugModeEntry.toUpperCase());
                LOG.info("Debug mode set to " + debugMode);
            } catch (final IllegalArgumentException e) {
                LOG.error("Invalid debug mode - " + debugModeEntry + " - mode set to OFF");
                debugMode = DebugMode.OFF;
            }
        } else {
            debugMode = DebugMode.OFF;
        }
    }

    public boolean isDebugEnabled(final AuthenticationSession session) {
        if (debugMode == DebugMode.ON) {
            return true;
        } else if (session != null && debugMode == DebugMode.SYSADMIN_ONLY && session.getRoles().contains("sysadmin")) {
            return true;
        } else if (session != null && debugMode == DebugMode.NAMED && (debugUsers.contains(session.getUserName()) || session.getRoles().contains("sysadmin"))) {
            return true;
        }
        return false;
    }

    public List<String> getNames() {
        final ArrayList<String> users = new ArrayList<String>(debugUsers);
        Collections.sort(users);
        return users;
    }

    public void add(final String name) {
        if (!debugUsers.contains(name)) {
            debugUsers.add(name);
            LOG.info("Added '" + debugMode + "' to debug users list");
        }
    }

    public void remove(final String name) {
        debugUsers.remove(name);
        LOG.info("Removed '" + debugMode + "' from debug users list");
    }
}
