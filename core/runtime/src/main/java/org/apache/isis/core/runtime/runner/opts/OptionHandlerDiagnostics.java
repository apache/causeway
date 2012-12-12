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

import static org.apache.isis.core.runtime.runner.Constants.DIAGNOSTICS_OPT;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;
import org.apache.isis.core.runtime.runner.Constants;

public class OptionHandlerDiagnostics extends OptionHandlerAbstract {

    public OptionHandlerDiagnostics() {
        super();
    }

    @Override
    public void addOption(final Options options) {
        options.addOption(DIAGNOSTICS_OPT, false, "print information that can be used diagnose or report problems");
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        if (commandLine.hasOption(Constants.DIAGNOSTICS_OPT)) {
            bootPrinter.printDiagnostics();
            return false;
        }
        return true;
    }

    @Override
    public void primeConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder) {
        // TODO need to do what, exactly?
    }

}
