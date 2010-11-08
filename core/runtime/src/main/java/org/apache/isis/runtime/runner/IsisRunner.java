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


package org.apache.isis.runtime.runner;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.isis.core.commons.lang.Maybe;
import org.apache.isis.metamodel.config.ConfigurationBuilder;
import org.apache.isis.metamodel.config.ConfigurationBuilderDefault;
import org.apache.isis.metamodel.config.ConfigurationConstants;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.installers.InstallerLookupDefault;
import org.apache.isis.runtime.logging.IsisLoggingConfigurer;
import org.apache.isis.runtime.options.standard.OptionHandlerAdditionalProperty;
import org.apache.isis.runtime.options.standard.OptionHandlerConfiguration;
import org.apache.isis.runtime.options.standard.OptionHandlerConnector;
import org.apache.isis.runtime.options.standard.OptionHandlerDebug;
import org.apache.isis.runtime.options.standard.OptionHandlerDeploymentType;
import org.apache.isis.runtime.options.standard.OptionHandlerDiagnostics;
import org.apache.isis.runtime.options.standard.OptionHandlerFixture;
import org.apache.isis.runtime.options.standard.OptionHandlerHelp;
import org.apache.isis.runtime.options.standard.OptionHandlerNoSplash;
import org.apache.isis.runtime.options.standard.OptionHandlerPersistor;
import org.apache.isis.runtime.options.standard.OptionHandlerQuiet;
import org.apache.isis.runtime.options.standard.OptionHandlerReflector;
import org.apache.isis.runtime.options.standard.OptionHandlerUserProfileStore;
import org.apache.isis.runtime.options.standard.OptionHandlerVerbose;
import org.apache.isis.runtime.options.standard.OptionHandlerVersion;
import org.apache.isis.runtime.options.standard.OptionHandlerViewer;
import org.apache.isis.runtime.runner.options.OptionHandler;
import org.apache.isis.runtime.runner.options.OptionValidator;
import org.apache.isis.runtime.runner.options.OptionValidatorForConnector;
import org.apache.isis.runtime.runner.options.OptionValidatorForPersistor;
import org.apache.isis.runtime.runner.options.OptionValidatorForViewers;
import org.apache.isis.runtime.system.DeploymentType;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.internal.Lists;

public class IsisRunner {

    private final IsisLoggingConfigurer loggingConfigurer = new IsisLoggingConfigurer();
    
    private final String[] args;
    private final OptionHandlerDeploymentType optionHandlerDeploymentType;
    private final InstallerLookup installerLookup;
    
    private OptionHandlerViewer optionHandlerViewer;

    private final List<OptionHandler> optionHandlers = Lists.newArrayList();
    private final List<OptionValidator> validators = Lists.newArrayList();
    private ConfigurationBuilder configurationBuilder = new ConfigurationBuilderDefault();

    private Injector globalInjector;
;


    // ///////////////////////////////////////////////////////////////////////////////////////
    // Construction and adjustments
    // ///////////////////////////////////////////////////////////////////////////////////////

    public IsisRunner(final String[] args,
            OptionHandlerDeploymentType optionHandlerDeploymentType) {

        this.args = args;
        this.optionHandlerDeploymentType = optionHandlerDeploymentType;

        // setup logging immediately
        loggingConfigurer.configureLogging(determineConfigDirectory(),
        args);

        this.installerLookup = new InstallerLookupDefault(
                IsisRunner.class);
        
        addOptionHandler(optionHandlerDeploymentType);
        this.optionHandlerViewer = addStandardOptionHandlersAndValidators(this.installerLookup);
    }

    private String determineConfigDirectory() {
        if (new File(ConfigurationConstants.WEBINF_CONFIG_DIRECTORY).exists()) {
            return ConfigurationConstants.WEBINF_CONFIG_DIRECTORY;
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
    public final boolean addOptionHandler(OptionHandler optionHandler) {
        return optionHandlers.add(optionHandler);
    }


    /**
     * Adds additional validators; typically goes hand-in-hand will calls to
     * {@link #addOptionHandler(OptionHandler)}.
     * <p>
     * An adjustment (as per GOOS book).
     */
    public void addValidator(OptionValidator validator) {
        validators.add(validator);
    }

    /**
     * The default implementation is a {@link ConfigurationBuilderDefault},
     * which looks to the <tt>config/</tt> directory, the
     * <tt>src/main/webapp/WEB-INF</tt> directory, and then finally to the
     * classpath. However, this could be a security concern in a production
     * environment; a user could edit the <tt>isis.properties</tt>
     * config files to disable security, for example.
     * <p>
     * This method therefore allows this system to be configured using a
     * different {@link ConfigurationBuilder}. For example, a security-conscious
     * subclass could return a {@link ConfigurationBuilder} that only reads from
     * the classpath. This would allow the application to be deployed as a
     * single sealed JAR that could not be tampered with.
     * <p>
     * An adjustment (as per GOOS book).
     */
    public void setConfigurationBuilder(
            ConfigurationBuilder configurationBuilder) {
        this.configurationBuilder = configurationBuilder;
    }


    // ///////////////////////////////////////////////////////////////////////////////////////
    // parse and validate
    // ///////////////////////////////////////////////////////////////////////////////////////

    public final boolean parseAndValidate() {

        // add options (ie cmd line flags)
        final Options options = createOptions();

        // parse & validate options from the cmd line
        BootPrinter printer = new BootPrinter(getClass());
        return parseOptions(options, printer) &&
                validateOptions(options, printer);
    }

    private Options createOptions() {
        final Options options = new Options();
        for (OptionHandler optionHandler : optionHandlers) {
            optionHandler.addOption(options);
        }
        return options;
    }

    private boolean parseOptions(final Options options, BootPrinter printer) {
        final CommandLineParser parser = new BasicParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            for (OptionHandler optionHandler : optionHandlers) {
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

    private boolean validateOptions(final Options options, BootPrinter printer) {
        final DeploymentType deploymentType = optionHandlerDeploymentType
                .getDeploymentType();

        for (OptionValidator validator : validators) {
            Maybe<String> errorMessage = validator.validate(deploymentType);
            if (errorMessage.isSet()) {
                printer.printErrorAndHelp(
                        options,
                        errorMessage.get());
                return false;
            }
        }
        return true;
    }


    // ///////////////////////////////////////////////////////////////////////////////////////
    // Bootstrapping
    // ///////////////////////////////////////////////////////////////////////////////////////

    public final void bootstrap(IsisBootstrapper bootstrapper) {

        final DeploymentType deploymentType = optionHandlerDeploymentType
                .getDeploymentType();
        
        this.globalInjector = createGuiceInjector(deploymentType, configurationBuilder, installerLookup, optionHandlers);

        bootstrapper.bootstrap(globalInjector);
    }

    private Injector createGuiceInjector(DeploymentType deploymentType,
            ConfigurationBuilder configurationBuilder,
            InstallerLookup installerLookup,
            List<OptionHandler> optionHandlers) {
        IsisModule isisModule = new IsisModule(deploymentType,configurationBuilder, installerLookup);
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


    private OptionHandlerViewer addStandardOptionHandlersAndValidators(
            InstallerLookup installerLookup) {

        addOptionHandler(new OptionHandlerConfiguration());

        OptionHandlerConnector optionHandlerClientConnection;
        OptionHandlerPersistor optionHandlerPersistor;
        OptionHandlerViewer optionHandlerViewer;
        
        addOptionHandler(optionHandlerClientConnection = new OptionHandlerConnector(
                installerLookup));
        addOptionHandler(optionHandlerPersistor = new OptionHandlerPersistor(
                installerLookup));
        addOptionHandler(optionHandlerViewer = new OptionHandlerViewer(
                installerLookup));

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
        addValidator(new OptionValidatorForConnector(optionHandlerClientConnection));
        addValidator(new OptionValidatorForPersistor(optionHandlerPersistor));
        
        return optionHandlerViewer;
    }


}
