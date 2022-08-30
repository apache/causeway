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
package org.apache.isis.testing.h2console.ui.webmodule;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.h2.server.web.ConnectionInfo;
import org.h2.server.web.WebServlet;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.datasources.DataSourceIntrospectionService;
import org.apache.isis.core.config.datasources.DataSourceIntrospectionService.DataSourceInfo;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.security.authentication.standard.RandomCodeGenerator;
import org.apache.isis.core.webapp.modules.WebModuleAbstract;
import org.apache.isis.core.webapp.modules.WebModuleContext;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.test.WebModuleH2Console")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("H2Console")
@Log4j2
public class WebModuleH2Console extends WebModuleAbstract {

    private static final String SERVLET_NAME = "H2Console";
    private static final String CONSOLE_PATH = "/db";

    @Getter
    private final LocalResourcePath localResourcePathIfEnabled;

    private final IsisSystemEnvironment isisSystemEnvironment;

    private final boolean applicable;

    @Inject
    public WebModuleH2Console(
            final DataSourceIntrospectionService datasourceIntrospector,
            final IsisSystemEnvironment isisSystemEnvironment,
            final ServiceInjector serviceInjector) {

        super(serviceInjector);
        this.isisSystemEnvironment = isisSystemEnvironment;

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

                //[ISIS-3128] presence of "webAllowOthers" is a potential security risk
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

    public static class H2WebServlet extends WebServlet {

        private static final long serialVersionUID = 1L;

        private static String jdbcUrl;

        @Inject private IsisConfiguration isisConfiguration;
        @Inject private RandomCodeGenerator randomCodeGenerator;

        @Override
        public void init() {
            super.init();

            if(_Strings.isEmpty(jdbcUrl)) {
                return;
            }

            val dataSourceProperties = new DataSourceProperties();
            dataSourceProperties.setUsername("sa");
            dataSourceProperties.setUrl(jdbcUrl);

            val connectionInfo = new ConnectionInfo(
                    String.format("Generic Spring Datasource|%s|%s|%s",
                            dataSourceProperties.determineDriverClassName(),
                            dataSourceProperties.determineUrl(),
                            dataSourceProperties.determineUsername()));

            val webServlet = this;

            H2WebServerWrapper.withH2WebServerWrapperDo(webServlet, h2WebServerWrapper->{
                h2WebServerWrapper.setConnectionInfo(connectionInfo);
                h2WebServerWrapper.setAllowOthers(isWebAllowRemoteAccess());
                if(isGenerateRandomWebAdminPassword()) {
                    val webAdminPass = randomCodeGenerator.generateRandomCode();
                    log.info("webAdminPass: {}", webAdminPass);
                    h2WebServerWrapper.setAdminPassword(webAdminPass);
                }
            });

        }

        public static void configure(final String jdbcUrl) {
            H2WebServlet.jdbcUrl = jdbcUrl;
        }

        private boolean isWebAllowRemoteAccess() {
            return isisConfiguration.getPrototyping().getH2Console().isWebAllowRemoteAccess();
        }

        private boolean isGenerateRandomWebAdminPassword() {
            return isisConfiguration.getPrototyping().getH2Console().isGenerateRandomWebAdminPassword();
        }

    }

    // -- HELPER

    private boolean isPrototyping() {
        return isisSystemEnvironment.getDeploymentType().isPrototyping();
    }

    private boolean isH2MemConnectionUsed(final DataSourceIntrospectionService datasourceIntrospector) {

        return datasourceIntrospector.getDataSourceInfos()
        .stream()
        .map(DataSourceInfo::getJdbcUrl)
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
