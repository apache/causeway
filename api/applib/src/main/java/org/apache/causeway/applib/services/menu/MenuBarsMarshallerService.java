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

import java.util.EnumSet;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.layout.menubars.MenuBars;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Try;

import lombok.NonNull;

/**
 * Supports {@link MenuBars} marshaling and unmarshaling.
 * <p>
 * The service is <i>called</i> by the default implementations of
 * {@link MenuBarsService} and {@link LayoutService}.
 *
 * @since 2.0 {@index}
 */
public interface MenuBarsMarshallerService<T extends MenuBars> {

    Class<T> implementedMenuBarsClass();

    /**
     * Supported format(s) for {@link #unmarshal(String, CommonMimeType)}
     * and {@link #marshal(MenuBars, CommonMimeType)}.
     */
    EnumSet<CommonMimeType> supportedFormats();

    /**
     * @throws UnsupportedOperationException when format is not supported
     */
    String marshal(@NonNull T menuBars, @NonNull CommonMimeType format);

    /**
     * Returns a new instance of a {@link MenuBars} wrapped in a {@link Try}.
     * @throws UnsupportedOperationException when format is not supported (not wrapped)
     */
    Try<T> unmarshal(@Nullable String layoutFileContent, @NonNull CommonMimeType format);

}
