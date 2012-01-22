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

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfigurationBuilderAware;
import org.apache.isis.runtimes.dflt.runtime.systemdependencyinjector.SystemDependencyInjectorAware;
import org.apache.isis.runtimes.dflt.runtime.viewer.web.WebAppSpecification;

/**
 * Defines an mechanism for manipulating the domain objects.
 * 
 * <p>
 * The mechanism may be realized as a user interface (for example the DnD viewer
 * or HTML viewer) but might also be an abstract 'remoting' viewer of sockets or
 * HTTP servlet requests.
 */
public interface IsisViewer extends ApplicationScopedComponent, SystemDependencyInjectorAware, IsisConfigurationBuilderAware {

    /**
     * Provide requirement for running a viewer from within an embedded web
     * container.
     * 
     * <p>
     * Returns <tt>null</tt> if does not run within a web container.
     */
    WebAppSpecification getWebAppSpecification();
}
