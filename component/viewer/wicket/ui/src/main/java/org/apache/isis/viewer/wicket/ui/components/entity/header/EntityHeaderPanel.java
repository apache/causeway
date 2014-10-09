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

package org.apache.isis.viewer.wicket.ui.components.entity.header;

import java.util.List;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.wicket.Component;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing the summary details (title, icon and
 * actions) of an entity, as per the provided {@link EntityModel}.
 */
public class EntityHeaderPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_ACTIONS = "entityActions";

    private final EntityActionLinkFactory linkFactory;


    public EntityHeaderPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        linkFactory = new EntityActionLinkFactory(getEntityModel());
    }

    /**
     * For the {@link EntityActionLinkFactory}.
     */
    public EntityModel getEntityModel() {
        return getModel();
    }

    @Override
    protected void onBeforeRender() {
        buildGui();
        super.onBeforeRender();
    }

    private void buildGui() {
        addOrReplaceIconAndTitle();
        buildEntityActionsGui();
    }

    private void addOrReplaceIconAndTitle() {
        final ComponentFactory componentFactory = getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_TITLE_AND_COPYLINK, getEntityModel());
        final Component component = componentFactory.createComponent(getEntityModel());
        addOrReplace(component);
    }


    private void buildEntityActionsGui() {
        final EntityModel model = getModel();
        final ObjectAdapter adapter = model.getObject();
        final ObjectAdapterMemento adapterMemento = model.getObjectAdapterMemento();
        if (adapter != null) {
            final List<ObjectAction> topLevelActions = getTopLevelActions(adapter);

            if(!topLevelActions.isEmpty()) {
                final ActionPromptProvider actionPromptProvider = ActionPromptProvider.Util.getFrom(this);
                final CssMenuBuilder cssMenuBuilder = new CssMenuBuilder(
                        adapterMemento, topLevelActions, linkFactory, actionPromptProvider, null);
                final CssMenuPanel cssMenuPanel = cssMenuBuilder.buildPanel(ID_ENTITY_ACTIONS, "Actions");

                this.addOrReplace(cssMenuPanel);
            } else {
                permanentlyHide(ID_ENTITY_ACTIONS);
            }
        } else {
            permanentlyHide(ID_ENTITY_ACTIONS);
        }
    }

    private List<ObjectAction> getTopLevelActions(final ObjectAdapter adapter) {
        final List<ObjectAction> topLevelActions = Lists.newArrayList();
        
        addTopLevelActions(adapter, ActionType.USER, topLevelActions);
        if(getDeploymentType().isPrototyping()) {
            addTopLevelActions(adapter, ActionType.EXPLORATION, topLevelActions);
            addTopLevelActions(adapter, ActionType.PROTOTYPE, topLevelActions);
        }
        return topLevelActions;
    }

    private void addTopLevelActions(
            final ObjectAdapter adapter,
            final ActionType actionType,
            final List<ObjectAction> topLevelActions) {

        final ObjectSpecification adapterSpec = adapter.getSpecification();
        final AuthenticationSession authenticationSession = getAuthenticationSession();

        @SuppressWarnings({ "unchecked", "deprecation" })
        Filter<ObjectAction> filter = Filters.and(
                ObjectAction.Filters.memberOrderNotAssociationOf(adapterSpec),
                ObjectAction.Filters.dynamicallyVisible(authenticationSession, adapter, Where.ANYWHERE),
                ObjectAction.Filters.notBulkOnly(),
                ObjectAction.Filters.excludeWizardActions(adapterSpec));

        final List<ObjectAction> userActions = adapterSpec.getObjectActions(actionType, Contributed.INCLUDED, filter);
        topLevelActions.addAll(userActions);
    }


    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    protected PageClassRegistry getPageClassRegistry() {
        final PageClassRegistryAccessor pcra = (PageClassRegistryAccessor) getApplication();
        return pcra.getPageClassRegistry();
    }


    // ///////////////////////////////////////////////
    // Dependency Injection
    // ///////////////////////////////////////////////

    @Inject
    private ImageResourceCache imageCache;

    protected ImageResourceCache getImageCache() {
        return imageCache;
    }


    protected DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }


}
