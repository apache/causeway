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
package org.apache.causeway.viewer.commons.services.menu;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBar;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuUiModel;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuUiService;
import org.apache.causeway.viewer.commons.applib.services.menu.MenuVisitor;
import org.apache.causeway.viewer.commons.services.CausewayModuleViewerCommonsServices;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named(CausewayModuleViewerCommonsServices.NAMESPACE + ".MenuUiServiceDefault")
@Priority(PriorityPrecedence.LATE)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class MenuUiServiceDefault
implements MenuUiService {

    private final MetaModelContext metaModelContext;
    private final MenuBarsService menuBarsService;

    @Override
    public MenuUiModel getMenu(final DomainServiceLayout.MenuBar menuBarSelect) {
        return MenuUiModel.of(menuBarSelect, select(menuBarSelect));
    }

    @Override
    public void buildMenuItems(
            final MenuUiModel menuUiModel,
            final MenuVisitor menuBuilder) {

        val menuBars = menuBarsService.menuBars();
        val menuBar = (BSMenuBar) menuBars.menuBarFor(menuUiModel.getMenuBarSelect());

        _MenuItemBuilder.buildMenuItems(
                metaModelContext,
                menuBar,
                menuBuilder);

    }

    // -- HELPER

    private List<String> select(final DomainServiceLayout.MenuBar menuBarSelect) {
        return metaModelContext.streamServiceAdapters()
                .filter(with(menuBarSelect))
                .map(ManagedObject::getSpecification)
                .map(ObjectSpecification::getLogicalTypeName)
                .collect(Collectors.toList());
    }

    private static Predicate<ManagedObject> with(final DomainServiceLayout.MenuBar menuBarSelect) {
        return (final ManagedObject adapter) ->

            Facets.domainServiceLayoutMenuBar(adapter.getSpecification())
                    .orElse(DomainServiceLayout.MenuBar.PRIMARY)
                    .equals(menuBarSelect);
    }

}
