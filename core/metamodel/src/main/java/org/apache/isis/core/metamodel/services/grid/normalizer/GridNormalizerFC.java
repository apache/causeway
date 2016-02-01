/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.grid.normalizer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.layout.fixedcols.FCColumn;
import org.apache.isis.applib.layout.fixedcols.FCColumnOwner;
import org.apache.isis.applib.layout.fixedcols.FCGrid;
import org.apache.isis.applib.layout.fixedcols.FCTab;
import org.apache.isis.applib.layout.fixedcols.FCTabGroup;
import org.apache.isis.applib.layout.common.MemberRegionOwner;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.ActionLayoutDataOwner;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.actions.layout.ActionPositionFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.BookmarkPolicyFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFaFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.CssClassFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.DescribedAsFacetForActionXml;
import org.apache.isis.core.metamodel.facets.actions.layout.HiddenFacetForActionLayoutXml;
import org.apache.isis.core.metamodel.facets.actions.layout.NamedFacetForActionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.CssClassFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.DescribedAsFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.HiddenFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.NamedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.PagedFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.collections.layout.SortedByFacetForCollectionXml;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetXml;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.DescribedAsFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.NamedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyXml;
import org.apache.isis.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyXml;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class GridNormalizerFC extends GridNormalizerAbstract<FCGrid> {

    public static final String TNS = "http://isis.apache.org/schema/applib/layout/fixedcols";
    public static final String SCHEMA_LOCATION = "http://isis.apache.org/schema/applib/layout/fixedcols/fixedcols.xsd";


    public GridNormalizerFC(
            final TranslationService translationService,
            final SpecificationLoader specificationLookup) {
        super(translationService, specificationLookup);
    }

    @Override
    public void normalize(final FCGrid page, final Class<?> domainClass) {

        final ObjectSpecification objectSpec = specificationLookup.loadSpecification(domainClass);

        final Map<String, OneToOneAssociation> oneToOneAssociationById =
                ObjectMember.Util.mapById(getOneToOneAssociations(objectSpec));
        final Map<String, OneToManyAssociation> oneToManyAssociationById =
                ObjectMember.Util.mapById(getOneToManyAssociations(objectSpec));
        final Map<String, ObjectAction> objectActionById =
                ObjectMember.Util.mapById(objectSpec.getObjectActions(Contributed.INCLUDED));

        derive(page, oneToOneAssociationById, oneToManyAssociationById, objectActionById);
        overwrite(page, oneToOneAssociationById, oneToManyAssociationById, objectActionById);
    }

    /**
     * Ensures that all object members (properties, collections and actions) are in the metadata.
     *
     * <p>
     *     If they are missing then they will be added to default tabs (created on the fly if need be).
     * </p>
     */
    private static void derive(
            final FCGrid metadata,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById) {

        final LinkedHashMap<String, PropertyLayoutData> propertyIds = metadata.getAllPropertiesById();
        final LinkedHashMap<String, CollectionLayoutData> collectionIds = metadata.getAllCollectionsById();
        final LinkedHashMap<String, ActionLayoutData> actionIds = metadata.getAllActionsById();

        final AtomicReference<FieldSet> defaultPropertyGroupRef = new AtomicReference<>();
        final AtomicReference<FCColumn> firstColumnRef = new AtomicReference<>();
        final AtomicReference<FCTabGroup> lastTabGroupRef = new AtomicReference<>();

        // capture the first column, and also
        // capture the first property group (if any) with the default name ('General')
        metadata.visit(new FCGrid.VisitorAdapter() {
            @Override
            public void visit(final FCColumn fcColumn) {
                firstColumnRef.compareAndSet(null, fcColumn);
            }
            @Override
            public void visit(final FieldSet fieldSet) {
                if(MemberGroupLayoutFacet.DEFAULT_GROUP.equals(fieldSet.getName())) {
                    defaultPropertyGroupRef.compareAndSet(null, fieldSet);
                }
            }
            @Override
            public void visit(final FCTabGroup fcTabGroup) {
                lastTabGroupRef.set(fcTabGroup);
            }
        });

        // any missing properties will be added to the (first) 'General' property group found
        // if there is no default ('General') property group
        // then one will be added to the first Column of the first Tab.
        final Tuple<List<String>> propertyIdTuple = surplusAndMissing(propertyIds.keySet(), oneToOneAssociationById.keySet());
        final List<String> surplusPropertyIds = propertyIdTuple.first;
        final List<String> missingPropertyIds = propertyIdTuple.second;

        for (String surplusPropertyId : surplusPropertyIds) {
            propertyIds.get(surplusPropertyId).setMetadataError("No such property");
        }

        if(!missingPropertyIds.isEmpty()) {
            // ensure that there is a property group to use
            boolean wasSet = defaultPropertyGroupRef.compareAndSet(null, new FieldSet(MemberGroupLayoutFacet.DEFAULT_GROUP));
            final FieldSet defaultFieldSet = defaultPropertyGroupRef.get();
            if(wasSet) {
                firstColumnRef.get().getFieldSets().add(defaultFieldSet);
            }
            for (final String propertyId : missingPropertyIds) {
                defaultFieldSet.getProperties().add(new PropertyLayoutData(propertyId));
            }
        }


        // any missing collections will be added as tabs to the last TabGroup.
        // If there is only a single tab group then a new TabGroup will be added first
        final Tuple<List<String>> collectionIdTuple = surplusAndMissing(collectionIds.keySet(), oneToManyAssociationById.keySet());
        final List<String> surplusCollectionIds = collectionIdTuple.first;
        final List<String> missingCollectionIds = collectionIdTuple.second;

        for (String surplusCollectionId : surplusCollectionIds) {
            collectionIds.get(surplusCollectionId).setMetadataError("No such collection");
        }

        if(!missingCollectionIds.isEmpty()) {
            while(metadata.getTabGroups().size() < 2) {
                final FCTabGroup tabGroup = new FCTabGroup();
                metadata.getTabGroups().add(tabGroup);
                lastTabGroupRef.set(tabGroup);
            }
            final FCTabGroup lastTabGroup = lastTabGroupRef.get();
            for (final String collectionId : missingCollectionIds) {
                final FCTab FCTab = new FCTab();
                lastTabGroup.getTabs().add(FCTab);
                FCColumn left = new FCColumn(12);
                FCTab.setLeft(left);
                final CollectionLayoutData layoutMetadata = new CollectionLayoutData(collectionId);
                layoutMetadata.setDefaultView("table");
                left.getCollections().add(layoutMetadata);
            }
        }

        // any missing actions will be added as domain object actions (in the header)
        final Tuple<List<String>> actionIdTuple = surplusAndMissing(actionIds.keySet(), objectActionById.keySet());
        final List<String> surplusActionIds = actionIdTuple.first;
        final List<String> missingActionIds = actionIdTuple.second;

        for (String surplusActionId : surplusActionIds) {
            actionIds.get(surplusActionId).setMetadataError("No such action");
        }

        if(!missingActionIds.isEmpty()) {
            for (String actionId : missingActionIds) {
                List<ActionLayoutData> actions = metadata.getActions();
                if(actions == null) {
                    actions = Lists.newArrayList();
                    metadata.setActions(actions);
                }
                actions.add(new ActionLayoutData(actionId));
            }
        }
    }

    static class Tuple<T> {
        final T first;
        final T second;
        private Tuple(final T first, final T second) {
            this.first = first;
            this.second = second;
        }
        public static <T> Tuple<T> of(final T first, final T second) {
            return new Tuple<>(first, second);
        }
    }
    /**
     * Returns a 2-element tuple of [first-second, second-first]
     */
    static <T> Tuple<List<T>> surplusAndMissing(final Collection<T> first, final Collection<T> second){
        final List<T> firstNotSecond = Lists.newArrayList(first);
        firstNotSecond.removeAll(second);
        final List<T> secondNotFirst = Lists.newArrayList(second);
        secondNotFirst.removeAll(first);
        return Tuple.of(firstNotSecond, secondNotFirst);
    }

    private void overwrite(
            final FCGrid page,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById) {

        final Map<String, int[]> propertySequenceByGroup = Maps.newHashMap();

        page.visit(new FCGrid.VisitorAdapter() {
            private int collectionSequence = 1;
            private int actionDomainObjectSequence = 1;
            private int actionPropertyGroupSequence = 1;
            private int actionPropertySequence = 1;
            private int actionCollectionSequence = 1;

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                final ActionLayoutDataOwner actionLayoutDataOwner = actionLayoutData.getOwner();
                final ObjectAction objectAction = objectActionById.get(actionLayoutData.getId());
                if(objectAction == null) {
                    return;
                }

                final String memberOrderName;
                final int memberOrderSequence;
                if(actionLayoutDataOwner instanceof FieldSet) {
                    final FieldSet fieldSet = (FieldSet) actionLayoutDataOwner;
                    final List<PropertyLayoutData> properties = fieldSet.getProperties();
                    final PropertyLayoutData propertyLayoutData = properties.get(0); // any will do
                    memberOrderName = propertyLayoutData.getId();
                    memberOrderSequence = actionPropertyGroupSequence++;
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    final PropertyLayoutData propertyLayoutData = (PropertyLayoutData) actionLayoutDataOwner;
                    memberOrderName = propertyLayoutData.getId();
                    memberOrderSequence = actionPropertySequence++;
                } else if(actionLayoutDataOwner instanceof CollectionLayoutData) {
                    final CollectionLayoutData collectionLayoutData = (CollectionLayoutData) actionLayoutDataOwner;
                    memberOrderName = collectionLayoutData.getId();
                    memberOrderSequence = actionCollectionSequence++;
                } else {
                    // DomainObject
                    memberOrderName = null;
                    memberOrderSequence = actionDomainObjectSequence++;
                }
                FacetUtil.addFacet(
                        new MemberOrderFacetXml(memberOrderName, ""+memberOrderSequence, translationService, objectAction));


                if(actionLayoutDataOwner instanceof FieldSet) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.BELOW ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.RIGHT) {
                        actionLayoutData.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.PANEL);
                    }
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL_DROPDOWN ||
                            actionLayoutData.getPosition() == org.apache.isis.applib.annotation.ActionLayout.Position.PANEL) {
                        actionLayoutData.setPosition(org.apache.isis.applib.annotation.ActionLayout.Position.BELOW);
                    }
                } else {
                    // doesn't do anything for DomainObject or Collection
                    actionLayoutData.setPosition(null);
                }

                FacetUtil.addFacet(ActionPositionFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addFacet(BookmarkPolicyFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addFacet(CssClassFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addFacet(CssClassFaFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addFacet(DescribedAsFacetForActionXml.create(actionLayoutData, objectAction));
                FacetUtil.addFacet(HiddenFacetForActionLayoutXml.create(actionLayoutData, objectAction));
                FacetUtil.addFacet(NamedFacetForActionXml.create(actionLayoutData, objectAction));
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                final OneToOneAssociation oneToOneAssociation = oneToOneAssociationById.get(propertyLayoutData.getId());
                if(oneToOneAssociation == null) {
                    return;
                }

                FacetUtil.addFacet(CssClassFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addFacet(DescribedAsFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addFacet(HiddenFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addFacet(LabelAtFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addFacet(MultiLineFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addFacet(NamedFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addFacet(
                        RenderedAdjustedFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));
                FacetUtil.addFacet(TypicalLengthFacetForPropertyXml.create(propertyLayoutData, oneToOneAssociation));

                // @MemberOrder#name based on owning property group, @MemberOrder#sequence monotonically increasing
                final FieldSet fieldSet = propertyLayoutData.getOwner();
                final String groupName = fieldSet.getName();
                final String sequence = nextInSequenceFor(groupName, propertySequenceByGroup);
                FacetUtil.addFacet(
                        new MemberOrderFacetXml(groupName, sequence, translationService, oneToOneAssociation));
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                final OneToManyAssociation oneToManyAssociation = oneToManyAssociationById.get(collectionLayoutData.getId());
                if(oneToManyAssociation == null) {
                    return;
                }

                FacetUtil.addFacet(CssClassFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addFacet(
                        DefaultViewFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addFacet(
                        DescribedAsFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addFacet(HiddenFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addFacet(NamedFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addFacet(PagedFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));
                FacetUtil.addFacet(SortedByFacetForCollectionXml.create(collectionLayoutData, oneToManyAssociation));

                // @MemberOrder#name based on the collection's id (so that each has a single "member group")
                final String groupName = collectionLayoutData.getId();
                final String sequence = "" + collectionSequence++;
                FacetUtil.addFacet(
                        new MemberOrderFacetXml(groupName, sequence, translationService, oneToManyAssociation));

                // if there is only a single column and no other contents, then copy the collection Id onto the tab'
                final MemberRegionOwner memberRegionOwner = collectionLayoutData.getOwner();
                if(memberRegionOwner instanceof FCColumn) {
                    final FCColumn FCColumn = (FCColumn) memberRegionOwner;
                    final FCColumnOwner holder = FCColumn.getOwner();
                    if(holder instanceof FCTab) {
                        final FCTab FCTab = (FCTab) holder;
                        if(FCTab.getContents().size() == 1 && Strings.isNullOrEmpty(FCTab.getName()) ) {
                            final String collectionName = oneToManyAssociation.getName();
                            FCTab.setName(collectionName);
                        }
                    }
                }
            }
        });
    }

    private String nextInSequenceFor(
            final String key, final Map<String, int[]> seqByKey) {
        synchronized (seqByKey) {
            int[] holder = seqByKey.get(key);
            if(holder == null) {
                holder = new int[]{0};
                seqByKey.put(key, holder);
            }
            holder[0]++;
            return ""+holder[0];
        }
    }

    private static List<OneToOneAssociation> getOneToOneAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec.getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.PROPERTIES);
        return associations;
    }
    private static List<OneToManyAssociation> getOneToManyAssociations(final ObjectSpecification objectSpec) {
        List associations = objectSpec.getAssociations(Contributed.INCLUDED, ObjectAssociation.Filters.COLLECTIONS);
        return associations;
    }

}
