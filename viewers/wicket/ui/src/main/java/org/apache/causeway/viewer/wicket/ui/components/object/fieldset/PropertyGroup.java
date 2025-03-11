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
package org.apache.causeway.viewer.wicket.ui.components.object.fieldset;

import java.util.List;
import java.util.function.Consumer;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.entityactions.ActionLinksPanel;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributePanel;
import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

public class PropertyGroup extends PanelAbstract<ManagedObject, UiObjectWkt> implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;
    private static final String ID_MEMBER_GROUP = "memberGroup";
    private static final String ID_MEMBER_GROUP_NAME = "memberGroupName";

    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL = "associatedActionLinksPanel";
    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN = "associatedActionLinksPanelDropDown";

    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";

    private final FieldSet fieldSet;
    private final Can<AttributePanel> childScalarPanels;
    private final List<Component> childComponents;

    public PropertyGroup(final String id, final UiObjectWkt model, final FieldSet fieldSet) {
        super(id, model);
        this.fieldSet = fieldSet;

        // the UI is only ever built once.
        childComponents = buildGui();
        childScalarPanels =
                _NullSafe.stream(childComponents)
                .filter(AttributePanel.class::isInstance)
                .map(AttributePanel.class::cast)
                .collect(Can.toCan());

    }

    @Override
    public UiObjectWkt getModel() {
        return (UiObjectWkt) getDefaultModel();
    }

    private List<Component> buildGui() {

        final List<Component> childComponents = _Lists.newArrayList();

        setOutputMarkupPlaceholderTag(true);
        setOutputMarkupId(true);

        final WebMarkupContainer div = new WebMarkupContainer(ID_MEMBER_GROUP);
        div.setMarkupId("fieldSet-" + fieldSet.getId());

        final RepeatingView propertyRv = new RepeatingView(ID_PROPERTIES);
        div.addOrReplace(propertyRv);

        var properties = getPropertiesNotStaticallyHidden();

        var memberGroupActions = collectMemberGroupActions(propertyRv, childComponents::add);

        final WebMarkupContainer panelHeading = new WebMarkupContainer("panelHeading");
        div.addOrReplace(panelHeading);
        if(_Strings.isNullOrEmpty(fieldSet.getName())) {
            panelHeading.setVisibilityAllowed(false);
        } else {
            Wkt.labelAdd(panelHeading, ID_MEMBER_GROUP_NAME, fieldSet.getName());

            ActionLinksPanel.addActionLinks(
                    panelHeading, ID_ASSOCIATED_ACTION_LINKS_PANEL,
                    memberGroupActions
                        .filter(ActionModel.isPositionedAt(ActionLayout.Position.PANEL)),
                    ActionLinksPanel.Style.INLINE_LIST);

            ActionLinksPanel.addActionLinks(
                    panelHeading, ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN,
                    memberGroupActions
                        .filter(ActionModel.isPositionedAt(ActionLayout.Position.PANEL_DROPDOWN)),
                    ActionLinksPanel.Style.DROPDOWN);
        }

        // either add the built content, or hide entire
        if(properties.isEmpty()) {
            WktComponents.permanentlyHide(this, div.getId());
        } else {
            this.addOrReplace(div);
        }

        return childComponents;
    }

    private Can<ActionModel> collectMemberGroupActions(
            final RepeatingView container,
            final Consumer<Component> onNewChildComponent) {

        var memberGroupActionList = _Lists.<ActionModel>newArrayList();

        for (var property : getPropertiesNotStaticallyHidden()) {
            var propertyRvContainer = new WebMarkupContainer(container.newChildId());
            container.addOrReplace(propertyRvContainer);
            onNewChildComponent.accept(
                    addPropertyToForm(getModel(), property, propertyRvContainer, memberGroupActionList::add));
        }

        return Can.ofCollection(memberGroupActionList);
    }

    private Can<OneToOneAssociation> getPropertiesNotStaticallyHidden() {

        var entity = getModel().getManagedObject();
        var propertyLayouts = this.fieldSet.getProperties();
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
            entity.objSpec().getProperty(propertyLayoutData.getId())
            .orElse(null)
        )
        .filter(_NullSafe::isPresent)
        .filter(
            Facets.hiddenWhereMatches(where->
                // static invisible.
                where.isAlways()
                        || where == Where.OBJECT_FORMS)
            .negate()
        )
        .collect(Can.toCan());
    }

    private Component addPropertyToForm(
            final UiObjectWkt objectModel,
            final OneToOneAssociation property,
            final WebMarkupContainer container,
            final Consumer<ActionModel> onAssociatedAction) {

        final UiAttributeWkt attributeModel =
                objectModel.getPropertyModel(property, ViewOrEditMode.VIEWING, RenderingHint.REGULAR);

        final Component scalarNameAndValueComponent = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ID_PROPERTY, UiComponentType.ATTRIBUTE_NAME_AND_VALUE, attributeModel);
//XXX[CAUSEWAY-3026] this is a bad idea
//        if(scalarNameAndValueComponent instanceof MarkupContainer) {
//            Wkt.cssAppend(scalarNameAndValueComponent, attributeModel.getIdentifier());
//        }

        var entity = objectModel.getManagedObject();

        ObjectAction.Util.findForAssociation(entity, property)
        .map(act->ActionModel.forEntity(act, objectModel))
        .forEach(onAssociatedAction);

        return scalarNameAndValueComponent;
    }

    @Override
    public void onConfigure() {
        for (final AttributePanel childComponent : childScalarPanels) {
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

        for (final AttributePanel childComponent : childScalarPanels) {
            if(childComponent.isVisibilityAllowed()) {
                return true;
            }
        }
        return false;
    }

}
