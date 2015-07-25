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
package org.apache.isis.viewer.restfulobjects.tck;

import org.apache.http.client.HttpClient;
import org.jboss.resteasy.client.ClientExecutor;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;

public class IsisWebServerRule implements MethodRule {

    private static ThreadLocal<WebServer> WEBSERVER = new ThreadLocal<WebServer>();

    private RestfulClient client;

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        getWebServer(); // creates and starts running if required
        return base;
    }

    public WebServer getWebServer() {
        WebServer webServer = WEBSERVER.get();
        if(webServer == null) {
            webServer = new WebServer();
            WEBSERVER.set(webServer);
            webServer.run(39393);
        }
        return webServer;
    }
    
    public void discardWebApp() {
        getWebServer().stop();
        WEBSERVER.set(null);
        IsisContext.testReset();
    }

    public RestfulClient getClient() {
        if(client == null) {
            final WebServer webServer = getWebServer();
            client = new RestfulClient(webServer.getBase());
        }
        return client;
    }
    
    public RestfulClient getClient(HttpClient httpClient) {
        if(client == null) {
            final WebServer webServer = getWebServer();
            client = new RestfulClient(webServer.getBase(), httpClient);
        }
        return client;
    }
    
    public RestfulClient getClient(ClientExecutor clientExecutor) {
        if(client == null) {
            final WebServer webServer = getWebServer();
            client = new RestfulClient(webServer.getBase(), clientExecutor);
        }
        return client;
    }
    
}
