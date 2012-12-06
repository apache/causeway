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

package org.apache.isis.runtimes.dflt.runtime.runner.opts;

import static org.apache.isis.runtimes.dflt.runtime.runner.Constants.VIEWER_LONG_OPT;
import static org.apache.isis.runtimes.dflt.runtime.runner.Constants.VIEWER_OPT;

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.lang.ListUtils;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerRepository;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.IsisViewerInstaller;
import org.apache.isis.runtimes.dflt.runtime.runner.Constants;
import org.apache.isis.runtimes.dflt.runtime.system.SystemConstants;

public class OptionHandlerViewer extends OptionHandlerAbstract {

    private final InstallerRepository installerRepository;
    private List<String> viewerNames;

    public OptionHandlerViewer(final InstallerRepository installerRepository) {
        this.installerRepository = installerRepository;
    }

    @Override
    @SuppressWarnings("static-access")
    public void addOption(final Options options) {
        final Object[] viewers = installerRepository.getInstallers(IsisViewerInstaller.class);
        final Option option = OptionBuilder.withArgName("name|class name").hasArg().withLongOpt(VIEWER_LONG_OPT).withDescription("viewer to use, or for server to listen on: " + availableInstallers(viewers) + "; or class name").create(VIEWER_OPT);
        options.addOption(option);

    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        viewerNames = getOptionValues(commandLine, Constants.VIEWER_OPT);
        return true;
    }

    @Override
    public void primeConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder) {
        isisConfigurationBuilder.add(SystemConstants.VIEWER_KEY, ListUtils.listToString(viewerNames));
    }

    public List<String> getViewerNames() {
        return viewerNames;
    }

}
