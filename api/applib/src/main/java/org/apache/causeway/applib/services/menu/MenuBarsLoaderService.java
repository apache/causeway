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
package org.apache.causeway.applib.services.menu;

import java.util.Optional;

import org.apache.causeway.applib.layout.menubars.MenuBars;

import lombok.NonNull;

/**
 * Returns the {@link MenuBars} instance for the UI.
 *
 * <p>
 *     The default implementation de-serializes the `menubars.layout...` file
 *     read from the classpath.
 * </p>
 *
 * <p>
 *     The service is <i>called</i> by the default implementation of
 *     {@link MenuBarsService}.
 * </p>
 *
 * @since 1.x - revised for 2.0 {@index}
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
     * Optionally returns a new instance of a {@link MenuBars},
     * based on whether the underlying resource could be found, loaded and parsed.
     * @throws UnsupportedOperationException - when format is not supported
     */
    <T extends MenuBars> Optional<T> menuBars(@NonNull MenuBarsMarshallerService<T> marshaller);

}
