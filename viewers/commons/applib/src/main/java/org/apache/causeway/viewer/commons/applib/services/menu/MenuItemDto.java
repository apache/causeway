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
package org.apache.causeway.viewer.commons.applib.services.menu;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;

import org.jspecify.annotations.NonNull;

public record MenuItemDto(
        @NonNull String name,
        @Nullable String cssClassFa,
        // eg. topLevel
        @Nullable ManagedAction managedAction,
        boolean isTertiaryRoot) {

    public static MenuItemDto topLevel(final String name, final String cssClassFa) {
        return new MenuItemDto(name, cssClassFa, null, false);
    }

    public static MenuItemDto tertiaryRoot(final String name, final String cssClassFa) {
        return new MenuItemDto(name, cssClassFa, null, true);
    }

    public static MenuItemDto subMenu(final @NonNull ManagedAction managedAction, final String named, final String cssClassFa) {
        var name = _Strings.isNotEmpty(named)
                ? named
                : managedAction.getFriendlyName();
        return new MenuItemDto(name, cssClassFa, managedAction, false);
    }

}
