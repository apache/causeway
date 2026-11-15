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
package org.apache.causeway.extensions.d3.wkt.integration;

import org.apache.wicket.Page;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.markup.html.SecurePackageResourceGuard;
import org.apache.wicket.protocol.http.WebApplication;

import de.agilecoders.wicket.webjars.WicketWebjars;

/**
 * Minimal Wicket {@link WebApplication} for the D3 integration tests: enables the
 * Wicket infrastructure (Session, RequestCycle) without a servlet container, installs
 * webjars (required by {@code D3Reference}) and widens the package-resource guard so
 * that {@code *.mjs} (the D3 integration script) and {@code *.css} resources resolve.
 */
public class D3TestApplication extends WebApplication {

    @Override
    protected void init() {
        super.init();
        WicketWebjars.install(this);
        var resourceGuard = (SecurePackageResourceGuard) getResourceSettings().getPackageResourceGuard();
        resourceGuard.addPattern("+*.mjs");
        resourceGuard.addPattern("+*.css");
    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        return RuntimeConfigurationType.DEVELOPMENT;
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return null;
    }
}
