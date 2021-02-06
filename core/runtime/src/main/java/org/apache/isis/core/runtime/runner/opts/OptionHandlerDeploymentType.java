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

import static org.apache.isis.core.runtime.runner.Constants.TYPE_LONG_OPT;
import static org.apache.isis.core.runtime.runner.Constants.TYPE_OPT;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.config.NotFoundPolicy;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.runtime.optionhandler.BootPrinter;
import org.apache.isis.core.runtime.optionhandler.OptionHandlerAbstract;
import org.apache.isis.core.runtime.runner.Constants;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.SystemConstants;

public abstract class OptionHandlerDeploymentType extends OptionHandlerAbstract {

    private final DeploymentType defaultDeploymentType;
    private final String types;

    private DeploymentType deploymentType;

    public OptionHandlerDeploymentType(final DeploymentType defaultDeploymentType, final String types) {
        this.defaultDeploymentType = defaultDeploymentType;
        this.types = types;
    }

    @Override
    public void addOption(final Options options) {
    	
    	Option option = Option.builder(TYPE_OPT)
    	.argName("name")
    	.hasArg()
    	.longOpt(TYPE_LONG_OPT)
    	.desc("deployment type: " + types)
    	.build();

        options.addOption(option);
    }

    @Override
    public boolean handle(final CommandLine commandLine, final BootPrinter bootPrinter, final Options options) {
        final String deploymentTypeName = commandLine.getOptionValue(Constants.TYPE_OPT);

        if (deploymentTypeName == null) {
            deploymentType = defaultDeploymentType;
            return true;
        }

        deploymentType = DeploymentType.lookup(deploymentTypeName.toUpperCase());
        if (deploymentType != null) {
            return true;
        }
        bootPrinter.printErrorAndHelp(options, "Unable to determine deployment type");
        return false;
    }

    /**
     * Only populated after {@link #handle(CommandLine, BootPrinter, Options)}.
     */
    public DeploymentType getDeploymentType() {
        return deploymentType;
    }

    @Override
    public void prime(final IsisConfigurationBuilder isisConfigurationBuilder) {
        final String type = deploymentType.nameLowerCase();
        isisConfigurationBuilder.addConfigurationResource(type + ".properties", NotFoundPolicy.CONTINUE, IsisConfigurationDefault.ContainsPolicy.IGNORE);

        isisConfigurationBuilder.add(SystemConstants.DEPLOYMENT_TYPE_KEY, deploymentType.name());
    }

}
