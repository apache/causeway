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

package org.apache.isis.runtimes.dflt.webserver;

import java.net.URI;
import java.util.Formatter;

import org.apache.isis.core.commons.lang.ArrayUtils;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.runtimes.dflt.runtime.runner.Constants;
import org.apache.isis.runtimes.dflt.runtime.runner.IsisRunner;
import org.apache.isis.runtimes.dflt.webserver.internal.OptionHandlerAddress;
import org.apache.isis.runtimes.dflt.webserver.internal.OptionHandlerDeploymentTypeWebServer;
import org.apache.isis.runtimes.dflt.webserver.internal.OptionHandlerPort;
import org.apache.isis.runtimes.dflt.webserver.internal.OptionHandlerResourceBase;
import org.apache.isis.runtimes.dflt.webserver.internal.OptionHandlerStartupMode;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class WebServer {

    public static enum StartupMode {
        FOREGROUND,
        BACKGROUND;

        public static StartupMode lookup(String value) {
            if(value == null) {
                return null;
            }
            try {
                return valueOf(value.toUpperCase());
            } catch (Exception e) {
                return null;
            }
        }

        public boolean isForeground() {
            return this == FOREGROUND;
        }

        public boolean isBackground() {
            return this == BACKGROUND;
        }

    }

    private Server jettyServer;

    public static void main(final String[] args) {
        new WebServer().run(ArrayUtils.append(args, "--" + Constants.NO_SPLASH_LONG_OPT));
    }

    /**
     * Originally introduced to allow the WebServer to be used by tests.
     */
    public void run(int port) {
        String[] args = new String[0];
        args = OptionHandlerStartupMode.appendArg(args,StartupMode.BACKGROUND);
        args = OptionHandlerPort.appendArg(args,port);
        run(args);
    }

    public void run(final String[] args) {
        final IsisRunner runner = new IsisRunner(args, new OptionHandlerDeploymentTypeWebServer());

        // adjustments
        runner.addOptionHandler(new OptionHandlerPort());
        runner.addOptionHandler(new OptionHandlerAddress());
        runner.addOptionHandler(new OptionHandlerResourceBase());
        runner.addOptionHandler(new OptionHandlerStartupMode());

        if (!runner.parseAndValidate()) {
            return;
        }

        WebServerBootstrapper bootstrapper = new WebServerBootstrapper(runner);
        runner.bootstrap(bootstrapper);
        jettyServer = bootstrapper.getJettyServer();
    }

    public void stop() {
        if(jettyServer == null) {
            return;
        }
        try {
            jettyServer.stop();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public URI getBase() {
        return URI.create(baseFor(jettyServer));
    }

    private String baseFor(Server jettyServer) {
        Connector connector = jettyServer.getConnectors()[0];
        String scheme = "http";
        String host = StringUtils.coalesce(connector.getHost(), "localhost");
        int port = connector.getPort();

        WebAppContext handler = (WebAppContext) jettyServer.getHandler();
        String contextPath = handler.getContextPath();

        StringBuilder buf = new StringBuilder();
        Formatter formatter = new Formatter(buf);
        formatter.format("%s://%s:%d/%s", scheme, host, port, contextPath);
        return appendSlashIfRequired(buf).toString();
    }

    private static StringBuilder appendSlashIfRequired(StringBuilder buf) {
        if(buf.charAt(buf.length()-1) != '/') {
            buf.append('/');
        }
        return buf;
    }
}
