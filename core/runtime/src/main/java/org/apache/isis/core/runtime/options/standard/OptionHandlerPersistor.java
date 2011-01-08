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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.installers.InstallerRepository;
import org.apache.isis.core.runtime.persistence.PersistenceMechanismInstaller;
import org.apache.isis.core.runtime.runner.BootPrinter;
import org.apache.isis.core.runtime.runner.Constants;
import org.apache.isis.core.runtime.runner.options.OptionHandlerAbstract;
import org.apache.isis.core.runtime.system.SystemConstants;

import static org.apache.isis.core.runtime.runner.Constants.OBJECT_PERSISTENCE_LONG_OPT;
import static org.apache.isis.core.runtime.runner.Constants.OBJECT_PERSISTENCE_OPT;

public class OptionHandlerPersistor extends OptionHandlerAbstract {

	private InstallerRepository installerRepository;
	private String persistorName;

	public OptionHandlerPersistor(final InstallerRepository installerRepository) {
		this.installerRepository = installerRepository;
	}

	@SuppressWarnings("static-access")
	public void addOption(Options options) {
        Object[] objectPersistenceMechanisms = installerRepository.getInstallers(PersistenceMechanismInstaller.class);
        Option option = OptionBuilder.withArgName("name|class name").hasArg().withLongOpt(OBJECT_PERSISTENCE_LONG_OPT).withDescription(
                "object persistence mechanism to use (ignored if type is prototype or client): " + availableInstallers(objectPersistenceMechanisms)
                        + "; or class name").create(OBJECT_PERSISTENCE_OPT);
        options.addOption(option);
	}

	public boolean handle(CommandLine commandLine, BootPrinter bootPrinter, Options options) {
		persistorName = commandLine.getOptionValue(Constants.OBJECT_PERSISTENCE_OPT);		
		return true;
	}
	
	public void primeConfigurationBuilder(
			IsisConfigurationBuilder isisConfigurationBuilder) {
		isisConfigurationBuilder.add(SystemConstants.OBJECT_PERSISTOR_INSTALLER_KEY, persistorName);
	}

	
	public String getPersistorName() {
		return persistorName;
	}

}
