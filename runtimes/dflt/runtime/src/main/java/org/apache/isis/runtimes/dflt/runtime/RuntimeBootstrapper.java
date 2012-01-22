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

package org.apache.isis.runtimes.dflt.runtime;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Injector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import org.apache.isis.core.commons.lang.Threads;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.InstallerLookup;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.EmbeddedWebServerInstaller;
import org.apache.isis.runtimes.dflt.runtime.runner.IsisBootstrapper;
import org.apache.isis.runtimes.dflt.runtime.runner.IsisModule.ViewerList;
import org.apache.isis.runtimes.dflt.runtime.system.IsisSystem;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.viewer.IsisViewer;
import org.apache.isis.runtimes.dflt.runtime.viewer.web.WebAppSpecification;
import org.apache.isis.runtimes.dflt.runtime.web.EmbeddedWebServer;

final class RuntimeBootstrapper implements IsisBootstrapper {

    @Override
    public void bootstrap(final Injector injector) {

        bootstrapSystem(injector);
        bootstrapViewers(injector);
    }

    private void bootstrapSystem(final Injector injector) {

        // sufficient just to look it up
        @SuppressWarnings("unused")
        final IsisSystem system = injector.getInstance(IsisSystem.class);
    }

    private void bootstrapViewers(final Injector injector) {
        final List<IsisViewer> viewers = lookupViewers(injector);

        // split viewers into web viewers and non-web viewers
        final List<IsisViewer> webViewers = findWebViewers(viewers);
        final List<IsisViewer> nonWebViewers = findNonWebViewers(viewers, webViewers);

        startNonWebViewers(nonWebViewers);
        startWebViewers(injector, webViewers);
    }

    private List<IsisViewer> lookupViewers(final Injector injector) {
        final List<IsisViewer> viewers = injector.getInstance(ViewerList.class).getViewers();

        // looking up viewers may have merged in some further config files,
        // so update the NOContext global
        // REVIEW: would rather inject this
        final InstallerLookup installerLookup = injector.getInstance(InstallerLookup.class);
        IsisContext.setConfiguration(installerLookup.getConfiguration());

        return viewers;
    }

    private List<IsisViewer> findWebViewers(final List<IsisViewer> viewers) {
        final List<IsisViewer> webViewers = new ArrayList<IsisViewer>(viewers);
        CollectionUtils.filter(webViewers, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                final IsisViewer viewer = (IsisViewer) object;
                return viewer.getWebAppSpecification() != null;
            }
        });
        return webViewers;
    }

    private List<IsisViewer> findNonWebViewers(final List<IsisViewer> viewers, final List<IsisViewer> webViewers) {
        final List<IsisViewer> nonWebViewers = new ArrayList<IsisViewer>(viewers);
        nonWebViewers.removeAll(webViewers);
        return nonWebViewers;
    }

    /**
     * Starts each (non web) {@link IsisViewer viewer} in its own thread.
     */
    private void startNonWebViewers(final List<IsisViewer> viewers) {
        for (final IsisViewer viewer : viewers) {
            final Runnable target = new Runnable() {
                @Override
                public void run() {
                    viewer.init();
                }
            };
            Threads.startThread(target, "Viewer");
        }
    }

    /**
     * Starts all the web {@link IsisViewer viewer}s in an instance of an
     * {@link EmbeddedWebServer}.
     */
    private void startWebViewers(final Injector injector, final List<IsisViewer> webViewers) {
        if (webViewers.size() == 0) {
            return;
        }

        final InstallerLookup installerLookup = injector.getInstance(InstallerLookup.class);

        // TODO: we could potentially offer pluggability here
        final EmbeddedWebServerInstaller webServerInstaller = installerLookup.embeddedWebServerInstaller(Isis.DEFAULT_EMBEDDED_WEBSERVER);
        final EmbeddedWebServer embeddedWebServer = webServerInstaller.createEmbeddedWebServer();
        for (final IsisViewer viewer : webViewers) {
            final WebAppSpecification webContainerRequirements = viewer.getWebAppSpecification();
            embeddedWebServer.addWebAppSpecification(webContainerRequirements);
        }
        embeddedWebServer.init();
    }
}