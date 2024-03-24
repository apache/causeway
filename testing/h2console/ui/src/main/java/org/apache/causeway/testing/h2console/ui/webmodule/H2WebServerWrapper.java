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
package org.apache.causeway.testing.h2console.ui.webmodule;

import java.util.function.Consumer;

import org.h2.server.web.ConnectionInfo;
import org.h2.server.web.H2WebServletForJakarta;
import org.h2.server.web.WebServer;

import lombok.NonNull;
import lombok.SneakyThrows;

/**
 * Provides programmatic access to otherwise protected H2 {@link WebServer} configuration.
 */
public interface H2WebServerWrapper {

    /**
     * Update the connection information setting.
     */
    void setConnectionInfo(ConnectionInfo connectionInfo);

    /**
     * Whether to allow other computers to connect.
     */
    void setAllowOthers(boolean b);

    /**
     * Whether other computers are allowed to connect.
     */
    boolean getAllowOthers();

    /**
     * Web Admin Password.
     */
    void setAdminPassword(String password);

    // -- UTILITY

    @SneakyThrows
    static void withH2WebServerWrapperDo(
            final @NonNull H2WebServletForJakarta webServlet,
            final @NonNull Consumer<H2WebServerWrapper> onConfiguration) {

        onConfiguration.accept(new H2WebServerWrapper() {

            @Override
            public void setConnectionInfo(ConnectionInfo connectionInfo) {
                webServlet.setConnectionInfo(connectionInfo);
            }

            @Override
            public void setAllowOthers(boolean b) {
                webServlet.setAllowOthers(b);
            }

            @Override
            public boolean getAllowOthers() {
                return webServlet.getAllowOthers();
            }

            @Override
            public void setAdminPassword(String password) {
                String encodedPassword = WebServer.encodeAdminPassword(password);
                webServlet.setAdminPassword(encodedPassword);
            }

        });
    }

}
