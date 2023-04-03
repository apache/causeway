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
package org.apache.causeway.viewer.thymeflux.model.root;

import java.awt.MenuItem;

import org.apache.causeway.viewer.commons.applib.services.menu.MenuItemDto;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuVisitor;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

//TODO just a stub yet
@RequiredArgsConstructor(staticName = "of")
@Log4j2
class MenuBuilder implements MenuVisitor {

    @Override
    public void addTopLevel(final MenuItemDto menuDto) {
        log.debug("top level menu {}", menuDto.getName());
    }

    @Override
    public void addSubMenu(final MenuItemDto menuDto) {
        val managedAction = menuDto.getManagedAction();
        log.debug("sub menu {}", menuDto.getName());
    }

    @Override
    public void addSectionSpacer() {
        log.debug("menu spacer");
    }

    @Override
    public void addSectionLabel(final String named) {
        log.debug("section label  {}", named);
        val menuItem = new MenuItem(named);
    }

}
