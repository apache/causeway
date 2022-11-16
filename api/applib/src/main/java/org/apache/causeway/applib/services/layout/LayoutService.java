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
package org.apache.causeway.applib.services.layout;

import java.util.EnumSet;

import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;

/**
 * Provides the ability to obtain the serialized layout (eg. XML) for a single domain object or
 * for all domain objects, as well as the serialized layout for the application's menu-bars.
 *
 * @since 1.x - revised for 2.0 {@index}
 */
public interface LayoutService {

    // -- OBJECT LAYOUT

    /**
     * Supported format(s) for {@link #objectLayout(Class, LayoutExportStyle, CommonMimeType)}
     * and {@link #toZip(LayoutExportStyle, CommonMimeType)}.
     */
    EnumSet<CommonMimeType> supportedObjectLayoutFormats();

    /**
     * Obtains the serialized form of the object layout (grid) for the specified domain class.
     * @throws UnsupportedOperationException when format is not supported
     */
    String objectLayout(Class<?> domainClass, LayoutExportStyle style, CommonMimeType format);

    /**
     * Obtains a zip file of the serialized layouts (grids) of all domain entities and view models.
     * @throws UnsupportedOperationException when format is not supported
     */
    byte[] toZip(LayoutExportStyle style, CommonMimeType format);

    // -- MENUBARS LAYOUT

    /**
     * Supported format(s) for
     * {@link #menuBarsLayout(org.apache.causeway.applib.services.menu.MenuBarsService.Type, CommonMimeType)}.
     */
    EnumSet<CommonMimeType> supportedMenuBarsLayoutFormats();

    /**
     * Obtains the serialized form of the menu bars layout ({@link MenuBarsService}).
     * @throws UnsupportedOperationException when format is not supported
     */
    String menuBarsLayout(MenuBarsService.Type type, CommonMimeType format);

}
