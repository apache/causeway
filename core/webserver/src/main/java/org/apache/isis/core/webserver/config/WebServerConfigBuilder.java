package org.apache.isis.core.webserver.config;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.commons.resource.ResourceStreamSourceContextLoaderClassPath;
import org.apache.isis.core.commons.resource.ResourceStreamSourceFileSystem;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandler;

public class WebServerConfigBuilder {
    
    private final IsisConfigurationBuilder isisConfigurationBuilder = new IsisConfigurationBuilder();
    
    public WebServerConfigBuilder() {
        isisConfigurationBuilder.addResourceStreamSources(resourceStreamSources());
    }

    public boolean parseAndPrimeWith(final List<OptionHandler> optionHandlers, final String[] args) {

        // add options (ie cmd line flags)
        final Options options = new Options();
        for (final OptionHandler optionHandler : optionHandlers) {
            optionHandler.addOption(options);
        }

        // parse options from the cmd line
        final boolean parsedOk = parseAndPrimeWith(options, optionHandlers, args);

        if(parsedOk) {
            for (final OptionHandler optionHandler : optionHandlers) {
                isisConfigurationBuilder.primeWith(optionHandler);
            }
        }

        return parsedOk;
    }

    private boolean parseAndPrimeWith(final Options options, final List<OptionHandler> optionHandlers, final String[] args) {
        final BootPrinter printer = new BootPrinter(getClass());
        final CommandLineParser parser = new DefaultParser();
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
    
    /**
     * Set of locations to search for config files.
     */
    private static List<ResourceStreamSource> resourceStreamSources() {
        final List<ResourceStreamSource> rssList = _Lists.newArrayList();
        rssList.addAll(Arrays.asList(
                ResourceStreamSourceFileSystem.create(ConfigurationConstants.DEFAULT_CONFIG_DIRECTORY),
                ResourceStreamSourceFileSystem.create(ConfigurationConstants.WEBINF_FULL_DIRECTORY),
                ResourceStreamSourceContextLoaderClassPath.create(),
                ResourceStreamSourceContextLoaderClassPath.create(ConfigurationConstants.WEBINF_DIRECTORY)));
        return rssList;
    }

    public IsisConfiguration build() {
        return isisConfigurationBuilder.build();
    }
    
}
