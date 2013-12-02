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

package org.apache.isis.viewer.wicket.ui.pages.entity;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.actions.homepage.HomePageFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class EntityPage extends PageAbstract {

    private static final long serialVersionUID = 1L;
    
    private final EntityModel model;


    /**
     * Called reflectively, in support of 
     * {@link BookmarkablePageLink bookmarkable} links.
     */
    public EntityPage(final PageParameters pageParameters) {
        this(pageParameters, new EntityModel(pageParameters));
    }
    
    private EntityPage(final PageParameters pageParameters, final EntityModel entityModel) {
        this(pageParameters, entityModel, entityModel.getObject().titleString(null));
    }

    public EntityPage(final ObjectAdapter adapter) {
        this(adapter, null);
    }

    /**
     * Ensure that any {@link ConcurrencyException} that might have occurred already
     * (eg from an action invocation) is show.
     */
    public EntityPage(ObjectAdapter adapter, ConcurrencyException exIfAny) {
        this(new PageParameters(), newEntityModel(adapter, exIfAny));
    }

    private static EntityModel newEntityModel(ObjectAdapter adapter, ConcurrencyException exIfAny) {
        EntityModel model = new EntityModel(adapter);
        model.setException(exIfAny);
        return model;
    }

    private EntityPage(PageParameters pageParameters, EntityModel entityModel, String titleString) {
        super(pageParameters, ApplicationActions.INCLUDE, titleString, ComponentType.ENTITY);
        this.model = entityModel;
        addChildComponents(model);
        
        final ObjectAndAction objectAndAction =lookupHomePageAction();
        final ActionModel actionModel = ActionModel.create(objectAndAction.objectAdapter, objectAndAction.action);
        
        bookmarkPage(model);
        addBookmarkedPages();
    }

    private static class ObjectAndAction {
        ObjectAndAction(final ObjectAdapter serviceAdapter, final ObjectAction objectAction) {
            this.objectAdapter = serviceAdapter;
            action = objectAction;
        }
        ObjectAdapter objectAdapter;
        ObjectAction action;
    }
    
    private ObjectAndAction lookupHomePageAction() {
        List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (ObjectAdapter serviceAdapter : serviceAdapters) {
            final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
            List<ObjectAction> objectActions = serviceSpec.getObjectActions(Contributed.EXCLUDED);
            for (ObjectAction objectAction : objectActions) {
                if(objectAction.containsFacet(HomePageFacet.class)) {
                    return new ObjectAndAction(serviceAdapter, objectAction);
                }
            }
        }
        return null;
    }


    /**
     * A rather crude way of intercepting the redirect-and-post strategy.
     * 
     * <p>
     * Performs eager loading of corresponding {@link EntityModel}, with
     * {@link ConcurrencyChecking#NO_CHECK no} concurrency checking.
     */
    @Override
    protected void onBeforeRender() {
        this.model.load(ConcurrencyChecking.NO_CHECK);
        super.onBeforeRender();
    }

}
