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

package org.apache.isis.viewer.html.monitoring;

import org.apache.isis.core.runtime.installerregistry.installerapi.IsisViewerInstallerAbstract;
import org.apache.isis.core.runtime.viewer.IsisViewer;
import org.apache.isis.core.runtime.viewer.web.WebAppSpecification;
import org.apache.isis.core.runtime.web.EmbeddedWebViewer;

public class WebServerMonitorInstaller extends IsisViewerInstallerAbstract {

    public WebServerMonitorInstaller() {
        super("web-monitor");
    }

    @Override
    public IsisViewer doCreateViewer() {
        return new EmbeddedWebViewer() {
            @Override
            public WebAppSpecification getWebAppSpecification() {
                final WebAppSpecification requirements = new WebAppSpecification();
                requirements.addServletSpecification(MonitorServlet.class, "/monitor/*");
                return requirements;
            }
        };
    }

}
