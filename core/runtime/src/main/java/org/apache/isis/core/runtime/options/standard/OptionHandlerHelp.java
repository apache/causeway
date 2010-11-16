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


package org.apache.isis.core.runtime.options.standard;

import static org.apache.isis.core.runtime.runner.Constants.HELP_LONG_OPT;
import static org.apache.isis.core.runtime.runner.Constants.HELP_OPT;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.isis.core.metamodel.config.ConfigurationBuilder;
import org.apache.isis.core.runtime.runner.BootPrinter;
import org.apache.isis.core.runtime.runner.Constants;
import org.apache.isis.core.runtime.runner.options.OptionHandlerAbstract;

public class OptionHandlerHelp extends OptionHandlerAbstract {

	public OptionHandlerHelp() {
		super();
	}

	public void addOption(Options options) {
		options.addOption(HELP_OPT, HELP_LONG_OPT, false, "show this help");
		
	}

	public boolean handle(CommandLine commandLine, BootPrinter bootPrinter, Options options) {
        if (commandLine.hasOption(Constants.HELP_OPT)) {
        	bootPrinter.printHelp(options);
            return false;
        }
        return true;
	}
	
	public void primeConfigurationBuilder(
			ConfigurationBuilder configurationBuilder) {
		// nothing to do
		
	}


}
