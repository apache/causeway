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

package org.apache.isis.runtimes.dflt.runtime.runner;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderDefault;
import org.apache.isis.core.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerAdditionalProperty;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerConfiguration;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerDebug;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerDeploymentType;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerDiagnostics;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerFixture;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerHelp;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerNoSplash;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerPersistor;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerQuiet;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerReflector;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerUserProfileStore;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerVerbose;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerVersion;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionHandlerViewer;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionValidator;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionValidatorForPersistor;
import org.apache.isis.runtimes.dflt.runtime.runner.opts.OptionValidatorForViewers;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class IsisRunner {

    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();

    private final String[] args;
    private final OptionHandlerDeploymentType optionHandlerDeploymentType;
    private final InstallerLookup installerLookup;

    private final OptionHandlerViewer optionHandlerViewer;

    private final List<OptionHandler> optionHandlers = Lists.newArrayList();
    private final List<OptionValidator> validators = Lists.newArrayList();
    private IsisConfigurationBuilder isisConfigurationBuilder = new IsisConfigurationBuilderDefault();

    private Injector globalInjector;;

    // ///////////////////////////////////////////////////////////////////////////////////////
    // Construction and adjustments
    // ///////////////////////////////////////////////////////////////////////////////////////

    public IsisRunner(final String[] args, final OptionHandlerDeploymentType optionHandlerDeploymentType) {

        this.args = args;
        this.optionHandlerDeploymentType = optionHandlerDeploymentType;

        // setup logging immediately
        loggingConfigurer.configureLogging(determineConfigDirectory(), args);

        this.installerLookup = new InstallerLookupDefault();

        addOptionHandler(optionHandlerDeploymentType);
        this.optionHandlerViewer = addStandardOptionHandlersAndValidators(this.installerLookup);
    }

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

    // ///////////////////////////////////////////////////////////////////////////////////////
    // Bootstrapping
    // ///////////////////////////////////////////////////////////////////////////////////////

    public final void bootstrap(final IsisBootstrapper bootstrapper) {

        final DeploymentType deploymentType = optionHandlerDeploymentType.getDeploymentType();

        this.globalInjector = createGuiceInjector(deploymentType, isisConfigurationBuilder, installerLookup, optionHandlers);

        bootstrapper.bootstrap(globalInjector);
    }

    private Injector createGuiceInjector(final DeploymentType deploymentType, final IsisConfigurationBuilder isisConfigurationBuilder, final InstallerLookup installerLookup, final List<OptionHandler> optionHandlers) {
        final IsisModule isisModule = new IsisModule(deploymentType, isisConfigurationBuilder, installerLookup);
        isisModule.addConfigurationPrimers(optionHandlers);
        isisModule.addViewerNames(optionHandlerViewer.getViewerNames());
        return Guice.createInjector(isisModule);
    }

    // ///////////////////////////////////////////////////////////////////////////////////////
    // Handlers & Validators
    // ///////////////////////////////////////////////////////////////////////////////////////

    public final List<OptionHandler> getOptionHandlers() {
        return Collections.unmodifiableList(optionHandlers);
    }

    private OptionHandlerViewer addStandardOptionHandlersAndValidators(final InstallerLookup installerLookup) {

        addOptionHandler(new OptionHandlerConfiguration());

        OptionHandlerPersistor optionHandlerPersistor;
        OptionHandlerViewer optionHandlerViewer;

        addOptionHandler(optionHandlerPersistor = new OptionHandlerPersistor(installerLookup));
        addOptionHandler(optionHandlerViewer = new OptionHandlerViewer(installerLookup));

        addOptionHandler(new OptionHandlerReflector(installerLookup));
        addOptionHandler(new OptionHandlerUserProfileStore(installerLookup));

        addOptionHandler(new OptionHandlerFixture());
        addOptionHandler(new OptionHandlerNoSplash());
        addOptionHandler(new OptionHandlerAdditionalProperty());

        addOptionHandler(new OptionHandlerDebug());
        addOptionHandler(new OptionHandlerDiagnostics());
        addOptionHandler(new OptionHandlerQuiet());
        addOptionHandler(new OptionHandlerVerbose());

        addOptionHandler(new OptionHandlerHelp());
        addOptionHandler(new OptionHandlerVersion());

        // validators
        addValidator(new OptionValidatorForViewers(optionHandlerViewer));
        addValidator(new OptionValidatorForPersistor(optionHandlerPersistor));

        return optionHandlerViewer;
    }

}
