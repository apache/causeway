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

package org.apache.isis.core.webserver.internal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.isis.config.builder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;
import org.apache.isis.core.webserver.WebServer.StartupMode;
import org.apache.isis.core.webserver.WebServerConstants;

public final class OptionHandlerStartupMode implements OptionHandler {

    static final String STARTUP_MODE_LONG_OPT = "startup";
    static final String STARTUP_MODE_BASE_OPT = "a";

    public static String[] appendArg(final String[] args, final StartupMode startupMode) {
        return ArrayExtensions.append(args, "--" + STARTUP_MODE_LONG_OPT, "" + startupMode.name());
    }

    private StartupMode startupMode;

    @Override
    @SuppressWarnings("static-access")
    public void addOption(final Options options) {
        final Option option = OptionBuilder.withArgName("startup mode").hasArg().withLongOpt(OptionHandlerStartupMode.STARTUP_MODE_LONG_OPT).withDescription("start in foreground (sync) or background (async)").create(OptionHandlerStartupMode.STARTUP_MODE_BASE_OPT);
        options.addOption(option);
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        startupMode = StartupMode.lookup(commandLine.getOptionValue(OptionHandlerStartupMode.STARTUP_MODE_BASE_OPT));
        return true;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        if (startupMode != null) {
            isisConfigurationBuilder.add(WebServerConstants.EMBEDDED_WEB_SERVER_STARTUP_MODE_KEY, startupMode.name());
        }
    }

}