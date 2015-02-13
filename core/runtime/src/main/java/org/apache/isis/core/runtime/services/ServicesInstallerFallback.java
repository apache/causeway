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

package org.apache.isis.core.runtime.services;

import java.util.List;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.InstallerAbstract;
import org.apache.isis.core.metamodel.services.i18n.LocaleProviderDefault;
import org.apache.isis.core.metamodel.services.i18n.TranslationServiceLogging;
import org.apache.isis.core.runtime.system.DeploymentType;

public class ServicesInstallerFallback extends InstallerAbstract implements ServicesInstaller {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInstallerFallback.class);

    public ServicesInstallerFallback() {
        super(ServicesInstaller.TYPE, "fallback");
    }

    @Override
    public List<Object> getServices(final DeploymentType deploymentType) {
        return Lists.newArrayList(new TranslationServiceLogging(), new LocaleProviderDefault());
    }

    @Override
    public void setIgnoreFailures(final boolean ignoreFailures) {
    }

    @Override
    public List<Class<?>> getTypes() {
        return listOf(List.class); // ie List<Object.class>, of services
    }
}
