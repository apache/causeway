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
import org.h2.server.web.WebServer;
import org.h2.server.web.WebServlet;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._Reflect;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

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
            final @NonNull WebServlet webServlet,
            final @NonNull Consumer<H2WebServerWrapper> onConfiguration) {
        try {
            val serverWrapper = H2WebServerWrapper.wrap(webServlet);
            onConfiguration.accept(serverWrapper);
        } catch (Throwable cause) {
            // if for any reason wrapping fails, we fail hard to harden against potential security issues
            throw _Exceptions.unrecoverable(cause, "Unable to customize settings for H2 console");
        }
    }

    // -- HELPER

    @SneakyThrows
    private static H2WebServerWrapper wrap(final @NonNull WebServlet webServlet) {
        return new H2WebServerWrapper() {

            final WebServer webServer = (WebServer) _Reflect.getFieldOn(
                    WebServlet.class.getDeclaredField("server"),
                    webServlet);

            @SneakyThrows
            @Override
            public void setConnectionInfo(final ConnectionInfo connectionInfo) {
                val updateSettingMethod = WebServer.class.getDeclaredMethod("updateSetting",
                        ConnectionInfo.class);
                _Reflect.invokeMethodOn(updateSettingMethod, webServer, connectionInfo);
            }

            @SneakyThrows
            @Override
            public void setAllowOthers(boolean b) {
                val method = WebServer.class.getDeclaredMethod("setAllowOthers",
                        boolean.class);
                _Reflect.invokeMethodOn(method, webServer, b);

                // just so we verify reflection works
                _Assert.assertEquals(b, getAllowOthers());
            }

            @SneakyThrows
            @Override
            public boolean getAllowOthers() {
                val method = WebServer.class.getDeclaredMethod("getAllowOthers",
                        _Constants.emptyClasses);
                return (boolean)_Reflect.invokeMethodOn(method, webServer,
                        _Constants.emptyObjects)
                        .getValue().get();
            }

            @SneakyThrows
            @Override
            public void setAdminPassword(String password) {
                val method = WebServer.class.getDeclaredMethod("setAdminPassword",
                        String.class);
                _Reflect.invokeMethodOn(method, webServer, password);
            }

        };

    }


}
