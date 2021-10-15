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
package org.apache.isis.viewer.wicket.ui.components.layout.bs3.col;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3TabGroup;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.fieldset.PropertyGroup;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.row.Row;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.tabs.TabGroupPanel;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import lombok.val;

public class Col
extends PanelAbstract<ManagedObject, EntityModel>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_COL = "col";
    private static final String ID_ENTITY_HEADER_PANEL = "entityHeaderPanel";
    private static final String ID_ROWS = "rows";
    private static final String ID_TAB_GROUPS = "tabGroups";
    private static final String ID_FIELD_SETS = "fieldSets";
    private static final String ID_COLLECTIONS = "collections";

    private final BS3Col bs3Col;

    public Col(
            final String id,
            final EntityModel entityModel, final BS3Col bs3Col) {

        super(id, entityModel);

        this.bs3Col = bs3Col;

        buildGui();
    }

    private void buildGui() {

        setRenderBodyOnly(true);

        if(bs3Col.getSpan() == 0) {
            Components.permanentlyHide(this, ID_COL);
            return;
        }

        final WebMarkupContainer div = new WebMarkupContainer(ID_COL);

        CssClassAppender.appendCssClassTo(div, bs3Col.toCssClass());

        // icon/title
        final DomainObjectLayoutData domainObject = bs3Col.getDomainObject();

        final WebMarkupContainer actionOwner;
        final String actionIdToUse;
        final String actionIdToHide;
        if(domainObject != null) {
            final WebMarkupContainer entityHeaderPanel = new WebMarkupContainer(ID_ENTITY_HEADER_PANEL);
            div.add(entityHeaderPanel);
            final ComponentFactory componentFactory =
                    getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_TITLE_AND_COPYLINK, getModel());
            final Component component = componentFactory.createComponent(getModel());
            entityHeaderPanel.addOrReplace(component);

            actionOwner = entityHeaderPanel;
            actionIdToUse = "entityActions";
            actionIdToHide = "actions";

            visible = true;
        } else {
            Components.permanentlyHide(div, ID_ENTITY_HEADER_PANEL);
            actionOwner = div;
            actionIdToUse = "actions";
            actionIdToHide = null;
        }


        // actions
        // (rendering depends on whether also showing the icon/title)
        final List<ActionLayoutData> actionLayoutDataList = bs3Col.getActions();

        val visibleActions = _NullSafe.stream(actionLayoutDataList)
        .filter(actionLayoutData -> actionLayoutData.getMetadataError() == null)
        .filter(_NullSafe::isPresent)
        .map(actionLayoutData ->
            getModel().getTypeOfSpecification().getAction(actionLayoutData.getId()).orElse(null)
        )
        .filter(_NullSafe::isPresent)
        .map(LinkAndLabelFactory.forEntity(getModel()))
        .collect(Can.toCan());

        if (!visibleActions.isEmpty()) {
            AdditionalLinksPanel.addAdditionalLinks(actionOwner, actionIdToUse, visibleActions, AdditionalLinksPanel.Style.INLINE_LIST);
            visible = true;
        } else {
            Components.permanentlyHide(actionOwner, actionIdToUse);
        }
        if(actionIdToHide != null) {
            Components.permanentlyHide(div, actionIdToHide);
        }


        // rows
        final List<BS3Row> rows = _Lists.newArrayList(this.bs3Col.getRows());
        if(!rows.isEmpty()) {
            final RepeatingViewWithDynamicallyVisibleContent rowsRv = buildRows(ID_ROWS, rows);
            div.add(rowsRv);
            visible = visible || rowsRv.isVisible();
        } else {
            Components.permanentlyHide(div, ID_ROWS);
        }


        // tab groups
        final List<BS3TabGroup> tabGroupsWithNonEmptyTabs =
                _NullSafe.stream(bs3Col.getTabGroups())
                .filter(_NullSafe::isPresent)
                .filter(bs3TabGroup ->
                        _NullSafe.stream(bs3TabGroup.getTabs())
                                .anyMatch(BS3Tab.Predicates.notEmpty())
                )
                .collect(Collectors.toList());

        if(!tabGroupsWithNonEmptyTabs.isEmpty()) {
            final RepeatingViewWithDynamicallyVisibleContent tabGroupRv =
                    new RepeatingViewWithDynamicallyVisibleContent(ID_TAB_GROUPS);

            for (BS3TabGroup bs3TabGroup : tabGroupsWithNonEmptyTabs) {

                final String id = tabGroupRv.newChildId();
                final List<BS3Tab> tabs = _NullSafe.stream(bs3TabGroup.getTabs())
                        .filter(BS3Tab.Predicates.notEmpty())
                        .collect(Collectors.toList());

                switch (tabs.size()) {
                case 0:
                    // shouldn't occur; previously have filtered these out
                    throw new IllegalStateException("Cannot render tabGroup with no tabs");
                case 1:
                    if(bs3TabGroup.isCollapseIfOne() == null || bs3TabGroup.isCollapseIfOne()) {
                        final BS3Tab bs3Tab = tabs.get(0);
                        // render the rows of the one-and-only tab of this tab group.
                        final List<BS3Row> tabRows = bs3Tab.getRows();
                        final RepeatingViewWithDynamicallyVisibleContent rowsRv = buildRows(id, tabRows);
                        tabGroupRv.add(rowsRv);
                        break;
                    }
                    // else fall through...
                default:
                    final WebMarkupContainer tabGroup = new TabGroupPanel(id, getModel(), bs3TabGroup);

                    tabGroupRv.add(tabGroup);
                    break;
                }

            }
            div.add(tabGroupRv);
            visible = visible || tabGroupRv.isVisible();
        } else {
            Components.permanentlyHide(div, ID_TAB_GROUPS);
        }



        // fieldsets
        final List<FieldSet> fieldSetsWithProperties =
                _NullSafe.stream(bs3Col.getFieldSets())
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
            Components.permanentlyHide(div, ID_FIELD_SETS);
        }


        // collections
        final List<CollectionLayoutData> collections =
                _NullSafe.stream(bs3Col.getCollections())
                .filter(
                        new Predicate<CollectionLayoutData>() {
                            @Override
                            public boolean test(final CollectionLayoutData collectionLayoutData) {
                                return collectionLayoutData.getMetadataError() == null;
                            }
                        })
                .collect(Collectors.toList());
        if(!collections.isEmpty()) {
            final RepeatingViewWithDynamicallyVisibleContent collectionRv =
                    new RepeatingViewWithDynamicallyVisibleContent(ID_COLLECTIONS);

            final EntityModel entityModel = getModel();
            final CollectionLayoutData snapshot = entityModel.getCollectionLayoutData();

            for (CollectionLayoutData collection : collections) {

                final String id = collectionRv.newChildId();

                // we successively trample over the layout data; but that's ok, this is synchronous code anyway...
                entityModel.setCollectionLayoutData(collection);

                // the entityModel's getLayoutData() provides the hint as to which collection of the entity to render.
                final ComponentFactory componentFactory =
                        getComponentFactoryRegistry().findComponentFactory(
                                ComponentType.ENTITY_COLLECTION, entityModel);
                final Component collectionPanel = componentFactory.createComponent(id, entityModel);
                collectionRv.add(collectionPanel);
            }
            div.add(collectionRv);
            visible = visible || collectionRv.isVisible();

            //XXX ISIS-1698 restore original state after trampling over
            entityModel.setCollectionLayoutData(snapshot);

        } else {
            Components.permanentlyHide(div, ID_COLLECTIONS);
        }


        final WebMarkupContainer panel = this;
        if(visible) {
            panel.add(div);
        } else {
            Components.permanentlyHide(panel, div.getId());
        }

    }

    private RepeatingViewWithDynamicallyVisibleContent buildRows(final String owningId, final List<BS3Row> rows) {
        final RepeatingViewWithDynamicallyVisibleContent rowRv =
                new RepeatingViewWithDynamicallyVisibleContent(owningId);

        for(final BS3Row bs3Row: rows) {
            final String id = rowRv.newChildId();
            final Row row = new Row(id, getModel(), bs3Row);
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
