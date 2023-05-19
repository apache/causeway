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

import java.util.Locale;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuVisitor;

import lombok.val;

public record NavbarSection(
        DomainServiceLayout.MenuBar menuBarSelect,
        Can<MenuDropdown> topLevelEntries) {

    public String cssClass() {
        return menuBarSelect.name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * Depth-first visit of the model.
     * @param menuVisitor
     */
    public void visitMenuItems(final @Nullable MenuVisitor menuVisitor) {
        if(menuVisitor==null) return;

        topLevelEntries.forEach(topLevel->{
            menuVisitor.onTopLevel(topLevel);
            topLevel.subEntries().forEach(subEntry->{
                val asAction = subEntry.asAction();
                asAction.ifPresentOrElse(menuVisitor::onMenuAction, ()->{
                    val asSpacer = subEntry.asSpacer();
                    asSpacer.ifPresent(spacer->{
                        if(spacer.isEmpty()) {
                            menuVisitor.onSectionSpacer();
                        } else {
                            menuVisitor.onSectionLabel(spacer.label());
                        }
                    });
                });

            });
        });

    }

}