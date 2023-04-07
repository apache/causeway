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
package org.apache.causeway.viewer.commons.applib.services.menu.model;

import java.util.List;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;

import lombok.NonNull;

public record MenuDropdownBuilder (
        @NonNull String name,
        @NonNull List<MenuEntry> subEntries) {

    public void addSectionSpacer() {
        subEntries().add(MenuSpacer.empty());
    }

    public void addSectionSpacer(final @NonNull String label) {
        subEntries().add(new MenuSpacer(label));
    }

    public void addAction(final ManagedAction action) {
        subEntries().add(MenuAction.of(action));
    }

    public MenuDropdown build() {
        return new MenuDropdown(name, Can.ofCollection(subEntries));
    }
}
