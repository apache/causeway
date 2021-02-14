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
package org.apache.isis.applib.services.menu;

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;

/**
 * Returns the {@link BS3MenuBars} instance (bootstrap3-specific subtype of
 * {@link org.apache.isis.applib.layout.menubars.MenuBars}, for the UI.
 *
 * <p>
 *     The default implementation deserializes the `menubars.layout.xml` file
 *     read from the classpath.
 * </p>
 *
 * <p>
 *     The service is <i>called</i> by the default implementation of
 *     {@link MenuBarsService}.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface MenuBarsLoaderService {

    /**
     * Whether dynamic reloading of layouts is enabled.
     *
     * <p>
     * If not, then the calling {@link MenuBarsService}will cache the layout
     * once loaded.
     * </p>
     */
    boolean supportsReloading();

    /**
     * Returns a new instance of a {@link BS3MenuBars} if possible,
     * else <tt>null</tt>.
     */
    BS3MenuBars menuBars();

}
