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

import static org.apache.isis.core.runtime.runner.Constants.NO_SPLASH_LONG_OPT;
import static org.apache.isis.core.runtime.runner.Constants.NO_SPLASH_OPT;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;
import org.apache.isis.core.runtime.system.SystemConstants;

public class OptionHandlerNoSplash extends OptionHandlerAbstract {

    private boolean noSplash;

    public OptionHandlerNoSplash() {
        super();
    }

    @Override
    public void addOption(final Options options) {
        options.addOption(NO_SPLASH_OPT, NO_SPLASH_LONG_OPT, false, "don't show splash window");
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        noSplash = commandLine.hasOption(NO_SPLASH_OPT);
        return true;
    }

    @Override
    public void primeConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder) {
        if (noSplash) {
            isisConfigurationBuilder.add(SystemConstants.NOSPLASH_KEY, "true");
        }
        // configurationBuilder.add(SystemConstants.NOSPLASH_KEY, noSplash ?
        // "true" : "false");
    }

}
