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
package org.apache.isis.core.metamodel.services.grid.bootstrap3;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.ActionLayoutDataOwner;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3TabGroup;
import org.apache.isis.applib.layout.grid.bootstrap3.Size;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;
import org.apache.isis.core.metamodel.services.grid.GridSystemServiceAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class GridSystemServiceBS3 extends GridSystemServiceAbstract<BS3Grid> {

    public static final String TNS = "http://isis.apache.org/applib/layout/grid/bootstrap3";
    public static final String SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/grid/bootstrap3/bootstrap3.xsd";

    public GridSystemServiceBS3() {
        super(BS3Grid.class, TNS, SCHEMA_LOCATION);
    }


    @Programmatic
    @Override
    public BS3Grid defaultGrid(final Class<?> domainClass) {
        final BS3Grid bs3Grid = new BS3Grid();

        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(domainClass);
        bs3Grid.setDomainClass(domainClass);

        final BS3Row headerRow = new BS3Row();
        bs3Grid.getRows().add(headerRow);
        final BS3Col headerRowCol = new BS3Col();
        headerRowCol.setSpan(12);
        headerRowCol.setUnreferencedActions(true);
        headerRowCol.setDomainObject(new DomainObjectLayoutData());
        headerRow.getCols().add(headerRowCol);

        final BS3Row propsRow = new BS3Row();
        bs3Grid.getRows().add(propsRow);

//TODO [ahuber] marked for removal (breaks legacy functionality) ...
//        final MemberGroupLayoutFacet memberGroupLayoutFacet =
//                objectSpec.getFacet(MemberGroupLayoutFacet.class);
//        if(memberGroupLayoutFacet != null) {
//            // if have @MemberGroupLayout (or equally, a .layout.json file)
//            final MemberGroupLayout.ColumnSpans columnSpans = memberGroupLayoutFacet.getColumnSpans();
//            addFieldSetsToColumn(propsRow, columnSpans.getLeft(), memberGroupLayoutFacet.getLeft(), true);
//            addFieldSetsToColumn(propsRow, columnSpans.getMiddle(), memberGroupLayoutFacet.getMiddle(), false);
//            addFieldSetsToColumn(propsRow, columnSpans.getRight(), memberGroupLayoutFacet.getRight(), false);
//
//            final BS3Col col = new BS3Col();
//            final int collectionSpan = columnSpans.getCollections();
//            col.setUnreferencedCollections(true);
//            col.setSpan(collectionSpan > 0? collectionSpan: 12);
//            propsRow.getCols().add(col);
//
//            // will already be sorted per @MemberOrder
//            final List<OneToManyAssociation> collections = objectSpec.getCollections(Contributed.INCLUDED);
//            for (OneToManyAssociation collection : collections) {
//                col.getCollections().add(new CollectionLayoutData(collection.getId()));
//            }
//        } else 
        {

            // if no layout hints other than @MemberOrder
            addFieldSetsToColumn(propsRow, 4, Arrays.asList("General"), true);

            final BS3Col col = new BS3Col();
            col.setUnreferencedCollections(true);
            col.setSpan(12);
            propsRow.getCols().add(col);
        }
        return bs3Grid;
    }

    static void addFieldSetsToColumn(
            final BS3Row propsRow,
            final int span,
            final List<String> memberGroupNames,
            final boolean unreferencedProperties) {

        if(span > 0 || unreferencedProperties) {
            final BS3Col col = new BS3Col();
            col.setSpan(span); // in case we are here because of 'unreferencedProperties' needs setting
            propsRow.getCols().add(col);
            final List<String> leftMemberGroups = memberGroupNames;
            for (String memberGroup : leftMemberGroups) {
                final FieldSet fieldSet = new FieldSet();
                fieldSet.setName(memberGroup);
                // fieldSet's id will be derived from the name later
                // during normalization phase.
                if(unreferencedProperties && col.getFieldSets().isEmpty()) {
                    fieldSet.setUnreferencedProperties(true);
                }
                col.getFieldSets().add(fieldSet);
            }
        }
    }

    // //////////////////////////////////////

    /**
     * Mandatory hook method defined in {@link GridSystemServiceAbstract superclass}, called by {@link #normalize(Grid, Class)}.
     */
    @Override
    protected boolean validateAndNormalize(
            final Grid grid,
            final Class<?> domainClass) {


        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(domainClass);

        final Map<String, OneToOneAssociation> oneToOneAssociationById =
                ObjectMember.Util.mapById(getOneToOneAssociations(objectSpec));
        final Map<String, OneToManyAssociation> oneToManyAssociationById =
                ObjectMember.Util.mapById(getOneToManyAssociations(objectSpec));
        final Map<String, ObjectAction> objectActionById =
                ObjectMember.Util.mapById(
                        FluentIterable
                        .from(objectSpec.getObjectActions(Contributed.INCLUDED))
                        .filter((Predicate) ObjectAction.Predicates.notBulkOnly())
                        .toList());

        final BS3Grid bs3Grid = (BS3Grid) grid;

        final LinkedHashMap<String, PropertyLayoutData> propertyLayoutDataById = bs3Grid.getAllPropertiesById();
        final LinkedHashMap<String, CollectionLayoutData> collectionLayoutDataById = bs3Grid.getAllCollectionsById();
        final LinkedHashMap<String, ActionLayoutData> actionLayoutDataById = bs3Grid.getAllActionsById();


        // find all row and col ids
        // ensure that all Ids are different

        final List<String> gridIds = Lists.newArrayList();
        final LinkedHashMap<String, BS3Row> rowIds = Maps.newLinkedHashMap();
        final LinkedHashMap<String, BS3Col> colIds = Maps.newLinkedHashMap();
        final LinkedHashMap<String, FieldSet> fieldSetIds = Maps.newLinkedHashMap();

        final AtomicReference<Boolean> duplicateIdDetected = new AtomicReference<>(false);

        bs3Grid.visit(new BS3Grid.VisitorAdapter(){
            @Override
            public void visit(final BS3Row bs3Row) {
                final String id = bs3Row.getId();
                if(id == null) {
                    return;
                }
                if(gridIds.contains(id)) {
                    bs3Row.setMetadataError("There is another element in the grid with this id");
                    duplicateIdDetected.set(true);
                    return;
                }
                rowIds.put(id, bs3Row);
                gridIds.add(id);
            }

            @Override
            public void visit(final BS3Col bs3Col) {
                final String id = bs3Col.getId();
                if(id == null) {
                    return;
                }
                if(gridIds.contains(id)) {
                    bs3Col.setMetadataError("There is another element in the grid with this id");
                    duplicateIdDetected.set(true);
                    return;
                }
                colIds.put(id, bs3Col);
                gridIds.add(id);
            }

            @Override
            public void visit(final FieldSet fieldSet) {
                String id = fieldSet.getId();
                if(id == null) {
                    final String name = fieldSet.getName();
                    fieldSet.setId(id = asId(name));
                }
                if(gridIds.contains(id)) {
                    fieldSet.setMetadataError("There is another element in the grid with this id");
                    duplicateIdDetected.set(true);
                    return;
                }
                fieldSetIds.put(id, fieldSet);
                gridIds.add(id);
            }
        });

        if(duplicateIdDetected.get()) {
            return false;
        }


        // ensure that there is exactly one col with the
        // unreferencedActions, unreferencedProperties and unreferencedCollections attribute set.

        final AtomicReference<BS3Col> colForUnreferencedActionsRef = new AtomicReference<>();
        final AtomicReference<BS3Col> colForUnreferencedCollectionsRef = new AtomicReference<>();
        final AtomicReference<FieldSet> fieldSetForUnreferencedActionsRef = new AtomicReference<>();
        final AtomicReference<FieldSet> fieldSetForUnreferencedPropertiesRef = new AtomicReference<>();
        final AtomicReference<BS3TabGroup> tabGroupForUnreferencedCollectionsRef = new AtomicReference<>();

        bs3Grid.visit(new BS3Grid.VisitorAdapter(){

            @Override
            public void visit(final BS3Col bs3Col) {
                if(isSet(bs3Col.isUnreferencedActions())) {
                    if(colForUnreferencedActionsRef.get() != null) {
                        bs3Col.setMetadataError("More than one col with 'unreferencedActions' attribute set");
                    } else if(fieldSetForUnreferencedActionsRef.get() != null) {
                        bs3Col.setMetadataError("Already found a fieldset with 'unreferencedActions' attribute set");
                    } else {
                        colForUnreferencedActionsRef.set(bs3Col);
                    }
                }
                if(isSet(bs3Col.isUnreferencedCollections())) {
                    if(colForUnreferencedCollectionsRef.get() != null) {
                        bs3Col.setMetadataError("More than one col with 'unreferencedCollections' attribute set");
                    } else if(tabGroupForUnreferencedCollectionsRef.get() != null) {
                        bs3Col.setMetadataError("Already found a tabgroup with 'unreferencedCollections' attribute set");
                    } else {
                        colForUnreferencedCollectionsRef.set(bs3Col);
                    }
                }
            }

            @Override
            public void visit(final FieldSet fieldSet) {
                if(isSet(fieldSet.isUnreferencedActions())) {
                    if(fieldSetForUnreferencedActionsRef.get() != null) {
                        fieldSet.setMetadataError("More than one fieldset with 'unreferencedActions' attribute set");
                    } else if(colForUnreferencedActionsRef.get() != null) {
                        fieldSet.setMetadataError("Already found a column with 'unreferencedActions' attribute set");
                    } else {
                        fieldSetForUnreferencedActionsRef.set(fieldSet);
                    }
                }
                if(isSet(fieldSet.isUnreferencedProperties())) {
                    if(fieldSetForUnreferencedPropertiesRef.get() != null) {
                        fieldSet.setMetadataError("More than one col with 'unreferencedProperties' attribute set");
                    } else {
                        fieldSetForUnreferencedPropertiesRef.set(fieldSet);
                    }
                }
            }

            @Override
            public void visit(final BS3TabGroup bs3TabGroup) {
                if(isSet(bs3TabGroup.isUnreferencedCollections())) {
                    if(tabGroupForUnreferencedCollectionsRef.get() != null) {
                        bs3TabGroup.setMetadataError("More than one tabgroup with 'unreferencedCollections' attribute set");
                    } else if(colForUnreferencedCollectionsRef.get() != null) {
                        bs3TabGroup.setMetadataError("Already found a column with 'unreferencedCollections' attribute set");
                    } else {
                        tabGroupForUnreferencedCollectionsRef.set(bs3TabGroup);
                    }
                }
            }
        });

        if(colForUnreferencedActionsRef.get() == null && fieldSetForUnreferencedActionsRef.get() == null) {
            bs3Grid.getMetadataErrors().add("No column and also no fieldset found with the 'unreferencedActions' attribute set");
        }
        if(fieldSetForUnreferencedPropertiesRef.get() == null) {
            bs3Grid.getMetadataErrors().add("No fieldset found with the 'unreferencedProperties' attribute set");
        }
        if(colForUnreferencedCollectionsRef.get() == null && tabGroupForUnreferencedCollectionsRef.get() == null) {
            bs3Grid.getMetadataErrors().add("No column and also no tabgroup found with the 'unreferencedCollections' attribute set");
        }

        if(     colForUnreferencedActionsRef.get() == null && fieldSetForUnreferencedActionsRef.get() == null ||
                fieldSetForUnreferencedPropertiesRef.get() == null ||
                colForUnreferencedCollectionsRef.get() == null && tabGroupForUnreferencedCollectionsRef.get() == null) {
            return false;
        }

        // add missing properties will be added to the first fieldset of the specified column
        final Tuple<List<String>> propertyIdTuple =
                surplusAndMissing(propertyLayoutDataById.keySet(),  oneToOneAssociationById.keySet());
        final List<String> surplusPropertyIds = propertyIdTuple.first;
        final List<String> missingPropertyIds = propertyIdTuple.second;

        for (String surplusPropertyId : surplusPropertyIds) {
            propertyLayoutDataById.get(surplusPropertyId).setMetadataError("No such property");
        }

        // catalog which associations are bound to an existing field set
        // so that (below) we can determine which missing property ids are not unbound vs which should be included
        // in the fieldset that they are bound to.
        final Map<String, Set<String>> boundAssociationIdsByFieldSetId = Maps.newHashMap();

        // all those explicitly in the grid
        for (FieldSet fieldSet : fieldSetIds.values()) {
            final String fieldSetId = fieldSet.getId();
            Set<String> boundAssociationIds = boundAssociationIdsByFieldSetId.get(fieldSetId);
            if(boundAssociationIds == null) {
                boundAssociationIds = Sets.newLinkedHashSet();
                boundAssociationIds.addAll(
                        FluentIterable.from(fieldSet.getProperties()).transform(
                                new Function<PropertyLayoutData, String>() {
                                    @Override
                                    public String apply(@Nullable final PropertyLayoutData propertyLayoutData) {
                                        return propertyLayoutData.getId();
                                    }
                                }).toList());
                boundAssociationIdsByFieldSetId.put(fieldSetId, boundAssociationIds);
            }
        }
        // along with any specified by existing metadata
        for (OneToOneAssociation otoa : oneToOneAssociationById.values()) {
            final MemberOrderFacet memberOrderFacet = otoa.getFacet(MemberOrderFacet.class);
            if(memberOrderFacet != null) {
                final String id = asId(memberOrderFacet.name());
                if(fieldSetIds.containsKey(id)) {
                    Set<String> boundAssociationIds = boundAssociationIdsByFieldSetId.get(id);
                    if(boundAssociationIds == null) {
                        boundAssociationIds = Sets.newLinkedHashSet();
                        boundAssociationIdsByFieldSetId.put(id, boundAssociationIds);
                    }
                    boundAssociationIds.add(otoa.getId());
                }
            }
        }

        if(!missingPropertyIds.isEmpty()) {

            final List<String> unboundPropertyIds = Lists.newArrayList(missingPropertyIds);

            for (final String fieldSetId : boundAssociationIdsByFieldSetId.keySet()) {
                final Set<String> boundPropertyIds = boundAssociationIdsByFieldSetId.get(fieldSetId);
                unboundPropertyIds.removeAll(boundPropertyIds);
            }

            for (final String fieldSetId : boundAssociationIdsByFieldSetId.keySet()) {
                final FieldSet fieldSet = fieldSetIds.get(fieldSetId);
                final Set<String> associationIds =
                        boundAssociationIdsByFieldSetId.get(fieldSetId);

                final List<OneToOneAssociation> associations = Lists.newArrayList(
                        FluentIterable.from(associationIds)
                        .transform(new Function<String, OneToOneAssociation>() {
                            @Nullable @Override public OneToOneAssociation apply(final String propertyId) {
                                return oneToOneAssociationById.get(propertyId);
                            }
                        })
                        .filter(Predicates.<OneToOneAssociation>notNull())
                        );

                Collections.sort(associations, ObjectMember.Comparators.byMemberOrderSequence());
                addPropertiesTo(fieldSet,
                        FluentIterable.from(associations)
                        .transform(ObjectAssociation.Functions.toId())
                        .toList(),
                        propertyLayoutDataById);
            }

            if(!unboundPropertyIds.isEmpty()) {
                final FieldSet fieldSet = fieldSetForUnreferencedPropertiesRef.get();
                if(fieldSet != null) {
                    addPropertiesTo(fieldSet, unboundPropertyIds, propertyLayoutDataById);
                }
            }
        }

        // any missing collections will be added as tabs to a new TabGroup in the specified column
        final Tuple<List<String>> collectionIdTuple =
                surplusAndMissing(collectionLayoutDataById.keySet(), oneToManyAssociationById.keySet());
        final List<String> surplusCollectionIds = collectionIdTuple.first;
        final List<String> missingCollectionIds = collectionIdTuple.second;

        for (String surplusCollectionId : surplusCollectionIds) {
            collectionLayoutDataById.get(surplusCollectionId).setMetadataError("No such collection");
        }

        if(!missingCollectionIds.isEmpty()) {
            List<OneToManyAssociation> sortedCollections = Lists.newArrayList(
                    FluentIterable.from(missingCollectionIds)
                    .transform(new Function<String, OneToManyAssociation>() {
                        @Nullable @Override public OneToManyAssociation apply(@Nullable final String collectionId) {
                            return oneToManyAssociationById.get(collectionId);
                        }
                    })
                    .toSortedList(ObjectMember.Comparators.byMemberOrderSequence())

                    );
            final ImmutableList<String> sortedMissingCollectionIds = FluentIterable.from(sortedCollections)
                    .transform(ObjectAssociation.Functions.toId()).toList();
            final BS3TabGroup bs3TabGroup = tabGroupForUnreferencedCollectionsRef.get();
            if(bs3TabGroup != null) {
                addCollectionsTo(bs3TabGroup, sortedMissingCollectionIds, objectSpec);
            } else {
                final BS3Col bs3Col = colForUnreferencedCollectionsRef.get();
                if(bs3Col != null) {
                    addCollectionsTo(bs3Col, sortedMissingCollectionIds, collectionLayoutDataById);
                }
            }
        }

        // any missing actions will be added as actions in the specified column
        final Tuple<List<String>> actionIdTuple =
                surplusAndMissing(actionLayoutDataById.keySet(), objectActionById.keySet());
        final List<String> surplusActionIds = actionIdTuple.first;
        final List<String> possiblyMissingActionIds = actionIdTuple.second;

        final List<String> associatedActionIds = Lists.newArrayList();

        List<ObjectAction> sortedPossiblyMissingActions = Lists.newArrayList(
                FluentIterable.from(possiblyMissingActionIds)
                .transform(new Function<String, ObjectAction>() {
                    @Nullable @Override public ObjectAction apply(@Nullable final String actionId) {
                        return objectActionById.get(actionId);
                    }
                })
                .toSortedList(ObjectMember.Comparators.byMemberOrderSequence()));

        List<String> sortedPossiblyMissingActionIds =
                FluentIterable.from(sortedPossiblyMissingActions)
                .transform(ObjectMember.Functions.getId())
                .toList();

        for (String actionId : sortedPossiblyMissingActionIds) {
            final ObjectAction oa = objectActionById.get(actionId);
            final MemberOrderFacet memberOrderFacet = oa.getFacet(MemberOrderFacet.class);
            if(memberOrderFacet == null) {
                continue;
            }
            final String memberOrderName = memberOrderFacet.name();
            if (memberOrderName == null) {
                continue;
            }
            final String id = asId(memberOrderName);

            if (oneToOneAssociationById.containsKey(id)) {
                associatedActionIds.add(actionId);

                if(!(memberOrderFacet instanceof MemberOrderFacetAnnotation)) {
                    // if binding not via annotation, then explicitly bind this
                    // action to the property
                    final PropertyLayoutData propertyLayoutData = propertyLayoutDataById.get(id);
                    final ActionLayoutData actionLayoutData = new ActionLayoutData(actionId);

                    final ActionPositionFacet actionPositionFacet = oa.getFacet(ActionPositionFacet.class);
                    final ActionLayoutDataOwner owner;
                    final ActionLayout.Position position;
                    if(actionPositionFacet != null) {
                        position = actionPositionFacet.position();
                        owner = position == ActionLayout.Position.PANEL ||
                                position == ActionLayout.Position.PANEL_DROPDOWN
                                ? propertyLayoutData.getOwner()
                                        : propertyLayoutData;
                    } else {
                        position = ActionLayout.Position.BELOW;
                        owner = propertyLayoutData;
                    }
                    actionLayoutData.setPosition(position);
                    addActionTo(owner, actionLayoutData);
                }

                continue;
            }
            if (oneToManyAssociationById.containsKey(id)) {
                associatedActionIds.add(actionId);

                if(!(memberOrderFacet instanceof MemberOrderFacetAnnotation)) {
                    // if binding not via annotation, then explicitly bind this
                    // action to the property
                    final CollectionLayoutData collectionLayoutData = collectionLayoutDataById.get(id);
                    final ActionLayoutData actionLayoutData = new ActionLayoutData(actionId);
                    addActionTo(collectionLayoutData, actionLayoutData);
                }
                continue;
            }
            // if the @MemberOrder for the action references a field set (that has bound
            // associations), then don't mark it as missing, but instead explicitly add it to the
            // list of actions of that fieldset.
            final Set<String> boundAssociationIds = boundAssociationIdsByFieldSetId.get(id);
            if(boundAssociationIds != null && !boundAssociationIds.isEmpty()) {

                associatedActionIds.add(actionId);

                final ActionLayoutData actionLayoutData = new ActionLayoutData(actionId);

                actionLayoutData.setPosition(ActionLayout.Position.PANEL_DROPDOWN);
                final FieldSet fieldSet = fieldSetIds.get(id);
                addActionTo(fieldSet, actionLayoutData);
                continue;
            }
        }

        // ... the missing actions are those in the second tuple, excluding those associated (via @MemberOrder#name)
        // to a property or collection.
        final List<String> missingActionIds = Lists.newArrayList(sortedPossiblyMissingActionIds);
        missingActionIds.removeAll(associatedActionIds);

        for (String surplusActionId : surplusActionIds) {
            actionLayoutDataById.get(surplusActionId).setMetadataError("No such action");
        }

        if(!missingActionIds.isEmpty()) {
            final BS3Col bs3Col = colForUnreferencedActionsRef.get();
            if(bs3Col != null) {
                addActionsTo(bs3Col, missingActionIds, actionLayoutDataById);
            } else {
                final FieldSet fieldSet = fieldSetForUnreferencedActionsRef.get();
                if(fieldSet != null) {
                    addActionsTo(fieldSet, missingActionIds, actionLayoutDataById);
                }
            }
        }

        return true;
    }

    protected void addPropertiesTo(
            final FieldSet fieldSet,
            final List<String> propertyIds,
            final LinkedHashMap<String, PropertyLayoutData> propertyLayoutDataById) {
        final ImmutableList<String> existingIds = FluentIterable
                .from(fieldSet.getProperties())
                .transform(new Function<PropertyLayoutData, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable final PropertyLayoutData propertyLayoutData) {
                        return propertyLayoutData.getId();
                    }
                })
                .toList();
        for (final String propertyId : propertyIds) {
            if(!existingIds.contains(propertyId)) {
                final PropertyLayoutData propertyLayoutData = new PropertyLayoutData(propertyId);
                fieldSet.getProperties().add(propertyLayoutData);
                propertyLayoutData.setOwner(fieldSet);
                propertyLayoutDataById.put(propertyId, propertyLayoutData);
            }
        }
    }

    protected void addCollectionsTo(
            final BS3Col tabRowCol,
            final List<String> collectionIds,
            final LinkedHashMap<String, CollectionLayoutData> collectionLayoutDataById) {
        for (final String collectionId : collectionIds) {
            final CollectionLayoutData collectionLayoutData = new CollectionLayoutData(collectionId);
            collectionLayoutData.setDefaultView("table");
            tabRowCol.getCollections().add(collectionLayoutData);
            collectionLayoutDataById.put(collectionId, collectionLayoutData);
        }
    }

    protected void addCollectionsTo(
            final BS3TabGroup tabGroup,
            final List<String> collectionIds,
            final ObjectSpecification objectSpec) {
        for (final String collectionId : collectionIds) {
            final BS3Tab bs3Tab = new BS3Tab();
            bs3Tab.setName(objectSpec.getAssociation(collectionId).getName());
            tabGroup.getTabs().add(bs3Tab);
            bs3Tab.setOwner(tabGroup);

            final BS3Row tabRow = new BS3Row();
            tabRow.setOwner(bs3Tab);
            bs3Tab.getRows().add(tabRow);

            final BS3Col tabRowCol = new BS3Col();
            tabRowCol.setSpan(12);
            tabRowCol.setSize(Size.MD);
            tabRowCol.setOwner(tabRow);
            tabRow.getCols().add(tabRowCol);

            final CollectionLayoutData layoutMetadata = new CollectionLayoutData(collectionId);
            layoutMetadata.setDefaultView("table");
            tabRowCol.getCollections().add(layoutMetadata);
        }
    }

    protected void addActionsTo(
            final BS3Col bs3Col,
            final List<String> actionIds,
            final LinkedHashMap<String, ActionLayoutData> actionLayoutDataById) {
        for (String actionId : actionIds) {
            final ActionLayoutData actionLayoutData = new ActionLayoutData(actionId);
            addActionTo(bs3Col, actionLayoutData);
            actionLayoutDataById.put(actionId, actionLayoutData);
        }
    }

    protected void addActionsTo(
            final FieldSet fieldSet,
            final List<String> actionIds,
            final LinkedHashMap<String, ActionLayoutData> actionLayoutDataById) {
        for (String actionId : actionIds) {
            final ActionLayoutData actionLayoutData = new ActionLayoutData(actionId);
            addActionTo(fieldSet, actionLayoutData);
            actionLayoutDataById.put(actionId, actionLayoutData);
        }
    }

    protected void addActionTo(
            final ActionLayoutDataOwner owner,
            final ActionLayoutData actionLayoutData) {
        List<ActionLayoutData> actions = owner.getActions();
        if(actions == null) {
            owner.setActions(actions = Lists.newArrayList());
        }
        actions.add(actionLayoutData);
        actionLayoutData.setOwner(owner);
    }

    private static Boolean isSet(final Boolean flag) {
        return flag != null && flag;
    }

    private static String asId(final String str) {
        if(Strings.isNullOrEmpty(str)) {
            return str;
        }
        final char c = str.charAt(0);
        return Character.toLowerCase(c) + str.substring(1).replaceAll("\\s+", "");
    }


}
