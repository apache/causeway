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


package org.apache.isis.runtime.options.standard;

import static org.apache.isis.runtime.runner.Constants.NO_SPLASH_LONG_OPT;
import static org.apache.isis.runtime.runner.Constants.NO_SPLASH_OPT;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.isis.core.metamodel.config.ConfigurationBuilder;
import org.apache.isis.runtime.runner.BootPrinter;
import org.apache.isis.runtime.runner.options.OptionHandlerAbstract;
import org.apache.isis.runtime.system.SystemConstants;


public class OptionHandlerNoSplash extends OptionHandlerAbstract {

    private boolean noSplash;

    public OptionHandlerNoSplash() {
        super();
    }

    public void addOption(Options options) {
        options.addOption(NO_SPLASH_OPT, NO_SPLASH_LONG_OPT, false, "don't show splash window");
    }

    public boolean handle(CommandLine commandLine, BootPrinter bootPrinter, Options options) {
        noSplash = commandLine.hasOption(NO_SPLASH_OPT);
        return true;
    }

    public void primeConfigurationBuilder(ConfigurationBuilder configurationBuilder) {
        if (noSplash) {
            configurationBuilder.add(SystemConstants.NOSPLASH_KEY, "true");
        }
 //       configurationBuilder.add(SystemConstants.NOSPLASH_KEY, noSplash ? "true" : "false");
    }

}
