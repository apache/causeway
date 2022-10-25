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

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

@Value(staticConstructor = "of")
public class MenuItemDto {

    @NonNull
    private final String name;

    @Nullable
    private final String cssClassFa;

    @Nullable // eg. topLevel
    private final ManagedAction managedAction;

    private final boolean isTertiaryRoot;

    public static MenuItemDto topLevel(final String name, final String cssClassFa) {
        return of(name, cssClassFa, null, false);
    }

    public static MenuItemDto tertiaryRoot(final String name, final String cssClassFa) {
        return of(name, cssClassFa, null, true);
    }

    public static MenuItemDto subMenu(@NonNull final ManagedAction managedAction, final String named, final String cssClassFa) {
        val name = _Strings.isNotEmpty(named)
                ? named
                : managedAction.getFriendlyName();
        return of(name, cssClassFa, managedAction, false);
    }

}
