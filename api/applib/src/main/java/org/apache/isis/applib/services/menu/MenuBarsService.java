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

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotations.Value;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * Responsible for returning a {@link MenuBarsService} instance, a data
 * structure representing the arrangement of domain service actions across
 * multiple menu bars, menus and sections.
 *
 * <p>
 * This is used by the Wicket viewer to build up the menu.  It is also served
 * as the "menuBars" resource by the Restful Objects viewer.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface MenuBarsService {

    @Value(logicalTypeName = IsisModuleApplib.NAMESPACE + ".services.menu.MenuBarsService.Type")
    enum Type {

        /**
         * Either derived from annotations or as obtained elsewhere
         * (eg using the {@link MenuBarsLoaderService} if the
         * default implementation of this service is in use).
         */
        DEFAULT,

        /**
         * As derived from annotations only.
         */
        ANNOTATED
    }


    /**
     * Returns {@link #menuBars()} with a type of {@link Type#DEFAULT}.
     */
    default MenuBars menuBars() {
        return menuBars(Type.DEFAULT);
    }

    /**
     * Returns the menu bars with the requested {@link Type}.
     *
     * @param type - as requested
     */
    MenuBars menuBars(final Type type);

    // -- JUNIT SUPPORT

    static MenuBarsService forTesting() {
        return new MenuBarsService() {

            @Override
            public MenuBars menuBars(Type type) {
                throw _Exceptions.unsupportedOperation();
            }

        };
    }
}
