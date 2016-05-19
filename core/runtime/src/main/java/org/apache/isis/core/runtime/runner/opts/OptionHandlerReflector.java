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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.metamodel.specloader.SpecificationLoaderInstaller;
import org.apache.isis.core.runtime.installerregistry.InstallerRepository;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;
import org.apache.isis.core.runtime.runner.Constants;
import org.apache.isis.core.runtime.system.SystemConstants;

import static org.apache.isis.core.runtime.runner.Constants.REFLECTOR_LONG_OPT;
import static org.apache.isis.core.runtime.runner.Constants.REFLECTOR_OPT;

/**
 * @deprecated - far better to use `isis.reflector.facets.include` and `isis.reflector.facets.exclude`; no longer registered in IsisRunner/IsisWebServer
 */
@Deprecated
public class OptionHandlerReflector extends OptionHandlerAbstract {

    private final InstallerRepository installerRepository;
    private String reflector;

    public OptionHandlerReflector(final InstallerRepository installerRepository) {
        this.installerRepository = installerRepository;
    }

    @Override
    @SuppressWarnings("static-access")
    public void addOption(final Options options) {
        final Object[] reflectors = installerRepository.getInstallers(SpecificationLoaderInstaller.class);
        final Option option = OptionBuilder.withArgName("name|class name").hasArg().withLongOpt(REFLECTOR_LONG_OPT).withDescription("reflector to use (ignored if type is prototype or client): " + availableInstallers(reflectors) + "; or class name").create(REFLECTOR_OPT);
        options.addOption(option);

    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        reflector = commandLine.getOptionValue(Constants.REFLECTOR_OPT);
        return true;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        isisConfigurationBuilder.add(SystemConstants.REFLECTOR_KEY, reflector);
    }

}
