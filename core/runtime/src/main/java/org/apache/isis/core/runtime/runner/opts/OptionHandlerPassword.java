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

import static org.apache.isis.core.runtime.runner.Constants.PASSWORD_LONG_OPT;
import static org.apache.isis.core.runtime.runner.Constants.PASSWORD_OPT;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.isis.config.builder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;
import org.apache.isis.core.runtime.runner.Constants;
import org.apache.isis.core.runtime.system.SystemConstants;

public class OptionHandlerPassword extends OptionHandlerAbstract {

    private String password;

    public OptionHandlerPassword() {
        super();
    }

    @Override
    @SuppressWarnings("static-access")
    public void addOption(final Options options) {
        final Option option = OptionBuilder.withArgName("password").hasArg().withLongOpt(PASSWORD_LONG_OPT).withDescription("password to automatically log in with").create(PASSWORD_OPT);
        options.addOption(option);
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        password = commandLine.getOptionValue(Constants.PASSWORD_OPT);
        return true;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        isisConfigurationBuilder.add(SystemConstants.PASSWORD_KEY, password);
    }

    public String getPassword() {
        return password;
    }

}
