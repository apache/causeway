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
package org.apache.isis.viewer.wicket.ui.components.entity.fieldset;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.mementos.PropertyMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelUtil;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormExecutor;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

public class PropertyGroup extends PanelAbstract<EntityModel> implements HasDynamicallyVisibleContent {

    private static final String ID_MEMBER_GROUP = "memberGroup";
    private static final String ID_MEMBER_GROUP_NAME = "memberGroupName";

    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL = "associatedActionLinksPanel";
    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN = "associatedActionLinksPanelDropDown";

    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";

    private final FieldSet fieldSet;

    public PropertyGroup(final String id, final EntityModel model, final FieldSet fieldSet) {
        super(id, model);
        this.fieldSet = fieldSet;

        buildGui();
    }

    public EntityModel getModel() {
        return (EntityModel) getDefaultModel();
    }

    private void buildGui() {

        final List<PropertyLayoutData> properties = fieldSet.getProperties();

        // changed to NO_CHECK because more complex BS3 layouts trip concurrency exception (haven't investigated as to why).
        final ObjectAdapter adapter = getModel().load(AdapterManager.ConcurrencyChecking.NO_CHECK);

        final WebMarkupContainer div = new WebMarkupContainer(ID_MEMBER_GROUP);

        String groupName = fieldSet.getName();


        final List<LinkAndLabel> memberGroupActions = Lists.newArrayList();
        final RepeatingView propertyRv = new RepeatingView(ID_PROPERTIES);
        div.add(propertyRv);

        final ImmutableList<ObjectAssociation> visibleAssociations = FluentIterable.from(properties)
                .filter(new Predicate<PropertyLayoutData>() {
                    @Override
                    public boolean apply(final PropertyLayoutData propertyLayoutData) {
                        return propertyLayoutData.getMetadataError() == null;
                    }
                })
                .transform(new Function<PropertyLayoutData, ObjectAssociation>() {
                    @Override
                    public ObjectAssociation apply(final PropertyLayoutData propertyLayoutData) {
                        ObjectSpecification adapterSpecification = adapter.getSpecification();
                        try {
                            // this shouldn't happen, but has been reported (https://issues.apache.org/jira/browse/ISIS-1574),
                            // suggesting that in some cases the GridService can get it wrong.  This is therefore a hack...
                            return adapterSpecification.getAssociation(propertyLayoutData.getId());
                        } catch (ObjectSpecificationException e) {
                            return null;
                        }
                    }
                })
                // TODO: this should probably be dynamic, as result of ISIS-1613 improved UI (no redirect)
                .filter(new Predicate<ObjectAssociation>() {
                    @Override public boolean apply(@Nullable final ObjectAssociation objectAssociation) {
                        if(objectAssociation == null) {
                            return false;
                        }
                        final Consent visibility =
                                objectAssociation .isVisible(adapter, InteractionInitiatedBy.USER, Where.OBJECT_FORMS);
                        return visibility.isAllowed();
                    }
                })
                .toList();

        for (final ObjectAssociation association : visibleAssociations) {
            final WebMarkupContainer propertyRvContainer = new WebMarkupContainer(propertyRv.newChildId());
            propertyRv.add(propertyRvContainer);
            addPropertyToForm(getModel(), (OneToOneAssociation) association, propertyRvContainer, memberGroupActions);
            visible = true;
        }

        WebMarkupContainer panelHeading = new WebMarkupContainer("panelHeading");
        div.add(panelHeading);
        if(Strings.isNullOrEmpty(groupName)) {
            panelHeading.setVisibilityAllowed(false);
        } else {
            panelHeading.add(new Label(ID_MEMBER_GROUP_NAME, groupName));
            final List<LinkAndLabel> actionsPanel = LinkAndLabel
                    .positioned(memberGroupActions, ActionLayout.Position.PANEL);
            final List<LinkAndLabel> actionsPanelDropDown = LinkAndLabel
                    .positioned(memberGroupActions, ActionLayout.Position.PANEL_DROPDOWN);

            AdditionalLinksPanel.addAdditionalLinks(
                    panelHeading, ID_ASSOCIATED_ACTION_LINKS_PANEL,
                    actionsPanel,
                    AdditionalLinksPanel.Style.INLINE_LIST);
            AdditionalLinksPanel.addAdditionalLinks(
                    panelHeading, ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN,
                    actionsPanelDropDown,
                    AdditionalLinksPanel.Style.DROPDOWN);

        }

        // either add the built content, or hide entire
        if(!visible) {
            Components.permanentlyHide(this, div.getId());
        } else {
            this.add(div);
        }
    }

    private void addPropertyToForm(
            final EntityModel entityModel,
            final OneToOneAssociation otoa,
            final WebMarkupContainer container,
            final List<LinkAndLabel> entityActions) {

        final PropertyMemento pm = new PropertyMemento(otoa, entityModel.getIsisSessionFactory());

        final ScalarModel scalarModel = entityModel.getPropertyModel(pm);
        scalarModel.setFormExecutor(new PropertyEditFormExecutor(scalarModel));

        getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ID_PROPERTY, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);

        final ObjectAdapter adapter = entityModel.load(AdapterManager.ConcurrencyChecking.NO_CHECK);
        final List<ObjectAction> associatedActions =
                ObjectAction.Util.findForAssociation(adapter, otoa, getDeploymentCategory());

        entityActions.addAll(
                LinkAndLabelUtil.asActionLinksForAdditionalLinksPanel(entityModel, associatedActions, null));
    }

    private boolean visible = false;
    @Override
    public boolean isVisible() {
        return visible;
    }

}
