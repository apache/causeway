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

import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.builder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;

public class OptionHandlerSystemProperties extends OptionHandlerAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(OptionHandlerSystemProperties.class);

    private Map<String,String> additionalProperties;

    @Override
    public void addOption(final Options options) {
        // no-op
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        this.additionalProperties = asMap(System.getProperties());
        return true;
    }

    private static Map<String, String> asMap(Properties properties) {
        final Map<String,String> map = _Maps.newTreeMap();
        for (String key : properties.stringPropertyNames()) {
            final String value = properties.getProperty(key);
            if (key.startsWith("isis.")) {
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        LOG.debug("priming configuration builder: {} properties to prime", additionalProperties.size());
        addConfigurationProperties(isisConfigurationBuilder, additionalProperties);
    }

    private static void addConfigurationProperties(final IsisConfigurationBuilder isisConfigurationBuilder, final Map<String, String> additionalProperties) {
        for (final String propertyKey : additionalProperties.keySet()) {
            final String propertyValue = additionalProperties.get(propertyKey);

            LOG.info("priming: {}={}", propertyKey, propertyValue);
            isisConfigurationBuilder.add(propertyKey, propertyValue);
        }
    }

}
