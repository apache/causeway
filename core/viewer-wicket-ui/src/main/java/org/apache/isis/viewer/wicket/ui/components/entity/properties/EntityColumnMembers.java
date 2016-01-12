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
package org.apache.isis.viewer.wicket.ui.components.entity.properties;

import java.util.List;
import java.util.Map;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.layout.v1_0.Column;
import org.apache.isis.applib.layout.v1_0.PropertyGroup;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecifications;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.widgets.containers.UiHintPathSignificantWebMarkupContainer;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

public class EntityColumnMembers extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_MEMBER_GROUP = "memberGroup";
    private static final String ID_MEMBER_GROUP_NAME = "memberGroupName";

    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL = "associatedActionLinksPanel";
    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN = "associatedActionLinksPanelDropDown";

    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";

    private final Component owningPanel;

    public EntityColumnMembers(
            final String id,
            final EntityModel entityModel,
            final Component owningPanel) {

        super(id, entityModel);
        this.owningPanel = owningPanel; // for repainting, perhaps

        buildGui();
    }

    private void buildGui() {
        addPropertiesAndCollections(this, getModel());
    }

    private void addPropertiesAndCollections(
            final MarkupContainer col,
            final EntityModel entityModel) {
        addPropertiesInColumn(col, entityModel);
        addCollectionsIfRequired(col, entityModel);
    }

    private void addPropertiesInColumn(
            final MarkupContainer markupContainer,
            final EntityModel entityModel) {

        final Column columnMetaDataIfAny = entityModel.getColumnMetadata();
        final Column.Hint hint = entityModel.getColumnHint();
        final ObjectAdapter adapter = entityModel.getObject();
        final ObjectSpecification objSpec = adapter.getSpecification();

        final Map<String, List<ObjectAssociation>> associationsByGroup = PropUtil
                .propertiesByMemberOrder(adapter);
        final List<String> groupNames = columnMetaDataIfAny != null
                ? FluentIterable
                .from(columnMetaDataIfAny.getPropertyGroups())
                .transform(PropertyGroup.Util.nameOf())
                .toList()
                : ObjectSpecifications.orderByMemberGroups(objSpec, associationsByGroup.keySet(), hint);

        final RepeatingView memberGroupRv = new RepeatingView(ID_MEMBER_GROUP);
        markupContainer.add(memberGroupRv);

        for(final String groupName: groupNames) {
            final List<ObjectAssociation> associationsInGroup = associationsByGroup.get(groupName);
            if(associationsInGroup==null) {
                continue;
            }

            final WebMarkupContainer memberGroupRvContainer = new WebMarkupContainer(memberGroupRv.newChildId());
            memberGroupRv.add(memberGroupRvContainer);
            memberGroupRvContainer.add(new Label(ID_MEMBER_GROUP_NAME, groupName));

            final List<LinkAndLabel> memberGroupActions = Lists.newArrayList();

            final RepeatingView propertyRv = new RepeatingView(ID_PROPERTIES);
            memberGroupRvContainer.add(propertyRv);

            @SuppressWarnings("unused")
            Component component;
            for (final ObjectAssociation association : associationsInGroup) {
                final WebMarkupContainer propertyRvContainer = new UiHintPathSignificantWebMarkupContainer(propertyRv.newChildId());
                propertyRv.add(propertyRvContainer);

                addPropertyToForm(entityModel, (OneToOneAssociation) association, propertyRvContainer, memberGroupActions);
            }

            final List<LinkAndLabel> actionsPanel = LinkAndLabel.positioned(memberGroupActions, ActionLayout.Position.PANEL);
            final List<LinkAndLabel> actionsPanelDropDown = LinkAndLabel.positioned(memberGroupActions, ActionLayout.Position.PANEL_DROPDOWN);

            AdditionalLinksPanel.addAdditionalLinks(
                    memberGroupRvContainer, ID_ASSOCIATED_ACTION_LINKS_PANEL,
                    actionsPanel,
                    AdditionalLinksPanel.Style.INLINE_LIST);
            AdditionalLinksPanel.addAdditionalLinks(
                    memberGroupRvContainer, ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN,
                    actionsPanelDropDown,
                    AdditionalLinksPanel.Style.DROPDOWN);
        }
    }

    private void addCollectionsIfRequired(
            final MarkupContainer column,
            final EntityModel entityModel) {

        final Column columnMetaDataIfAny = entityModel.getColumnMetadata();

        if(columnMetaDataIfAny != null) {
            getComponentFactoryRegistry()
                    .addOrReplaceComponent(column, "collections", ComponentType.ENTITY_COLLECTIONS, entityModel);
        } else {
            Components.permanentlyHide(column, "collections");
        }
    }

    private void addPropertyToForm(
            final EntityModel entityModel,
            final OneToOneAssociation otoa,
            final WebMarkupContainer container,
            final List<LinkAndLabel> entityActions) {
        final PropertyMemento pm = new PropertyMemento(otoa);

        final ScalarModel scalarModel = entityModel.getPropertyModel(pm);
        final Component component = getComponentFactoryRegistry().addOrReplaceComponent(container, ID_PROPERTY, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);

        final List<ObjectAction> associatedActions = EntityActionUtil.getObjectActionsForAssociation(entityModel,
                otoa, getDeploymentType());

        entityActions.addAll(EntityActionUtil.asLinkAndLabelsForAdditionalLinksPanel(entityModel, associatedActions));
    }



    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    protected DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }

}
