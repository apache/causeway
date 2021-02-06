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

/**
 * 
 */
package org.apache.isis.core.webserver.internal;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;
import org.apache.isis.core.webserver.WebServerConstants;

public final class OptionHandlerPort implements OptionHandler {
    private Integer port;
    static final String PORT_LONG_OPT = "port";
    static final String PORT_OPT = "p";

    public static String[] appendArg(final String[] args, final int port) {
        return ArrayExtensions.append(args, "--" + PORT_LONG_OPT, "" + port);
    }

    @Override
    public void addOption(final Options options) {
    	
    	Option option = Option.builder(OptionHandlerPort.PORT_OPT)
    	.argName("port")
    	.hasArg()
    	.longOpt(OptionHandlerPort.PORT_LONG_OPT)
    	.desc("port to listen on")
    	.build();
    	
        options.addOption(option);
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        final String portStr = commandLine.getOptionValue(OptionHandlerPort.PORT_OPT);
        if (portStr != null) {
            port = Integer.parseInt(portStr);
        }
        return true;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        if (port == null) {
            return;
        }
        isisConfigurationBuilder.add(WebServerConstants.EMBEDDED_WEB_SERVER_PORT_KEY, "" + port);
    }
}