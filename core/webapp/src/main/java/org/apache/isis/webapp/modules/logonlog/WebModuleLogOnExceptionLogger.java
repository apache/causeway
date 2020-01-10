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
package org.apache.isis.webapp.modules.logonlog;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.webapp.diagnostics.IsisLogOnExceptionFilter;
import org.apache.isis.webapp.modules.WebModuleAbstract;

import lombok.Getter;

/**
 * WebModule to log log-on exceptions.
 * 
 * @since 2.0
 */
@Service
@Named("isisWebapp.WebModuleLogOnExceptionLogger")
@Order(OrderPrecedence.HIGH - 100)
@Qualifier("LogOnExceptionLogger")
public final class WebModuleLogOnExceptionLogger extends WebModuleAbstract {

    private final static String LOGONLOGGER_FILTER_NAME = "IsisLogOnExceptionFilter";


    @Getter
    private final String name = "LogOn Exception Logger";

    @Inject
    public WebModuleLogOnExceptionLogger(final ServiceInjector serviceInjector) {
        super(serviceInjector);
    }


    @Override
    public List<ServletContextListener> init(ServletContext ctx) throws ServletException {

        registerFilter(ctx, LOGONLOGGER_FILTER_NAME, IsisLogOnExceptionFilter.class)
            .ifPresent(filterReg -> {
                filterReg.addMappingForUrlPatterns(
                        null,
                        true, // filterReg is forced last
                        webModuleContext.getProtectedPaths().toArray(String.class));
            });

        return null; // does not provide a listener
    }


    // -- HELPER


}
