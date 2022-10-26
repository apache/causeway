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

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
//@Log4j2
public class MenuUiModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @NonNull private final DomainServiceLayout.MenuBar menuBarSelect;
    @NonNull private final List<String> menuContributingServiceIds;

    public String getCssClass() {
        return menuBarSelect.name().toLowerCase(Locale.ENGLISH);
    }

    public void buildMenuItems(
            final MetaModelContext mmc,
            final MenuVisitor menuBuilder) {
        buildMenuItems(mmc.getServiceRegistry().lookupServiceElseFail(MenuUiService.class), menuBuilder);
    }

    public void buildMenuItems(
            final MenuUiService menuUiService,
            final MenuVisitor menuBuilder) {
        menuUiService.buildMenuItems(this, menuBuilder);
    }

}
