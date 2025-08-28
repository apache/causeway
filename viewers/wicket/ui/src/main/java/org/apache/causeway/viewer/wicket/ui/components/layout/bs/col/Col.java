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
package org.apache.causeway.viewer.wicket.ui.components.layout.bs.col;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.Model;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.entityactions.ActionLinksPanel;
import org.apache.causeway.viewer.wicket.ui.components.collection.parented.ParentedCollectionPanelFactory.CollectionOwnerAndLayout;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.row.Row;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.tabs.TabGroupPanel;
import org.apache.causeway.viewer.wicket.ui.components.object.fieldset.PropertyGroup;
import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

public class Col
extends PanelAbstract<ManagedObject, UiObjectWkt>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_COL = "col";
    private static final String ID_OBJECT_HEADER_PANEL = "objectHeaderPanel";
    private static final String ID_ROWS = "rows";
    private static final String ID_TAB_GROUPS = "tabGroups";
    private static final String ID_FIELD_SETS = "fieldSets";
    private static final String ID_COLLECTIONS = "collections";

    private final BSCol bsCol;

    public Col(
            final String id,
            final UiObjectWkt objectModel, final BSCol bsCol) {

        super(id, objectModel);

        this.bsCol = bsCol;

        buildGui();
    }

    private void buildGui() {

        setRenderBodyOnly(true);

        if(bsCol.getSpan() == 0) {
            WktComponents.permanentlyHide(this, ID_COL);
            return;
        }

        final WebMarkupContainer div = new WebMarkupContainer(ID_COL);

        Wkt.cssAppend(div, bsCol.toCssClass());

        // icon/title
        final DomainObjectLayoutData domainObject = bsCol.getDomainObject();

        final WebMarkupContainer actionOwner;
        final String actionIdToUse;
        final String actionIdToHide;
        if(domainObject != null) {
            var entityHeaderPanel = new WebMarkupContainer(ID_OBJECT_HEADER_PANEL);
            div.add(entityHeaderPanel);
            final Component component = getComponentFactoryRegistry()
                .createComponent(UiComponentType.OBJECT_ICON_TITLE_AND_COPYLINK, getModel());
            entityHeaderPanel.addOrReplace(component);

            actionOwner = entityHeaderPanel;
            actionIdToUse = "objectActions";
            actionIdToHide = "actions";

            visible = true;
        } else {
            WktComponents.permanentlyHide(div, ID_OBJECT_HEADER_PANEL);
            actionOwner = div;
            actionIdToUse = "actions";
            actionIdToHide = null;
        }

        // actions
        // (rendering depends on whether also showing the icon/title)
        final List<ActionLayoutData> actionLayoutDataList = bsCol.getActions();

        var visibleActions = _NullSafe.stream(actionLayoutDataList)
        .filter(actionLayoutData -> actionLayoutData.getMetadataError() == null)
        .filter(_NullSafe::isPresent)
        .map(actionLayoutData ->
            getModel().getTypeOfSpecification().getAction(actionLayoutData.getId()).orElse(null)
        )
        .filter(_NullSafe::isPresent)
        .map(act->ActionModel.forEntity(act, getModel()))
        .collect(Can.toCan());

        if (!visibleActions.isEmpty()) {
            ActionLinksPanel.addActionLinks(actionOwner, actionIdToUse, visibleActions, ActionLinksPanel.Style.INLINE_LIST);
            visible = true;
        } else {
            WktComponents.permanentlyHide(actionOwner, actionIdToUse);
        }
        if(actionIdToHide != null) {
            WktComponents.permanentlyHide(div, actionIdToHide);
        }

        // rows
        final List<BSRow> rows = _Lists.newArrayList(this.bsCol.getRows());
        if(!rows.isEmpty()) {
            final RepeatingViewWithDynamicallyVisibleContent rowsRv = buildRows(ID_ROWS, rows);
            div.add(rowsRv);
            visible = visible || rowsRv.isVisible();
        } else {
            WktComponents.permanentlyHide(div, ID_ROWS);
        }

        // tab groups
        final List<BSTabGroup> tabGroupsWithNonEmptyTabs =
                _NullSafe.stream(bsCol.getTabGroups())
                .filter(_NullSafe::isPresent)
                .filter(bsTabGroup ->
                        _NullSafe.stream(bsTabGroup.getTabs())
                                .anyMatch(BSTab.Predicates.notEmpty())
                )
                .collect(Collectors.toList());

        if(!tabGroupsWithNonEmptyTabs.isEmpty()) {
            final RepeatingViewWithDynamicallyVisibleContent tabGroupRv =
                    new RepeatingViewWithDynamicallyVisibleContent(ID_TAB_GROUPS);

            for (BSTabGroup bsTabGroup : tabGroupsWithNonEmptyTabs) {

                final String id = tabGroupRv.newChildId();
                final List<BSTab> tabs = _NullSafe.stream(bsTabGroup.getTabs())
                        .filter(BSTab.Predicates.notEmpty())
                        .collect(Collectors.toList());

                switch (tabs.size()) {
                case 0:
                    // shouldn't occur; previously have filtered these out
                    throw new IllegalStateException("Cannot render tabGroup with no tabs");
                case 1:
                    if(bsTabGroup.isCollapseIfOne() == null || bsTabGroup.isCollapseIfOne()) {
                        final BSTab bsTab = tabs.get(0);
                        // render the rows of the one-and-only tab of this tab group.
                        final List<BSRow> tabRows = bsTab.getRows();
                        final RepeatingViewWithDynamicallyVisibleContent rowsRv = buildRows(id, tabRows);
                        tabGroupRv.add(rowsRv);
                        break;
                    }
                    // else fall through...
                default:
                    final WebMarkupContainer tabGroup = new TabGroupPanel(id, getModel(), bsTabGroup);

                    tabGroupRv.add(tabGroup);
                    break;
                }

            }
            div.add(tabGroupRv);
            visible = visible || tabGroupRv.isVisible();
        } else {
            WktComponents.permanentlyHide(div, ID_TAB_GROUPS);
        }

        // fieldsets
        final List<FieldSet> fieldSetsWithProperties =
                _NullSafe.stream(bsCol.getFieldSets())
                .filter(_NullSafe::isPresent)
                .filter(fieldSet -> ! _NullSafe.isEmpty(fieldSet.getProperties()))
                .collect(Collectors.toList());

        if(!fieldSetsWithProperties.isEmpty()) {
            final RepeatingViewWithDynamicallyVisibleContent fieldSetRv =
                    new RepeatingViewWithDynamicallyVisibleContent(ID_FIELD_SETS);

            for (FieldSet fieldSet : fieldSetsWithProperties) {

                final String id = fieldSetRv.newChildId();

                final PropertyGroup propertyGroup = new PropertyGroup(id, getModel(), fieldSet);
                fieldSetRv.add(propertyGroup);
            }
            div.add(fieldSetRv);
            visible = visible || fieldSetRv.isVisible();
        } else {
            WktComponents.permanentlyHide(div, ID_FIELD_SETS);
        }

        final UiObjectWkt objectModel = getModel();
        var ownerSpec = objectModel.getManagedObject().objSpec();

        // collection layout data by collection id (the collection's member-id)
        // preserving order, as order matters
        var collectionLayoutById = new LinkedHashMap<String, CollectionLayoutData>();
        _NullSafe.stream(bsCol.getCollections())
            .filter(colLayoutData->colLayoutData.getMetadataError() == null)
            .filter(colLayoutData->!ownerSpec.getCollection(colLayoutData.getId()).isEmpty())
            .forEach(colLayoutData->collectionLayoutById.put(colLayoutData.getId(), colLayoutData));

        if(!collectionLayoutById.isEmpty()) {
            final RepeatingViewWithDynamicallyVisibleContent collectionRv =
                    new RepeatingViewWithDynamicallyVisibleContent(ID_COLLECTIONS);

            collectionLayoutById.forEach((id, layout)->{

                var helperModel = Model.of(new CollectionOwnerAndLayout(objectModel, layout));

                final ComponentFactory componentFactory =
                        getComponentFactoryRegistry().findComponentFactory(
                                UiComponentType.PARENTED_COLLECTION, helperModel);

                final Component collectionPanel = componentFactory.createComponent(collectionRv.newChildId(), helperModel);
                collectionRv.add(collectionPanel);
            });

            div.add(collectionRv);
            visible = visible || collectionRv.isVisible();

        } else {
            WktComponents.permanentlyHide(div, ID_COLLECTIONS);
        }

        final WebMarkupContainer panel = this;
        if(visible) {
            panel.add(div);
        } else {
            WktComponents.permanentlyHide(panel, div.getId());
        }

    }

    private RepeatingViewWithDynamicallyVisibleContent buildRows(final String owningId, final List<BSRow> rows) {
        final RepeatingViewWithDynamicallyVisibleContent rowRv =
                new RepeatingViewWithDynamicallyVisibleContent(owningId);

        for(final BSRow bsRow: rows) {
            final String id = rowRv.newChildId();
            final Row row = new Row(id, getModel(), bsRow);
            rowRv.add(row);
        }
        return rowRv;
    }

    private boolean visible = false;
    @Override
    public boolean isVisible() {
        return visible;
    }

}
