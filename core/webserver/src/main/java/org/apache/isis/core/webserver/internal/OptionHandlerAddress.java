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
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;
import org.apache.isis.core.webserver.WebServerConstants;

/**
 * @deprecated - never completed (unused)
 */
@Deprecated
public final class OptionHandlerAddress implements OptionHandler {
    private String address;
    static final String ADDRESS_OPT = "a";
    static final String ADDRESS_LONG_OPT = "address";

    @Override
    public void addOption(final Options options) {
    	Option option = Option.builder(OptionHandlerAddress.ADDRESS_OPT)
    	.argName("address")
    	.hasArg()
    	.longOpt(OptionHandlerAddress.ADDRESS_LONG_OPT)
    	.desc("address to listen on")
    	.build();
    	
        options.addOption(option);
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        address = commandLine.getOptionValue(OptionHandlerAddress.ADDRESS_OPT);
        return true;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        if (address == null) {
            return;
        }
        isisConfigurationBuilder.add(WebServerConstants.EMBEDDED_WEB_SERVER_ADDRESS_KEY, address);
    }
}