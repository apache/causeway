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

package org.apache.isis.core.runtime.runner;

import java.io.File;
import java.util.Collections;
import java.util.List;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderDefault;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderPrimer;
import org.apache.isis.core.runtime.installerregistry.InstallerLookup;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;
import org.apache.isis.core.runtime.runner.opts.*;
import org.apache.isis.core.runtime.runner.opts.OptionValidator;
import org.apache.isis.core.runtime.system.DeploymentType;

public class IsisRunner {

    private static final Logger LOG = LoggerFactory.getLogger(IsisRunner.class);

    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();

    private final String[] args;
    private final OptionHandlerDeploymentType optionHandlerDeploymentType;
    private final InstallerLookup installerLookup;

    private final List<OptionHandler> optionHandlers = Lists.newArrayList();
    private final List<OptionValidator> validators = Lists.newArrayList();
    private IsisConfigurationBuilder isisConfigurationBuilder;

    private Injector globalInjector;;

    // ///////////////////////////////////////////////////////////////////////////////////////
    // Construction and adjustments
    // ///////////////////////////////////////////////////////////////////////////////////////

    public IsisRunner(final String[] args, final OptionHandlerDeploymentType optionHandlerDeploymentType) {
        this.args = args;
        this.optionHandlerDeploymentType = optionHandlerDeploymentType;

        // setup logging immediately
        loggingConfigurer.configureLogging(determineConfigDirectory(), args);
        this.installerLookup = new InstallerLookup();

        addStandardOptionHandlersAndValidators(this.installerLookup);
    }

    // REVIEW is this something that IsisConfigBuilder should know about?
    private String determineConfigDirectory() {
        if (new File(ConfigurationConstants.WEBINF_FULL_DIRECTORY).exists()) {
            return ConfigurationConstants.WEBINF_FULL_DIRECTORY;
        } else {
            return ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY;
        }
    }

    /**
     * Adds additional option handlers; may also require additional
     * {@link OptionValidator validator}s to be
     * {@link #addValidator(OptionValidator) add}ed.
     * <p>
     * An adjustment (as per GOOS book).
     */
    public final boolean addOptionHandler(final OptionHandler optionHandler) {
        return optionHandlers.add(optionHandler);
    }

    /**
     * Adds additional validators; typically goes hand-in-hand will calls to
     * {@link #addOptionHandler(OptionHandler)}.
     * <p>
     * An adjustment (as per GOOS book).
     */
    public void addValidator(final OptionValidator validator) {
        validators.add(validator);
    }

    /**
     * The default implementation is a {@link IsisConfigurationBuilderDefault},
     * which looks to the <tt>config/</tt> directory, the
     * <tt>src/main/webapp/WEB-INF</tt> directory, and then finally to the
     * classpath. However, this could be a security concern in a production
     * environment; a user could edit the <tt>isis.properties</tt> config files
     * to disable security, for example.
     * <p>
     * This method therefore allows this system to be configured using a
     * different {@link IsisConfigurationBuilder}. For example, a
     * security-conscious subclass could return a
     * {@link IsisConfigurationBuilder} that only reads from the classpath. This
     * would allow the application to be deployed as a single sealed JAR that
     * could not be tampered with.
     * <p>
     * An adjustment (as per GOOS book).
     */
    public void setConfigurationBuilder(final IsisConfigurationBuilder isisConfigurationBuilder) {
        this.isisConfigurationBuilder = isisConfigurationBuilder;
    }

    // ///////////////////////////////////////////////////////////////////////////////////////
    // parse and validate
    // ///////////////////////////////////////////////////////////////////////////////////////

    public final boolean parseAndValidate() {

        // add options (ie cmd line flags)
        final Options options = createOptions();

        // parse & validate options from the cmd line
        final BootPrinter printer = new BootPrinter(getClass());
        return parseOptions(options, printer) && validateOptions(options, printer);
    }

    private Options createOptions() {
        final Options options = new Options();
        for (final OptionHandler optionHandler : optionHandlers) {
            optionHandler.addOption(options);
        }
        return options;
    }

    private boolean parseOptions(final Options options, final BootPrinter printer) {
        final CommandLineParser parser = new BasicParser();
        try {
            final CommandLine commandLine = parser.parse(options, args);
            for (final OptionHandler optionHandler : optionHandlers) {
                if (!optionHandler.handle(commandLine, printer, options)) {
                    return false;
                }
            }
        } catch (final ParseException e) {
            printer.printErrorMessage(e.getMessage());
            printer.printHelp(options);
            return false;
        }
        return true;
    }

    private boolean validateOptions(final Options options, final BootPrinter printer) {
        final DeploymentType deploymentType = optionHandlerDeploymentType.getDeploymentType();

        for (final OptionValidator validator : validators) {
            final Optional<String> errorMessage = validator.validate(deploymentType);
            if (errorMessage.isPresent()) {
                printer.printErrorAndHelp(options, errorMessage.get());
                return false;
            }
        }
        return true;
    }

    public IsisConfigurationBuilder getStartupConfiguration() {
        return isisConfigurationBuilder;
    }

    public void primeConfigurationWithCommandLineOptions() {
        for (final IsisConfigurationBuilderPrimer isisConfigurationBuilderPrimer : optionHandlers) {
            LOG.debug("priming configurations for " + isisConfigurationBuilderPrimer);
            isisConfigurationBuilderPrimer.primeConfigurationBuilder(isisConfigurationBuilder);
        }
    }

    public void loadInitialProperties() {
        isisConfigurationBuilder.addDefaultConfigurationResources();
    }


    // ///////////////////////////////////////////////////////////////////////////////////////
    // Bootstrapping
    // ///////////////////////////////////////////////////////////////////////////////////////

    // TODO remove and use is desktop runner

    public final void bootstrap(final IsisBootstrapper bootstrapper) {

        final DeploymentType deploymentType = optionHandlerDeploymentType.getDeploymentType();

        this.globalInjector = createGuiceInjector(deploymentType, isisConfigurationBuilder, installerLookup, optionHandlers);

        bootstrapper.bootstrap(globalInjector);
        isisConfigurationBuilder.lockConfiguration();
        isisConfigurationBuilder.dumpResourcesToLog();
    }

    private Injector createGuiceInjector(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder, final InstallerLookup installerLookup, final List<OptionHandler> optionHandlers) {
        final IsisInjectModule isisModule = new IsisInjectModule(deploymentType, isisConfigurationBuilder, installerLookup);
        return Guice.createInjector(isisModule);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////
    // Handlers & Validators
    // ///////////////////////////////////////////////////////////////////////////////////////

    public final List<OptionHandler> getOptionHandlers() {
        return Collections.unmodifiableList(optionHandlers);
    }

    private void addStandardOptionHandlersAndValidators(final InstallerLookup installerLookup) {
        addOptionHandler(optionHandlerDeploymentType);
        addOptionHandler(new OptionHandlerConfiguration());

        addOptionHandler(new OptionHandlerPersistor(installerLookup));
        addOptionHandler(new OptionHandlerReflector(installerLookup));

        addOptionHandler(new OptionHandlerFixture());
        addOptionHandler(new OptionHandlerNoSplash());
        addOptionHandler(new OptionHandlerAdditionalProperty());
        addOptionHandler(new OptionHandlerFixtureFromEnvironmentVariable());
        addOptionHandler(new OptionHandlerSystemProperties());

        addOptionHandler(new OptionHandlerDebug());
        addOptionHandler(new OptionHandlerDiagnostics());
        addOptionHandler(new OptionHandlerQuiet());
        addOptionHandler(new OptionHandlerVerbose());

        addOptionHandler(new OptionHandlerHelp());
        addOptionHandler(new OptionHandlerVersion());
    }

}
