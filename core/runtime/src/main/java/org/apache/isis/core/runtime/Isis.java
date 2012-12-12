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

package org.apache.isis.core.runtime;

import org.apache.isis.core.commons.config.IsisConfigurationBuilderDefault;
import org.apache.isis.core.runtime.runner.IsisRunner;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerDeploymentTypeIsis;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerPassword;
import org.apache.isis.core.runtime.runner.opts.OptionHandlerUser;
import org.apache.isis.core.runtime.runner.opts.OptionValidatorUserAndPasswordCombo;
import org.apache.isis.core.runtime.system.SystemConstants;

public class Isis {

    static final String DEFAULT_EMBEDDED_WEBSERVER = SystemConstants.WEBSERVER_DEFAULT;

    public static void main(final String[] args) {
        new Isis().run(args);
    }

    private void run(final String[] args) {
        final IsisRunner runner = new IsisRunner(args, new OptionHandlerDeploymentTypeIsis());
        addOptionHandlersAndValidators(runner);
        if (!runner.parseAndValidate()) {
            return;
        }
        runner.setConfigurationBuilder(new IsisConfigurationBuilderDefault());
        runner.primeConfigurationWithCommandLineOptions();
        runner.loadInitialProperties();
        runner.bootstrap(new RuntimeBootstrapper());
    }

    private void addOptionHandlersAndValidators(final IsisRunner runner) {
        final OptionHandlerUser optionHandlerUser = new OptionHandlerUser();
        final OptionHandlerPassword optionHandlerPassword = new OptionHandlerPassword();

        runner.addOptionHandler(optionHandlerUser);
        runner.addOptionHandler(optionHandlerPassword);

        runner.addValidator(new OptionValidatorUserAndPasswordCombo(optionHandlerUser, optionHandlerPassword));
    }

}
