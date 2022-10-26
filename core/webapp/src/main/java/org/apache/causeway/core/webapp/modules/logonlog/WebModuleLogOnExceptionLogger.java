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
package org.apache.causeway.core.webapp.modules.logonlog;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.webapp.modules.WebModuleAbstract;

import lombok.Getter;

/**
 * WebModule to log log-on exceptions.
 *
 * @since 2.0
 */
@Service
@Named("causeway.webapp.WebModuleLogOnExceptionLogger")
@javax.annotation.Priority(PriorityPrecedence.EARLY - 100)
@Qualifier("LogOnExceptionLogger")
public final class WebModuleLogOnExceptionLogger extends WebModuleAbstract {

    private static final String LOGONLOGGER_FILTER_NAME = "CausewayLogOnExceptionFilter";


    @Getter
    private final String name = "LogOn Exception Logger";

    @Inject
    public WebModuleLogOnExceptionLogger(final ServiceInjector serviceInjector) {
        super(serviceInjector);
    }


    @Override
    public Can<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerFilter(ctx, LOGONLOGGER_FILTER_NAME, CausewayLogOnExceptionFilter.class)
            .ifPresent(filterReg -> {
                filterReg.addMappingForUrlPatterns(
                        null,
                        true, // filterReg is forced last
                        webModuleContext.getProtectedPaths().toArray(String.class));
            });

        return Can.empty(); // registers no listeners
    }


    // -- HELPER


}
