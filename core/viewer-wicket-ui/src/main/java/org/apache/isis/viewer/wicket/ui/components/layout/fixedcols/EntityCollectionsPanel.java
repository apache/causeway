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

package org.apache.isis.viewer.wicket.ui.components.layout.fixedcols;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.fixedcols.FCColumn;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.services.DeweyOrderComparator;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.entity.collection.EntityCollectionPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing the properties of an entity, as per
 * the provided {@link EntityModel}.
 */
public class EntityCollectionsPanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_COLLECTIONS = "entityCollections";
    private static final String ID_COLLECTIONS = "collections";
    private static final String ID_COLLECTION = "collection";

    // view metadata (if any available)
    private final FCColumn fcColumnIfAny;

    public EntityCollectionsPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);

        fcColumnIfAny = (FCColumn) entityModel.getLayoutMetadata();

        buildGui();
    }

    private void buildGui() {
        buildEntityPropertiesAndOrCollectionsGui();
        setOutputMarkupId(true); // so can repaint via ajax
    }

    private void buildEntityPropertiesAndOrCollectionsGui() {
        final EntityModel model = getModel();
        final ObjectAdapter adapter = model.getObject();
        if (adapter != null) {
            addCollections();
        } else {
            permanentlyHide(ID_ENTITY_COLLECTIONS);
        }
    }

    private void addCollections() {
        final EntityModel entityModel = getModel();
        final ObjectAdapter adapter = entityModel.getObject();

        final Filter<ObjectAssociation> filter;
        if (fcColumnIfAny != null) {
            final ImmutableList<String> collectionIds = FluentIterable
                    .from(fcColumnIfAny.getCollections())
                    .transform(CollectionLayoutData.Functions.id())
                    .toList();
            filter = new Filter<ObjectAssociation>() {
                @Override
                public boolean accept(final ObjectAssociation objectAssociation) {
                    return collectionIds.contains(objectAssociation.getId());
                }
            };
        } else {
            filter = Filters.any();
        }

        final List<ObjectAssociation> associations = visibleCollections(adapter, filter);
        associations.sort(new Comparator<ObjectAssociation>() {
            private final DeweyOrderComparator deweyOrderComparator = new DeweyOrderComparator();
            @Override
            public int compare(final ObjectAssociation o1, final ObjectAssociation o2) {
                final MemberOrderFacet o1Facet = o1.getFacet(MemberOrderFacet.class);
                final MemberOrderFacet o2Facet = o2.getFacet(MemberOrderFacet.class);
                return o1Facet == null? +1:
                        o2Facet == null? -1:
                        deweyOrderComparator.compare(o1Facet.sequence(), o2Facet.sequence());
            }
        });

        final RepeatingView collectionRv = new RepeatingView(ID_COLLECTIONS);
        add(collectionRv);

        for (final ObjectAssociation association : associations) {

            final WebMarkupContainer collectionRvContainer = new WebMarkupContainer(collectionRv.newChildId());
            collectionRv.add(collectionRvContainer);

            final CollectionLayoutData collectionLayoutData = new CollectionLayoutData(association.getId());
            final EntityModel entityModelWithCollectionLayoutMetadata =
                    entityModel.cloneWithLayoutMetadata(collectionLayoutData);

            collectionRvContainer.add(new EntityCollectionPanel(ID_COLLECTION, entityModelWithCollectionLayoutMetadata));
        }
    }

    private static List<ObjectAssociation> visibleCollections(
            final ObjectAdapter adapter,
            final Filter<ObjectAssociation> filter) {
        return adapter.getSpecification().getAssociations(
                Contributed.INCLUDED, visibleCollectionsFilter(adapter, filter));
    }

    @SuppressWarnings("unchecked")
    private static Filter<ObjectAssociation> visibleCollectionsFilter(
            final ObjectAdapter adapter,
            final Filter<ObjectAssociation> filter) {
        return Filters.and(
                ObjectAssociation.Filters.COLLECTIONS,
                ObjectAssociation.Filters.dynamicallyVisible(
                        adapter, InteractionInitiatedBy.USER, Where.PARENTED_TABLES),
                filter);
    }

}
