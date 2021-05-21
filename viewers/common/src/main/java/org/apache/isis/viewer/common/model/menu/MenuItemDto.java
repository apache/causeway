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
package org.apache.isis.viewer.common.model.menu;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.viewer.common.model.action.ActionUiMetaModel;

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

    public static MenuItemDto topLevel(String name, String cssClassFa) {
        return of(name, cssClassFa, null, false);
    }

    public static MenuItemDto tertiaryRoot(String name, String cssClassFa) {
        return of(name, cssClassFa, null, true);
    }

    public static MenuItemDto subMenu(@NonNull ManagedAction managedAction, String named, String cssClassFa) {
        val name = _Strings.isNotEmpty(named)
                ? named
                : ActionUiMetaModel.of(managedAction).getLabel();
        return of(name, cssClassFa, managedAction, false);
    }

}
