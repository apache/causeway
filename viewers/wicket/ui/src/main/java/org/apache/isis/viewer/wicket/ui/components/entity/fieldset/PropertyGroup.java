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
import java.util.function.Consumer;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

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
    private final Can<ScalarPanelAbstract> childScalarPanels;
    private final List<Component> childComponents;

    public PropertyGroup(final String id, final EntityModel model, final FieldSet fieldSet) {
        super(id, model);
        this.fieldSet = fieldSet;

        // the UI is only ever built once.
        childComponents = buildGui();
        childScalarPanels =
                _NullSafe.stream(childComponents)
                .filter(ScalarPanelAbstract.class::isInstance)
                .map(ScalarPanelAbstract.class::cast)
                .collect(Can.toCan());

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

        final RepeatingView propertyRv = new RepeatingView(ID_PROPERTIES);
        div.addOrReplace(propertyRv);

        val properties = getPropertiesNotStaticallyHidden();

        val memberGroupActions = collectMemberGroupActions(propertyRv, childComponents::add);

        final WebMarkupContainer panelHeading = new WebMarkupContainer("panelHeading");
        div.addOrReplace(panelHeading);
        if(_Strings.isNullOrEmpty(fieldSet.getName())) {
            panelHeading.setVisibilityAllowed(false);
        } else {
            Wkt.labelAdd(panelHeading, ID_MEMBER_GROUP_NAME, fieldSet.getName());

            AdditionalLinksPanel.addAdditionalLinks(
                    panelHeading, ID_ASSOCIATED_ACTION_LINKS_PANEL,
                    memberGroupActions
                        .filter(LinkAndLabel.isPositionedAt(ActionLayout.Position.PANEL)),
                    AdditionalLinksPanel.Style.INLINE_LIST);

            AdditionalLinksPanel.addAdditionalLinks(
                    panelHeading, ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN,
                    memberGroupActions
                        .filter(LinkAndLabel.isPositionedAt(ActionLayout.Position.PANEL_DROPDOWN)),
                    AdditionalLinksPanel.Style.DROPDOWN);
        }

        // either add the built content, or hide entire
        if(properties.isEmpty()) {
            Components.permanentlyHide(this, div.getId());
        } else {
            this.addOrReplace(div);
        }

        return childComponents;
    }

    private Can<LinkAndLabel> collectMemberGroupActions(
            final RepeatingView container,
            final Consumer<Component> onNewChildComponent) {

        val memberGroupActionList = _Lists.<LinkAndLabel>newArrayList();

        for (val property : getPropertiesNotStaticallyHidden()) {
            val propertyRvContainer = new WebMarkupContainer(container.newChildId());
            container.addOrReplace(propertyRvContainer);
            onNewChildComponent.accept(
                    addPropertyToForm(getModel(), property, propertyRvContainer, memberGroupActionList::add));
        }

        return Can.ofCollection(memberGroupActionList);
    }

    private Can<OneToOneAssociation> getPropertiesNotStaticallyHidden() {

        val entity = getModel().getManagedObject();
        val propertyLayouts = this.fieldSet.getProperties();
        //
        // previously we filtered out any invisible properties.
        // However, the inline prompt/don't redirect logic introduced in 1.15.0 means that we keep the same page,
        // and it may be that individual properties start out as invisible but then become visible later.
        //
        // therefore the responsibility of determining whether an individual property's component should be visible
        // or not moves to ScalarPanelAbstract2#onConfigure(...)
        //

        return _NullSafe.stream(propertyLayouts)
        .filter(propertyLayoutData -> propertyLayoutData.getMetadataError() == null)
        .map(propertyLayoutData ->
            entity.getSpecification().getProperty(propertyLayoutData.getId())
            .orElse(null)
        )
        .filter(_NullSafe::isPresent)
        .filter(property -> {
            val hiddenFacet = property.getFacet(HiddenFacet.class);
            if(hiddenFacet != null) {
                // static invisible.
                if(hiddenFacet.where().isAlways()
                        || hiddenFacet.where() == Where.OBJECT_FORMS) {
                    return false;
                }
            }
            return true;
        })
        .collect(Can.toCan());
    }

    private Component addPropertyToForm(
            final EntityModel entityModel,
            final OneToOneAssociation property,
            final WebMarkupContainer container,
            final Consumer<LinkAndLabel> onAssociatedAction) {

        final ScalarModel scalarModel =
                entityModel.getPropertyModel(property, ScalarRepresentation.VIEWING, EntityModel.RenderingHint.REGULAR);

        final Component scalarNameAndValueComponent = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ID_PROPERTY, ComponentType.SCALAR_NAME_AND_VALUE, scalarModel);
        if(scalarNameAndValueComponent instanceof MarkupContainer) {
            Wkt.cssAppend(scalarNameAndValueComponent, scalarModel.getIdentifier());
        }

        val entity = entityModel.getManagedObject();

        ObjectAction.Util.findForAssociation(entity, property)
        .map(LinkAndLabelFactory.forEntity(entityModel))
        .forEach(onAssociatedAction);

        return scalarNameAndValueComponent;
    }

    @Override
    public void onConfigure() {
        for (final ScalarPanelAbstract childComponent : childScalarPanels) {
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
        if(childComponents.size() > childScalarPanels.size()) {
            return true;
        }
        // HACK:END

        for (final ScalarPanelAbstract childComponent : childScalarPanels) {
            if(childComponent.isVisibilityAllowed()) {
                return true;
            }
        }
        return false;
    }

}
