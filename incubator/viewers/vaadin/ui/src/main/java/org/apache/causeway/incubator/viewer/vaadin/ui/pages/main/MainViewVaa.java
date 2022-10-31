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
package org.apache.causeway.incubator.viewer.vaadin.ui.pages.main;

import javax.inject.Inject;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.incubator.viewer.vaadin.model.context.MemberInvocationHandler;
import org.apache.causeway.incubator.viewer.vaadin.model.context.UiContextVaa;
import org.apache.causeway.incubator.viewer.vaadin.ui.components.UiComponentFactoryVaa;
import org.apache.causeway.incubator.viewer.vaadin.ui.components.collection.TableViewVaa;
import org.apache.causeway.incubator.viewer.vaadin.ui.components.object.ObjectViewVaa;
import org.apache.causeway.incubator.viewer.vaadin.ui.util.LocalResourceUtil;
import org.apache.causeway.viewer.commons.applib.services.header.HeaderUiService;
import org.apache.causeway.viewer.commons.model.decorators.IconDecorator;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * top-level view
 */
@Route("main")
@RouteAlias("")
@JsModule("@vaadin/vaadin-lumo-styles/presets/compact.js")
@CssImport("./css/menu.css")
@Log4j2
public class MainViewVaa extends AppLayout
implements
    HasMetaModelContext,
    BeforeEnterObserver,
    MemberInvocationHandler<Component> {

    private static final long serialVersionUID = 1L;

    private final transient MetaModelContext metaModelContext;
    private final transient UiContextVaa uiContext;
    private final transient UiActionHandlerVaa uiActionHandler;
    private final transient UiComponentFactoryVaa uiComponentFactory;
    private final transient HeaderUiService headerUiService;

    private Div pageContent = new Div();

    /**
     * Constructs the main view of the web-application, with the menu-bar and page content.
     */
    @Inject
    public MainViewVaa(
            final MetaModelContext metaModelContext,
            final UiActionHandlerVaa uiActionHandler,
            final HeaderUiService headerUiService,
            final UiContextVaa uiContext,
            final UiComponentFactoryVaa uiComponentFactory) {

        this.metaModelContext = metaModelContext;
        this.uiActionHandler = uiActionHandler;
        this.headerUiService = headerUiService;
        this.uiContext = uiContext;
        this.uiComponentFactory = uiComponentFactory;

        uiContext.setNewPageHandler(this::replaceContent);
        uiContext.setPageFactory(this);
    }

    @Override
    public void beforeEnter(final BeforeEnterEvent event) {

        val faStyleSheet = LocalResourceUtil.ResourceDescriptor.webjars(IconDecorator.FONTAWESOME_RESOURCE);
        LocalResourceUtil.addStyleSheet(faStyleSheet);

        setPrimarySection(Section.NAVBAR);

        val menuBarContainer = MainView_createHeader.createHeader(
                metaModelContext,
                headerUiService.getHeader(),
                uiActionHandler::handleActionLinkClicked,
                this::renderHomepage);

        addToNavbar(menuBarContainer);
        setContent(pageContent = new Div());
        setDrawerOpened(false);
        renderHomepage();
    }

    private void replaceContent(final Component component) {
        pageContent.removeAll();
        pageContent.add(component);
    }

    private void renderHomepage() {
        log.info("about to render homepage");
        val homepage = getInteractionService().callAnonymous(metaModelContext::getHomePageAdapter);
        uiContext.route(homepage);
    }

    @Override
    public Component handle(final ManagedObject object) {
        return ObjectViewVaa.fromObject(
                uiContext,
                uiComponentFactory,
                uiActionHandler::handleActionLinkClicked,
                object);
    }

    @Override
    public Component handle(final ManagedAction managedAction, final Can<ManagedObject> params, final ManagedObject actionResult) {
        if (actionResult.getSpecification().isPlural()) {

            val dataTableModel = DataTableModel.forAction(managedAction, params, actionResult);

            return TableViewVaa.forDataTableModel(uiContext, dataTableModel, Where.STANDALONE_TABLES);
        } else {
            return handle(actionResult);
        }
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        if(metaModelContext==null) {
            // TODO needs static recovery
            throw _Exceptions.notImplemented();
        }
        return metaModelContext;
    }


}
