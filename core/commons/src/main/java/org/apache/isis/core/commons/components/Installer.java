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

package org.apache.isis.core.commons.components;

import java.util.List;

/**
 * A factory for a {@link Component}, defining that component's
 * {@link #getType() type} and its {@link #getName() name}.
 *
 * <p>
 * The ({@link #getType() type}, {@link #getName() name}) is expected to be a
 * unique identifier of a component.
 *
 * <p>
 * The <i>default runtime</i> (<tt>org.apache.isis.runtimes.dflt</tt> module),
 * which adopts a service locator design, uses the
 * <tt>installer-registry.properties</tt> resource as a registry of all
 * available installers. The installers are loaded and indexed by their name and
 * type. Other runtime implementations may use different approaches.
 */
public interface Installer extends ApplicationScopedComponent {


    /**
     * The name (qualified by type).
     */
    String getName();

    List<Class<?>> getTypes();
}
