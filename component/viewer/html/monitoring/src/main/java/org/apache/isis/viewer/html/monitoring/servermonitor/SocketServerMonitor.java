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

package org.apache.isis.viewer.html.monitoring.servermonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;

import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.runtime.system.IsisSystem;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class SocketServerMonitor extends AbstractServerMonitor {
    private static final int DEFAULT_PORT = 8009;
    private static final String PORT = ConfigurationConstants.ROOT + "monitor.telnet.port";

    private final MonitorListenerImpl monitor = new MonitorListenerImpl();
    private IsisSystem system;

    @Override
    protected int getPort() {
        return IsisContext.getConfiguration().getInteger(PORT, DEFAULT_PORT);
    }

    @Override
    protected boolean handleRequest(final PrintWriter writer, final String request) throws IOException {
        final String query = URLDecoder.decode(request, "UTF-8");

        if (query.equalsIgnoreCase("bye")) {
            writer.println("Disconnecting...");
            return false;
        } else if (query.equalsIgnoreCase("shutdown")) {
            writer.println("Shutting down system...");
            system.shutdown();
            exitSystem();
            return false;
        }

        monitor.writeTextPage(query, writer);
        writer.print("shutdown bye]\n#");
        writer.flush();
        return true;
    }

    @SuppressWarnings(value = "DM_EXIT")
    private void exitSystem() {
        System.exit(0);
    }

    @Override
    public void setTarget(final IsisSystem system) {
        this.system = system;
    }
}
