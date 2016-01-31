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

package org.apache.isis.viewer.wicket.ui.components.entity.tabbed;

import java.util.List;

import com.google.common.collect.FluentIterable;

import org.apache.isis.applib.layout.fixedcols.FCColumn;
import org.apache.isis.applib.layout.fixedcols.FCPage;
import org.apache.isis.applib.layout.fixedcols.FCTabGroup;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.layoutmetadata.PageFacet;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.entity.column.EntityColumn;
import org.apache.isis.viewer.wicket.ui.components.entity.tabgrouplist.TabGroupListPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * {@link PanelAbstract Panel} to represent an entity on a single page made up
 * of several &lt;div&gt; regions.
 */
public class EntityTabbedPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_LEFT_COLUMN = "leftColumn";
    private static final String ID_MIDDLE_COLUMN = "middleColumn";
    private static final String ID_RIGHT_COLUMN = "rightColumn";

    public EntityTabbedPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        buildGui();
    }

    private void buildGui() {
        final EntityModel model = getModel();
        final ObjectAdapter objectAdapter = model.getObject();
        final CssClassFacet facet = objectAdapter.getSpecification().getFacet(CssClassFacet.class);
        if(facet != null) {
            final String cssClass = facet.cssClass(objectAdapter);
            CssClassAppender.appendCssClassTo(this, cssClass);
        }

        // forces metadata to be derived && synced
        final PageFacet PageFacet = model.getTypeOfSpecification().getFacet(PageFacet.class);
        final FCPage page = (FCPage) PageFacet.getPage();


        addOrReplace(ComponentType.ENTITY_SUMMARY, model);

        final int leftSpan = addColumnIfRequired(ID_LEFT_COLUMN, page.getLeft());

        final TabGroupListPanel middleTabs = addTabGroups(ID_MIDDLE_COLUMN, page.getTabGroups());

        final int rightSpan = addColumnIfRequired(ID_RIGHT_COLUMN, page.getRight());

        final int columnSpans = leftSpan + rightSpan;
        int tabGroupSpan = columnSpans < 12 ? 12 - (columnSpans) : 12;
        CssClassAppender.appendCssClassTo(middleTabs, "col-xs-" + tabGroupSpan);

    }

    private TabGroupListPanel addTabGroups(
            final String id, final List<FCTabGroup> tabGroupList) {
        final EntityModel model = getModel();
        final List<FCTabGroup> tabGroups = FluentIterable
                .from(tabGroupList)
                .filter(FCTabGroup.Predicates.notEmpty())
                .toList();
        final EntityModel entityModelWitHints = model.cloneWithTabGroupListMetadata(tabGroups);
        final TabGroupListPanel middleComponent = new TabGroupListPanel(id, entityModelWitHints);
        addOrReplace(middleComponent);
        return middleComponent;
    }

    private int addColumnIfRequired(final String id, final FCColumn col) {
        if(col != null) {
            final EntityModel entityModel =
                    getModel().cloneWithColumnMetadata(col);
            final int span = entityModel.getFCColumn().getSpan();
            if(span > 0) {
                final EntityColumn entityColumn = new EntityColumn(id, entityModel);
                addOrReplace(entityColumn);
                CssClassAppender.appendCssClassTo(entityColumn, "col-xs-" + span);
                return span;
            }
        }
        permanentlyHide(id);
        return 0;
    }

}
