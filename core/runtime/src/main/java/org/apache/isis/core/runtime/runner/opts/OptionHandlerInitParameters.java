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

package org.apache.isis.core.runtime.runner.opts;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;

public class OptionHandlerInitParameters extends OptionHandlerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(OptionHandlerInitParameters.class);
    private final ServletContext servletContext;



    public OptionHandlerInitParameters(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    @SuppressWarnings("static-access")
    public void addOption(final Options options) {
        // no-op
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        // no-op
        return true;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        Map<String,String> additionalProperties = asMap(servletContext);
        LOG.info("priming configuration builder: {} properties to prime", additionalProperties.size());
        addConfigurationProperties(isisConfigurationBuilder, additionalProperties);
    }

    private static Map<String, String> asMap(ServletContext servletContext) {
        Enumeration<String> initParameterNames = servletContext.getInitParameterNames();
        final Map<String,String> map = _Maps.newTreeMap();
        while(initParameterNames.hasMoreElements()) {
            final String initParameterName = initParameterNames.nextElement();
            final String initParameterValue = servletContext.getInitParameter(initParameterName);
            if (initParameterName.startsWith("isis.")) {
                map.put(initParameterName, initParameterValue);
            }
        }
        return map;
    }

    private static void addConfigurationProperties(final IsisConfigurationBuilder isisConfigurationBuilder, final Map<String, String> additionalProperties) {
        for (final String propertyKey : additionalProperties.keySet()) {
            final String propertyValue = additionalProperties.get(propertyKey);

            LOG.info("priming: {}={}", propertyKey, propertyValue);
            isisConfigurationBuilder.add(propertyKey, propertyValue);
        }
    }

}
