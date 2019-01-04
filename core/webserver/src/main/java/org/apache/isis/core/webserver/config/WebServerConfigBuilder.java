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
package org.apache.isis.core.webserver.config;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.config.resource.ResourceStreamSource;
import org.apache.isis.config.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.config.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;

import static org.apache.isis.config.internal._Config.acceptBuilder;

public class WebServerConfigBuilder {
    
    public WebServerConfigBuilder() {
        acceptBuilder(builder->{
            builder.addResourceStreamSources(resourceStreamSources());    
        });
    }

    public boolean parseAndPrimeWith(final List<OptionHandler> optionHandlers, final String[] args) {

        // add options (ie cmd line flags)
        final Options options = new Options();
        for (final OptionHandler optionHandler : optionHandlers) {
            optionHandler.addOption(options);
        }

        // parse options from the cmd line
        final boolean parsedOk = parseAndPrimeWith(options, optionHandlers, args);

        if(parsedOk) {
            
            acceptBuilder(builder->{
                
                for (final OptionHandler optionHandler : optionHandlers) {
                    builder.primeWith(optionHandler);
                }
                
            });
            
        }

        return parsedOk;
    }

    private boolean parseAndPrimeWith(final Options options, final List<OptionHandler> optionHandlers, final String[] args) {
        final BootPrinter printer = new BootPrinter(getClass());
        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine commandLine = parser.parse(options, args);
            for (final OptionHandler optionHandler : optionHandlers) {
                if (!optionHandler.handle(commandLine, printer, options)) {
                    return false;
                }
            }
        } catch (final ParseException e) {
            printer.printErrorMessage(e.getMessage());
            printer.printHelp(options);
            return false;
        }
        return true;
    }
    
    /**
     * Set of locations to search for config files.
     */
    private static List<ResourceStreamSource> resourceStreamSources() {
        final List<ResourceStreamSource> rssList = _Lists.newArrayList();
        rssList.addAll(Arrays.asList(
                ResourceStreamSourceFileSystem.create(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY),
                ResourceStreamSourceFileSystem.create(ConfigurationConstants.WEBINF_FULL_DIRECTORY),
                ResourceStreamSourceContextLoaderClassPath.create(),
                ResourceStreamSourceContextLoaderClassPath.create(ConfigurationConstants.WEBINF_DIRECTORY)));
        return rssList;
    }

    public IsisConfiguration build() {
        return _Config.getConfiguration();
    }
    
}
