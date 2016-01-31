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

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.layout.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.bootstrap3.BS3TabGroup;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.DomainObjectLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.entity.collection.EntityCollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.entity.fieldset.PropertyGroup;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.Util;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.tabs.TabGroupPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public class Col extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_COL = "col";

    private final BS3Col bs3Col;

    public Col(
            final String id,
            final EntityModel entityModel) {

        super(id, entityModel);

        bs3Col = (BS3Col) entityModel.getLayoutMetadata();

        buildGui();
    }

    private void buildGui() {

        setRenderBodyOnly(true);

        final WebMarkupContainer div = new WebMarkupContainer(ID_COL);

        CssClassAppender.appendCssClassTo(div, bs3Col.toCssClass());
        Util.appendCssClass(div, bs3Col, "col");

        this.addOrReplace(div);

        // icon/title
        final DomainObjectLayoutData domainObject = bs3Col.getDomainObject();

        final WebMarkupContainer actionOwner;
        final String actionIdToUse;
        final String actionIdToHide;
        if(domainObject != null) {
            final WebMarkupContainer entityHeaderPanel = new WebMarkupContainer("entityHeaderPanel");
            div.add(entityHeaderPanel);
            final ComponentFactory componentFactory =
                    getComponentFactoryRegistry().findComponentFactory(ComponentType.ENTITY_ICON_TITLE_AND_COPYLINK, getModel());
            final Component component = componentFactory.createComponent(getModel());
            entityHeaderPanel.addOrReplace(component);

            actionOwner = entityHeaderPanel;
            actionIdToUse = "entityActions";
            actionIdToHide = "actions";
        } else {
            Components.permanentlyHide(div, "entityHeaderPanel");
            actionOwner = div;
            actionIdToUse = "actions";
            actionIdToHide = null;
        }


        // actions
        // (rendering depends on whether also showing the icon/title)
        final List<ActionLayoutData> actionLayoutDatas = bs3Col.getActions();
            final List<ObjectAction> objectActions =
                FluentIterable.from(actionLayoutDatas)
                        .transform(new Function<ActionLayoutData, ObjectAction>() {
                            @Nullable @Override public ObjectAction apply(@Nullable final ActionLayoutData actionLayoutData) {
                                return getModel().getTypeOfSpecification().getObjectAction(actionLayoutData.getId());
                            }
                        })
                        .filter(Predicates.<ObjectAction>notNull())
                        .toList();
        final List<LinkAndLabel> entityActionLinks = EntityActionUtil.asLinkAndLabelsForAdditionalLinksPanel(getModel(), objectActions);

        if(!entityActionLinks.isEmpty()) {
            AdditionalLinksPanel.addAdditionalLinks(actionOwner, actionIdToUse, entityActionLinks, AdditionalLinksPanel.Style.INLINE_LIST);
        } else {
            Components.permanentlyHide(actionOwner, actionIdToUse);
        }
        if(actionIdToHide != null) {
            Components.permanentlyHide(div, actionIdToHide);
        }



        // tab groups
        final List<BS3TabGroup> tabGroups = bs3Col.getTabGroups();
        if(!tabGroups.isEmpty()) {
            final RepeatingView rv = new RepeatingView("tabGroups");

            for (BS3TabGroup bs3TabGroup : tabGroups) {

                final String id = rv.newChildId();
                final EntityModel entityModelWithHints = getModel().cloneWithLayoutMetadata(bs3TabGroup);

                final WebMarkupContainer tabGroup = new TabGroupPanel(id, entityModelWithHints);

                rv.add(tabGroup);
            }
            div.add(rv);
        } else {
            Components.permanentlyHide(div, "tabGroups");
        }



        // fieldsets
        final List<FieldSet> fieldSets = bs3Col.getFieldSets();
        if(!fieldSets.isEmpty()) {
            final RepeatingView rv = new RepeatingView("fieldSets");

            for (FieldSet fieldSet : fieldSets) {

                final String id = rv.newChildId();
                final EntityModel entityModelWithHints = getModel().cloneWithLayoutMetadata(fieldSet);

                final WebMarkupContainer propertyGroup = new PropertyGroup(id, entityModelWithHints);
                rv.add(propertyGroup);
            }
            div.add(rv);
        } else {
            Components.permanentlyHide(div, "fieldSets");
        }



        // collections
        final List<CollectionLayoutData> collections = bs3Col.getCollections();
        if(!collections.isEmpty()) {
            final RepeatingView rv = new RepeatingView("collections");

            for (CollectionLayoutData collection : collections) {

                final String id = rv.newChildId();
                final EntityModel entityModelWithHints = getModel().cloneWithLayoutMetadata(collection);

                final WebMarkupContainer collectionPanel = new EntityCollectionPanel(id, entityModelWithHints);
                rv.add(collectionPanel);
            }
            div.add(rv);
        } else {
            Components.permanentlyHide(div, "collections");
        }

    }



    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////

    protected DeploymentType getDeploymentType() {
        return IsisContext.getDeploymentType();
    }

}
