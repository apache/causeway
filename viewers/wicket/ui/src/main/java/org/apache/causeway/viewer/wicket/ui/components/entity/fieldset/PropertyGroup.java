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
package org.apache.causeway.viewer.wicket.ui.components.entity.fieldset;

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
import org.apache.causeway.viewer.wicket.model.links.LinkAndLabel;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.causeway.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.causeway.viewer.wicket.ui.components.pops.PopPanelAbstract;
import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

import lombok.val;

public class PropertyGroup extends PanelAbstract<ManagedObject, UiObjectWkt>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;
    private static final String ID_MEMBER_GROUP = "memberGroup";
    private static final String ID_MEMBER_GROUP_NAME = "memberGroupName";

    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL = "associatedActionLinksPanel";
    private static final String ID_ASSOCIATED_ACTION_LINKS_PANEL_DROPDOWN = "associatedActionLinksPanelDropDown";

    private static final String ID_PROPERTIES = "properties";
    private static final String ID_PROPERTY = "property";

    private final FieldSet fieldSet;
    private final Can<PopPanelAbstract> childPopPanels;
    private final List<Component> childComponents;

    public PropertyGroup(final String id, final UiObjectWkt model, final FieldSet fieldSet) {
        super(id, model);
        this.fieldSet = fieldSet;

        // the UI is only ever built once.
        childComponents = buildGui();
        childPopPanels =
                _NullSafe.stream(childComponents)
                .filter(PopPanelAbstract.class::isInstance)
                .map(PopPanelAbstract.class::cast)
                .collect(Can.toCan());

    }

    @Override
    public UiObjectWkt getModel() {
        return (UiObjectWkt) getDefaultModel();
    }

    @Override
    public void onConfigure() {
        for (final PopPanelAbstract childComponent : childPopPanels) {
            childComponent.configure();
        }
        super.onConfigure();
    }

    @Override
    public boolean isVisible() {
        return super.isVisible()
            && isVisibleBasedOnContent();
    }

    @Override
    public boolean isVisibleBasedOnContent() {

        // HACK: there are some components that are not PopPanelAbstract2's, eg the pdfjsviewer.
        // In this case, don't ever hide.

        // TODO: should remove this hack.  We need some sort of SPI for PopPanelAbstract2's and any other component,
        // (eg PdfJsViewer) that can implement.  It's "probably" just a matter of having PdfJsViewer do its work in the
        // correct Wicket callback (probably onConfigure).
        if(childComponents.size() > childPopPanels.size()) {
            return true;
        }
        // HACK:END

        for (final PopPanelAbstract childComponent : childPopPanels) {
            if(childComponent.isVisibilityAllowed()) {
                return true;
            }
        }
        return false;
    }

    // -- HELPER

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
            WktComponents.permanentlyHide(this, div.getId());
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
        // or not moves to PopPanelAbstract2#onConfigure(...)
        //

        return _NullSafe.stream(propertyLayouts)
        .filter(propertyLayoutData -> propertyLayoutData.getMetadataError() == null)
        .map(propertyLayoutData ->
            entity.getSpecification().getProperty(propertyLayoutData.getId())
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
            final UiObjectWkt entityModel,
            final OneToOneAssociation property,
            final WebMarkupContainer container,
            final Consumer<LinkAndLabel> onAssociatedAction) {

        final PopModel popModel =
                entityModel.getPropertyModel(property, ViewOrEditMode.VIEWING, RenderingHint.REGULAR);

        final Component scalarNameAndValueComponent = getComponentFactoryRegistry()
                .addOrReplaceComponent(container, ID_PROPERTY, UiComponentType.POP_NAME_AND_VALUE, popModel);
//XXX[CAUSEWAY-3026] this is a bad idea
//        if(scalarNameAndValueComponent instanceof MarkupContainer) {
//            Wkt.cssAppend(scalarNameAndValueComponent, popModel.getIdentifier());
//        }

        val entity = entityModel.getManagedObject();

        ObjectAction.Util.findForAssociation(entity, property)
        .map(LinkAndLabelFactory.forEntity(entityModel))
        .forEach(onAssociatedAction);

        return scalarNameAndValueComponent;
    }

}
