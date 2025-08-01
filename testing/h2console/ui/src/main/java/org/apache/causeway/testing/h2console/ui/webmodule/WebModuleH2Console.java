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

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;

import org.h2.server.web.ConnectionInfo;
import org.h2.server.web.H2WebServletForJakarta;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService;
import org.apache.causeway.core.config.datasources.DataSourceIntrospectionService.DataSourceInfo;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.security.authentication.standard.RandomCodeGenerator;
import org.apache.causeway.core.webapp.modules.WebModuleAbstract;
import org.apache.causeway.core.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.test.WebModuleH2Console")
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("H2Console")
@Slf4j
public class WebModuleH2Console extends WebModuleAbstract {

    private static final String SERVLET_NAME = "H2Console";
    private static final String CONSOLE_PATH = "/db";

    @Getter
    private final LocalResourcePath localResourcePathIfEnabled;

    private final CausewaySystemEnvironment causewaySystemEnvironment;

    private final boolean applicable;

    @Inject
    public WebModuleH2Console(
            final DataSourceIntrospectionService datasourceIntrospector,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final ServiceInjector serviceInjector) {

        super(serviceInjector);
        this.causewaySystemEnvironment = causewaySystemEnvironment;

        this.applicable = isPrototyping()
                && isH2MemConnectionUsed(datasourceIntrospector);
        this.localResourcePathIfEnabled = applicable
                ? new LocalResourcePath(CONSOLE_PATH)
                : null;
    }

    @Getter
    private final String name = "H2Console";

    @Override
    public Can<ServletContextListener> init(final ServletContext ctx) throws ServletException {

        registerServlet(ctx, SERVLET_NAME, H2WebServlet.class)
            .ifPresent(servletReg -> {
                servletReg.addMapping(CONSOLE_PATH + "/*");

                //[CAUSEWAY-3128] presence of "webAllowOthers" is a potential security risk
                // setting this later based on configuration below ...
                //servletReg.setInitParameter("webAllowOthers", "true");

            });

        return Can.empty(); // registers no listeners
    }

    @Override
    public boolean isApplicable(final WebModuleContext ctx) {
        return applicable;
    }

    // -- WRAPPER AROUND H2'S SERVLET

    public static class H2WebServlet extends H2WebServletForJakarta {

        private static final long serialVersionUID = 1L;

        private static String jdbcUrl;

        @Inject private CausewayConfiguration causewayConfiguration;
        @Inject private RandomCodeGenerator randomCodeGenerator;

        @Override
        public void init() {
            super.init();

            if(_Strings.isEmpty(jdbcUrl)) {
                return;
            }

            var dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUsername("sa");
            dataSourceProperties.setUrl(jdbcUrl);

            var connectionInfo = new ConnectionInfo(
                    String.format("Generic Spring Datasource|%s|%s|%s",
                            dataSourceProperties.determineDriverClassName(),
                            dataSourceProperties.determineUrl(),
                            dataSourceProperties.determineUsername()));

            var webServlet = this;

            H2WebServerWrapper.withH2WebServerWrapperDo(webServlet, h2WebServerWrapper->{
                h2WebServerWrapper.setConnectionInfo(connectionInfo);
                h2WebServerWrapper.setAllowOthers(isWebAllowRemoteAccess());
                if(isGenerateRandomWebAdminPassword()) {
                    var webAdminPass = randomCodeGenerator.generateRandomCode(20); // h2 requires at least 12
                    log.info("webAdminPass: {}", webAdminPass);
                    h2WebServerWrapper.setAdminPassword(webAdminPass);
                }
            });

        }

        public static void configure(final String jdbcUrl) {
            H2WebServlet.jdbcUrl = jdbcUrl;
        }

        private boolean isWebAllowRemoteAccess() {
            return causewayConfiguration.prototyping().h2Console().webAllowRemoteAccess();
        }

        private boolean isGenerateRandomWebAdminPassword() {
            return causewayConfiguration.prototyping().h2Console().generateRandomWebAdminPassword();
        }

    }

    // -- HELPER

    private boolean isPrototyping() {
        return causewaySystemEnvironment.deploymentType().isPrototyping();
    }

    private boolean isH2MemConnectionUsed(final DataSourceIntrospectionService datasourceIntrospector) {

        return datasourceIntrospector.getDataSourceInfos()
        .stream()
        .map(DataSourceInfo::jdbcUrl)
        .anyMatch(jdbcUrl->{
            if(jdbcUrl.contains(":h2:mem:")) {
                log.info("found h2 in-memory data-source: {}", jdbcUrl);
                H2WebServlet.configure(jdbcUrl);
                return true;
            }
            return false;
        });
    }

}
