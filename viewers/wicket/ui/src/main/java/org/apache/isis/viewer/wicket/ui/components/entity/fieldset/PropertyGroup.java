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

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
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
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

public class PropertyGroup extends PanelAbstract<ManagedObject, EntityModel> implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;
    private static final String ID_MEMBER_GROUP = "memberGroup";
    private static final String ID_MEMBER_GROUP_NAME = "memberGroupName";

    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL = "associatedActionLinksPanel";
    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN = "associatedActionLinksPanelDropDown";

    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";

    private final FieldSet fieldSet;
    private final List<ScalarPanelAbstract> childScalarPanelAbstract2s;
    private final List<Component> childComponents;

    public PropertyGroup(final String id, final EntityModel model, final FieldSet fieldSet) {
        super(id, model);
        this.fieldSet = fieldSet;

        // the UI is only ever built once.
        childComponents = buildGui();
        childScalarPanelAbstract2s =
                _NullSafe.stream(childComponents)
                .filter(ScalarPanelAbstract.class::isInstance)
                .map(ScalarPanelAbstract.class::cast)
                .collect(Collectors.toList());

    }

    @Override
    public EntityModel getModel() {
        return (EntityModel) getDefaultModel();
    }



    private List<Component> buildGui() {

        final List<Component> childComponents = _Lists.newArrayList();

        setOutputMarkupPlaceholderTag(true);
        setOutputMarkupId(true);

        final WebMarkupContainer div = new WebMarkupContainer(ID_MEMBER_GROUP);
        div.setMarkupId("fieldSet-" + fieldSet.getId());

        String groupName = fieldSet.getName();

        val associations = getObjectAssociations();

        final List<LinkAndLabel> memberGroupActions = _Lists.newArrayList();
        final RepeatingView propertyRv = new RepeatingView(ID_PROPERTIES);
        div.addOrReplace(propertyRv);

        for (val association : associations) {
            final WebMarkupContainer propertyRvContainer = new WebMarkupContainer(propertyRv.newChildId());
            propertyRv.addOrReplace(propertyRvContainer);
            final Component component = addPropertyToForm(getModel(), (OneToOneAssociation) association,
                    propertyRvContainer, memberGroupActions::add);
            childComponents.add(component);
        }

        WebMarkupContainer panelHeading = new WebMarkupContainer("panelHeading");
        div.addOrReplace(panelHeading);
        if(_Strings.isNullOrEmpty(groupName)) {
            panelHeading.setVisibilityAllowed(false);
        } else {
            panelHeading.addOrReplace(new Label(ID_MEMBER_GROUP_NAME, groupName));
            final Can<LinkAndLabel> actionsPanel = LinkAndLabel
                    .positioned(ActionLayout.Position.PANEL, memberGroupActions.stream());
            final Can<LinkAndLabel> actionsPanelDropDown = LinkAndLabel
                    .positioned(ActionLayout.Position.PANEL_DROPDOWN, memberGroupActions.stream());
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
        if(associations.isEmpty()) {
            Components.permanentlyHide(this, div.getId());
        } else {
            this.addOrReplace(div);
        }

        return childComponents;
    }

    private List<ObjectAssociation> getObjectAssociations() {
        final List<PropertyLayoutData> properties = this.fieldSet.getProperties();
        // changed to NO_CHECK because more complex BS3 layouts trip concurrency exception
        // (haven't investigated as to why).
        val adapter = getModel().getManagedObject();
        return getObjectAssociations(properties, adapter);
    }

    private List<ObjectAssociation> getObjectAssociations(
            final List<PropertyLayoutData> properties,
            final ManagedObject adapter) {

        //
        // previously we filtered out any invisible properties.
        // However, the inline prompt/don't redirect logic introduced in 1.15.0 means that we keep the same page,
        // and it may be that individual properties start out as invisible but then become visible later.
        //
        // therefore the responsibility of determining whether an individual property's component should be visible
        // or not moves to ScalarPanelAbstract2#onConfigure(...)
        //

        val oas = _NullSafe.stream(properties)
                .filter(propertyLayoutData -> propertyLayoutData.getMetadataError() == null)
                .map(propertyLayoutData ->
                    adapter.getSpecification()
                    .getAssociation(propertyLayoutData.getId())
                    .orElse(null)
                )
                .filter(_NullSafe::isPresent)
                .filter(objectAssociation -> {
                    val hiddenFacet = objectAssociation.getFacet(HiddenFacet.class);
                    if(hiddenFacet != null && !hiddenFacet.isFallback()) {
                        // static invisible.
                        if(hiddenFacet.where() == Where.EVERYWHERE || hiddenFacet.where() == Where.OBJECT_FORMS) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return Collections.unmodifiableList(oas);
    }

    private Component addPropertyToForm(
            final EntityModel entityModel,
            final OneToOneAssociation otoa,
            final WebMarkupContainer container,
            final Consumer<LinkAndLabel> onEntityAction) {

        final PropertyMemento pm = new PropertyMemento(otoa);

        final ScalarModel scalarModel =
                entityModel.getPropertyModel(pm, EntityModel.Mode.VIEW, EntityModel.RenderingHint.REGULAR);

        final Component component = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ID_PROPERTY, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
        if(component instanceof MarkupContainer) {
            String identifier = scalarModel.getIdentifier();
            CssClassAppender.appendCssClassTo((MarkupContainer)component, identifier);
        }

        val adapter = entityModel.getManagedObject();
        val associatedActions = ObjectAction.Util.findForAssociation(adapter, otoa);

        LinkAndLabelUtil.asActionLinksForAdditionalLinksPanel(entityModel, associatedActions, null)
        .forEach(onEntityAction);

        return component;
    }

    @Override
    public void onConfigure() {
        for (final ScalarPanelAbstract childComponent : childScalarPanelAbstract2s) {
            childComponent.configure();
        }
        super.onConfigure();
    }

    @Override
    public boolean isVisible() {

        // HACK: there are some components that are not ScalarPanelAbstract2's, eg the pdfjsviewer.
        // In this case, don't ever hide.

        // TODO: should remove this hack.  We need some sort of SPI for ScalarPanelAbstract2's and any other component,
        // (eg PdfJsViewer) that can implement.  It's "probably" just a matter of having PdfJsViewer do its work in the
        // correct Wicket callback (probably onConfigure).
        if(childComponents.size() > childScalarPanelAbstract2s.size()) {
            return true;
        }
        // HACK:END

        for (final ScalarPanelAbstract childComponent : childScalarPanelAbstract2s) {
            if(childComponent.isVisibilityAllowed()) {
                return true;
            }
        }
        return false;
    }

}
