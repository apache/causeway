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

package org.apache.isis.runtimes.dflt.runtime.viewer;

import org.apache.isis.core.commons.components.Noop;
import org.apache.isis.core.commons.config.IsisConfigurationBuilder;
import org.apache.isis.runtimes.dflt.runtime.systemdependencyinjector.SystemDependencyInjector;
import org.apache.isis.runtimes.dflt.runtime.viewer.web.WebAppSpecification;

public class IsisViewerNoop implements IsisViewer, Noop {

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void setConfigurationBuilder(final IsisConfigurationBuilder configurationLoader) {
    }

    @Override
    public void setSystemDependencyInjector(final SystemDependencyInjector dependencyInjector) {
    }

    public boolean bootstrapsSystem() {
        return false;
    }

    @Override
    public WebAppSpecification getWebAppSpecification() {
        return null;
    }

}
