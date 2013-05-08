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

package org.apache.isis.viewer.wicket.ui.components.scalars.string;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.wicket.Session;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionFilters;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.links.LinksProvider;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldParseableAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.selector.links.LinksSelectorPanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

/**
 * Panel for rendering scalars of type {@link String}.
 */
public class StringPanel extends ScalarPanelTextFieldParseableAbstract {

    private static final long serialVersionUID = 1L;
    private static final String ID_SCALAR_VALUE = "scalarValue";

    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    private static final String ID_ADDITIONAL_LINK_LIST = "additionalLinkList";
    public static final String ID_ADDITIONAL_LINK = "additionalLink";
    private static final String ID_ADDITIONAL_LINK_ITEM = "additionalLinkItem";
    private static final String ID_ADDITIONAL_LINK_TITLE = "additionalLinkTitle";
    
    public StringPanel(final String id, final ScalarModel scalarModel) {
        super(id, ID_SCALAR_VALUE, scalarModel);
    }

    

    @Override
    protected void addSemantics() {
        super.addSemantics();
    }
    
    @Override
    protected FormComponentLabel addComponentForRegular() {
        final FormComponentLabel fcl = super.addComponentForRegular();

        final List<LinkAndLabel> entityActions;
        if(scalarModel.getKind() == ScalarModel.Kind.PROPERTY) {
            final ObjectAdapterMemento parentMemento = scalarModel.getParentObjectAdapterMemento();
            final EntityModel parentEntityModel = new EntityModel(parentMemento);
            entityActions = CollectionPanel.entityActions(parentEntityModel, scalarModel.getPropertyMemento().getProperty());
        } else {
            entityActions = null;
        }
        addAdditionalLinks(fcl, entityActions);

        return fcl;
    }
    
    protected void addAdditionalLinks(FormComponentLabel fcl, List<LinkAndLabel> links) {
        if(links == null || links.isEmpty()) {
            permanentlyHide(ID_ADDITIONAL_LINKS);
            return;
        }
        links = Lists.newArrayList(links); // copy, to serialize any lazy evaluation
        
        final WebMarkupContainer views = new WebMarkupContainer(ID_ADDITIONAL_LINKS);
        
        final WebMarkupContainer container = new WebMarkupContainer(ID_ADDITIONAL_LINK_LIST);
        
        views.addOrReplace(container);
        views.setOutputMarkupId(true);
        
        this.setOutputMarkupId(true);
        
        final ListView<LinkAndLabel> listView = new ListView<LinkAndLabel>(ID_ADDITIONAL_LINK_ITEM, links) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<LinkAndLabel> item) {
                final LinkAndLabel linkAndLabel = item.getModelObject();
                
                final AbstractLink link = linkAndLabel.getLink();
                        
                Label viewTitleLabel = new Label(ID_ADDITIONAL_LINK_TITLE, linkAndLabel.getLabel());
                String disabledReasonIfAny = linkAndLabel.getDisabledReasonIfAny();
                if(disabledReasonIfAny != null) {
                    viewTitleLabel.add(new AttributeAppender("title", disabledReasonIfAny));
                }
                viewTitleLabel.add(new CssClassAppender(StringUtils.toLowerDashed(linkAndLabel.getLabel())));
                link.addOrReplace(viewTitleLabel);
                item.addOrReplace(link);
            }
        };
        container.addOrReplace(listView);
        //addOrReplace(views);
        
        fcl.addOrReplace(views);
    }

    
}
