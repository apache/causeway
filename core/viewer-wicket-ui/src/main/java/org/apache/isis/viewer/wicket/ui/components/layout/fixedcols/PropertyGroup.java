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
package org.apache.isis.viewer.wicket.ui.components.layout.fixedcols;

import java.util.List;

import com.google.common.collect.Lists;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class PropertyGroup extends PanelAbstract<EntityModel> {

    private static final String ID_MEMBER_GROUP_NAME = "memberGroupName";

    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL = "associatedActionLinksPanel";
    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN = "associatedActionLinksPanelDropDown";

    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";

    private final FieldSet fieldSet;

    public PropertyGroup(final String id, final EntityModel model) {
        super(id, model);
        fieldSet = (FieldSet) model.getLayoutMetadata();

        buildGui();
    }

    public EntityModel getModel() {
        return (EntityModel) getDefaultModel();
    }

    private void buildGui() {
        String groupName = fieldSet.getName();
        final ObjectAdapter adapter = getModel().getObject();

        add(new Label(ID_MEMBER_GROUP_NAME, groupName));

        final List<LinkAndLabel> memberGroupActions = Lists.newArrayList();

        final RepeatingView propertyRv = new RepeatingView(ID_PROPERTIES);
        add(propertyRv);

        final List<PropertyLayoutData> properties = fieldSet.getProperties();
        for (PropertyLayoutData property : properties) {
            final ObjectAssociation association = adapter.getSpecification().getAssociation(property.getId());

            final WebMarkupContainer propertyRvContainer = new WebMarkupContainer(propertyRv.newChildId());
            propertyRv.add(propertyRvContainer);

            addPropertyToForm(getModel(), (OneToOneAssociation) association, propertyRvContainer,
                    memberGroupActions);
        }

        final List<LinkAndLabel> actionsPanel = LinkAndLabel
                .positioned(memberGroupActions, ActionLayout.Position.PANEL);
        final List<LinkAndLabel> actionsPanelDropDown = LinkAndLabel
                .positioned(memberGroupActions, ActionLayout.Position.PANEL_DROPDOWN);

        AdditionalLinksPanel.addAdditionalLinks(
                this, ID_ASSOCIATED_ACTION_LINKS_PANEL,
                actionsPanel,
                AdditionalLinksPanel.Style.INLINE_LIST);
        AdditionalLinksPanel.addAdditionalLinks(
                this, ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN,
                actionsPanelDropDown,
                AdditionalLinksPanel.Style.DROPDOWN);
    }

    private void addPropertyToForm(
            final EntityModel entityModel,
            final OneToOneAssociation otoa,
            final WebMarkupContainer container,
            final List<LinkAndLabel> entityActions) {
        final PropertyMemento pm = new PropertyMemento(otoa);

        final ScalarModel scalarModel = entityModel.getPropertyModel(pm);
        getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ID_PROPERTY, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);

        final List<ObjectAction> associatedActions =
                EntityActionUtil.getObjectActionsForAssociation(entityModel, otoa, getDeploymentCategory());

        entityActions.addAll(
                EntityActionUtil.asLinkAndLabelsForAdditionalLinksPanel(entityModel, associatedActions));
    }
}
