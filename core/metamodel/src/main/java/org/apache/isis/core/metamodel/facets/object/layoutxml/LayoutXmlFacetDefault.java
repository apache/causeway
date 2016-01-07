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
package org.apache.isis.core.metamodel.facets.object.layoutxml;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.layout.v1_0.Action;
import org.apache.isis.applib.layout.v1_0.ActionHolder;
import org.apache.isis.applib.layout.v1_0.ActionLayout;
import org.apache.isis.applib.layout.v1_0.Collection;
import org.apache.isis.applib.layout.v1_0.CollectionLayout;
import org.apache.isis.applib.layout.v1_0.Column;
import org.apache.isis.applib.layout.v1_0.DomainObject;
import org.apache.isis.applib.layout.v1_0.Property;
import org.apache.isis.applib.layout.v1_0.PropertyGroup;
import org.apache.isis.applib.layout.v1_0.PropertyLayout;
import org.apache.isis.applib.layout.v1_0.Tab;
import org.apache.isis.applib.layout.v1_0.TabGroup;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionPositionFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.BookmarkPolicyFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFaFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.DescribedAsFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.HiddenFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.NamedFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.collections.layout.CssClassFacetForCollectionLayoutXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionLayoutXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DescribedAsFacetForCollectionLayoutXml;
import org.apache.isis.core.metamodel.facets.collections.layout.HiddenFacetForCollectionLayoutXml;
import org.apache.isis.core.metamodel.facets.collections.layout.NamedFacetForCollectionLayoutXml;
import org.apache.isis.core.metamodel.facets.collections.layout.PagedFacetForCollectionLayoutXml;
import org.apache.isis.core.metamodel.facets.collections.layout.SortedByFacetForCollectionLayoutXml;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.DescribedAsFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.NamedFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyLayoutXml;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class LayoutXmlFacetDefault
            extends FacetAbstract
            implements LayoutXmlFacet {

    private final DomainObject metadata;
    private final TranslationService translationService;

    public static Class<? extends Facet> type() {
        return LayoutXmlFacet.class;
    }


    public static LayoutXmlFacet create(
            final FacetHolder facetHolder,
            final DomainObject domainObject,
            final TranslationService translationService) {
        if(domainObject == null) {
            return null;
        }
        return new LayoutXmlFacetDefault(facetHolder, domainObject, translationService);
    }

    private LayoutXmlFacetDefault(
            final FacetHolder facetHolder,
            final DomainObject metadata,
            final TranslationService translationService) {
        super(LayoutXmlFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.metadata = metadata;
        this.translationService = translationService;
    }


    private boolean derivedAndSynced;

    public DomainObject getLayoutMetadata() {
        //return derivedAndSynced ? metadata : deriveAndSync(metadata);
        return deriveAndSync(metadata);
    }

    private  DomainObject deriveAndSync(final DomainObject metadata) {
        synchronized (metadata) {
            doDeriveAndSync(metadata);
            derivedAndSynced = true;
        }
        return metadata;
    }

    private void doDeriveAndSync(final DomainObject metadata) {

        final ObjectSpecification objectSpec = (ObjectSpecification) getFacetHolder();
        final Map<String, OneToOneAssociation> oneToOneAssociationById =
                ObjectMember.Util.mapById(getOneToOneAssociations(objectSpec));
        final Map<String, OneToManyAssociation> oneToManyAssociationById =
                ObjectMember.Util.mapById(getOneToManyAssociations(objectSpec));
        final Map<String, ObjectAction> objectActionById =
                ObjectMember.Util.mapById(objectSpec.getObjectActions(Contributed.INCLUDED));

        derive(metadata, oneToOneAssociationById, oneToManyAssociationById, objectActionById);

        sync(metadata, oneToOneAssociationById, oneToManyAssociationById, objectActionById);
    }

    private void derive(
            final DomainObject metadata,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById) {
        final List<String> propertyIds = Lists.newArrayList();
        final List<String> collectionIds = Lists.newArrayList();
        final List<String> actionIds = Lists.newArrayList();
        final AtomicReference<PropertyGroup> generalPropertyGroupRef = new AtomicReference<>();
        final AtomicReference<Column> firstColumnRef = new AtomicReference<>();
        final AtomicReference<TabGroup> lastTabGroupRef = new AtomicReference<>();

        metadata.visit(new DomainObject.VisitorAdapter() {
            @Override
            public void visit(final Property property) {
                propertyIds.add(property.getId());
            }
            @Override
            public void visit(final Collection collection) {
                collectionIds.add(collection.getId());
            }
            @Override
            public void visit(final Action action, final ActionHolder holder) {
                actionIds.add(action.getId());
            }
            @Override
            public void visit(final Column column) {
                firstColumnRef.compareAndSet(null, column);
            }
            @Override
            public void visit(final PropertyGroup propertyGroup) {
                if(propertyGroup.getName().equals("General")) {
                    generalPropertyGroupRef.compareAndSet(null, propertyGroup);
                }
            }
            @Override
            public void visit(final TabGroup tabGroup) {
                lastTabGroupRef.set(tabGroup);
            }
        });

        // any missing properties will be added to the (first) 'General' property group found
        // if there is no 'General' property group then one will be added to the first Column of the first Tab.
        final List<String> missingPropertyIds = Lists.newArrayList(oneToOneAssociationById.keySet());
        missingPropertyIds.removeAll(propertyIds);

        if(!missingPropertyIds.isEmpty()) {
            // ensure that there is a property group to use
            boolean wasSet = generalPropertyGroupRef.compareAndSet(null, new PropertyGroup("General"));
            final PropertyGroup generalPropertyGroup = generalPropertyGroupRef.get();
            if(wasSet) {
                firstColumnRef.get().getContent().add(generalPropertyGroup);
            }
            Iterables.removeAll(propertyIds, oneToOneAssociationById.keySet());
            for (final String propertyId : missingPropertyIds) {
                generalPropertyGroup.getProperties().add(new Property(propertyId));
            }
        }

        // any missing collections will be added as tabs to the last TabGroup.
        // If there is only a single tab group then a new TabGroup will be added first
        final List<String> missingCollectionIds = Lists.newArrayList(oneToManyAssociationById.keySet());
        missingCollectionIds.removeAll(collectionIds);

        if(!missingCollectionIds.isEmpty()) {
            while(metadata.getTabGroups().size() < 2) {
                final TabGroup tabGroup = new TabGroup();
                metadata.getTabGroups().add(tabGroup);
                lastTabGroupRef.set(tabGroup);
            }
            final TabGroup lastTabGroup = lastTabGroupRef.get();
            for (final String collectionId : missingCollectionIds) {
                final Tab tab = new Tab();
                lastTabGroup.getTabs().add(tab);
                Column left = new Column(12);
                tab.setLeft(left);
                left.getContent().add(new Collection(collectionId));
            }
        }

        // any missing actions will be added as domain object actions (in the header)
        final List<String> missingActionIds = Lists.newArrayList(objectActionById.keySet());
        missingActionIds.removeAll(actionIds);

        if(!missingActionIds.isEmpty()) {
            for (String actionId : missingActionIds) {
                metadata.getActions().add(new Action(actionId));
            }
        }
    }

    private void sync(
            final DomainObject metadata,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById) {

        metadata.visit(new DomainObject.VisitorAdapter() {
            private int domainObjectSequence = 1;
            private int propertyGroupSequence = 1;
            private int propertySequence = 1;
            private int collectionSequence = 1;
            private Map<Action, ActionHolder> actionHolderByAction = Maps.newHashMap();
            @Override
            public void visit(final Action action, final ActionHolder actionHolder) {
                actionHolderByAction.put(action, actionHolder);
                final ObjectAction objectAction = objectActionById.get(action.getId());
                final String memberOrderName;
                final int memberOrderSequence;
                if(actionHolder instanceof PropertyGroup) {
                    final PropertyGroup propertyGroup = (PropertyGroup) actionHolder;
                    final List<Property> properties = propertyGroup.getProperties();
                    final Property property = properties.get(0); // any will do
                    memberOrderName = property.getId();
                    memberOrderSequence = propertyGroupSequence++;
                } else if(actionHolder instanceof Property) {
                    final Property property = (Property) actionHolder;
                    memberOrderName = property.getId();
                    memberOrderSequence = propertySequence++;
                } else if(actionHolder instanceof Collection) {
                    final Collection collection = (Collection) actionHolder;
                    memberOrderName = collection.getId();
                    memberOrderSequence = collectionSequence++;
                } else {
                    // DomainObject
                    memberOrderName = null;
                    memberOrderSequence = domainObjectSequence++;
                }
                FacetUtil.addFacet(
                    new MemberOrderFacetXml(memberOrderName, ""+memberOrderSequence, translationService, objectAction));
            }
            @Override
            public void visit(final ActionLayout actionLayout, final Action action) {
                final ObjectAction objectAction = objectActionById.get(action.getId());
                final ActionHolder actionHolder = actionHolderByAction.get(action);

                final ActionLayout actionLayoutForPosition;
                if(actionHolder instanceof PropertyGroup) {
                    // ensure that there is a non-null valid ActionLayout#position()
                    actionLayoutForPosition = actionLayout != null ? actionLayout : new ActionLayout();
                    if(     actionLayoutForPosition.getPosition() == null ||
                            actionLayoutForPosition.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.BELOW ||
                            actionLayoutForPosition.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.RIGHT) {
                        actionLayoutForPosition.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.PANEL);
                    }
                } else if(actionHolder instanceof Property) {
                    // ensure that there is a non-null valid ActionLayout#position()
                    actionLayoutForPosition = actionLayout != null ? actionLayout : new ActionLayout();
                    if(     actionLayoutForPosition.getPosition() == null ||
                            actionLayoutForPosition.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL_DROPDOWN ||
                            actionLayoutForPosition.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL) {
                        actionLayoutForPosition.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.BELOW);
                    }
                } else {
                    // doesn't do anything for DomainObject or Collection
                    actionLayoutForPosition = null;
                }
                FacetUtil.addFacet(ActionPositionFacetForActionLayoutXml.create(actionLayoutForPosition, objectAction));

                FacetUtil.addFacet(BookmarkPolicyFacetForActionLayoutXml.create(actionLayout, objectAction));
                FacetUtil.addFacet(CssClassFacetForActionLayoutXml.create(actionLayout, objectAction));
                FacetUtil.addFacet(CssClassFaFacetForActionLayoutXml.create(actionLayout, objectAction));
                FacetUtil.addFacet(DescribedAsFacetForActionLayoutXml.create(actionLayout, objectAction));
                FacetUtil.addFacet(HiddenFacetForActionLayoutXml.create(actionLayout, objectAction));
                FacetUtil.addFacet(NamedFacetForActionLayoutXml.create(actionLayout, objectAction));
            }
            @Override
            public void visit(final PropertyLayout propertyLayout, final Property property) {
                final OneToOneAssociation oneToOneAssociation = oneToOneAssociationById.get(property.getId());
                FacetUtil.addFacet(CssClassFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
                FacetUtil.addFacet(DescribedAsFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
                FacetUtil.addFacet(HiddenFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
                FacetUtil.addFacet(LabelAtFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
                FacetUtil.addFacet(MultiLineFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
                FacetUtil.addFacet(NamedFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
                FacetUtil.addFacet(RenderedAdjustedFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
                FacetUtil.addFacet(TypicalLengthFacetForPropertyLayoutXml.create(propertyLayout, oneToOneAssociation));
            }
            @Override
            public void visit(final CollectionLayout collectionLayout, final Collection collection) {
                final OneToManyAssociation oneToManyAssociation = oneToManyAssociationById.get(collection.getId());
                FacetUtil.addFacet(CssClassFacetForCollectionLayoutXml.create(collectionLayout, oneToManyAssociation));
                FacetUtil.addFacet(DefaultViewFacetForCollectionLayoutXml.create(collectionLayout, oneToManyAssociation));
                FacetUtil.addFacet(DescribedAsFacetForCollectionLayoutXml.create(collectionLayout, oneToManyAssociation));
                FacetUtil.addFacet(HiddenFacetForCollectionLayoutXml.create(collectionLayout, oneToManyAssociation));
                FacetUtil.addFacet(NamedFacetForCollectionLayoutXml.create(collectionLayout, oneToManyAssociation));
                FacetUtil.addFacet(PagedFacetForCollectionLayoutXml.create(collectionLayout, oneToManyAssociation));
                FacetUtil.addFacet(SortedByFacetForCollectionLayoutXml.create(collectionLayout, oneToManyAssociation));
            }
        });

    }

    private List<OneToOneAssociation> getOneToOneAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec
                .getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.PROPERTIES);
        return associations;
    }
    private List<OneToManyAssociation> getOneToManyAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec
                .getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.COLLECTIONS);
        return associations;
    }
}
