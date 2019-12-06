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
package org.apache.isis.metamodel.services.grid.bootstrap3;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.ActionLayout;
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
import org.apache.isis.applib.mixins.MixinConstants;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.metamodel.facets.members.order.annotprop.MemberOrderFacetAnnotation;
import org.apache.isis.metamodel.services.grid.GridReaderUsingJaxb;
import org.apache.isis.metamodel.services.grid.GridSystemServiceAbstract;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.extern.log4j.Log4j2;
import lombok.val;

@Service
@Named("isisMetaModel.gridSystemServiceBS3")
@Log4j2
public class GridSystemServiceBS3 extends GridSystemServiceAbstract<BS3Grid> {

    public static final String TNS = "http://isis.apache.org/applib/layout/grid/bootstrap3";
    public static final String SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/grid/bootstrap3/bootstrap3.xsd";

    public GridSystemServiceBS3() {
        super(BS3Grid.class, TNS, SCHEMA_LOCATION);
    }

    @Override
    public BS3Grid defaultGrid(final Class<?> domainClass) {

        try {
            final String content = _Resources.loadAsStringUtf8(getClass(), "DefaultGrid.layout.xml");
            return Optional.ofNullable(content)
                    .map(xml -> gridReader.loadGrid(xml))
                    .filter(BS3Grid.class::isInstance)
                    .map(BS3Grid.class::cast)
                    .map(bs3Grid -> withDomainClass(bs3Grid, domainClass))
                    .orElseGet(() -> fallback(domainClass))
                    ;
        } catch (final Exception e) {
            return fallback(domainClass);
        }
    }

    //
    // only ever called if fail to load DefaultGrid.layout.xml,
    // which *really* shouldn't happen
    //
    private BS3Grid fallback(Class<?> domainClass) {
        final BS3Grid bs3Grid = withDomainClass(new BS3Grid(), domainClass);

        final BS3Row headerRow = new BS3Row();
        bs3Grid.getRows().add(headerRow);
        final BS3Col headerRowCol = new BS3Col();
        headerRowCol.setSpan(12);
        headerRowCol.setUnreferencedActions(true);
        headerRowCol.setDomainObject(new DomainObjectLayoutData());
        headerRow.getCols().add(headerRowCol);

        final BS3Row propsRow = new BS3Row();
        bs3Grid.getRows().add(propsRow);

        // if no layout hints other than @MemberOrder
        addFieldSetsToColumn(propsRow, 4, Arrays.asList("General"), true);

        final BS3Col col = new BS3Col();
        col.setUnreferencedCollections(true);
        col.setSpan(12);
        propsRow.getCols().add(col);

        return bs3Grid;
    }

    private static BS3Grid withDomainClass(BS3Grid bs3Grid, Class<?> domainClass) {
        bs3Grid.setDomainClass(domainClass);
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


    private static final class GridVisitorResult {
        BS3Col colForUnreferencedActionsRef;
        BS3Col colForUnreferencedCollectionsRef;
        FieldSet fieldSetForUnreferencedActionsRef;
        FieldSet fieldSetForUnreferencedPropertiesRef;
        BS3TabGroup tabGroupForUnreferencedCollectionsRef;    
    }

    /**
     * Mandatory hook method defined in {@link GridSystemServiceAbstract superclass}, called by {@link GridSystemServiceAbstract#normalize(Grid, Class)} }.
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
                        objectSpec.streamObjectActions(Contributed.INCLUDED));

        final BS3Grid bs3Grid = (BS3Grid) grid;

        final LinkedHashMap<String, PropertyLayoutData> propertyLayoutDataById = bs3Grid.getAllPropertiesById();
        final LinkedHashMap<String, CollectionLayoutData> collectionLayoutDataById = bs3Grid.getAllCollectionsById();
        final LinkedHashMap<String, ActionLayoutData> actionLayoutDataById = bs3Grid.getAllActionsById();


        // find all row and col ids
        // ensure that all Ids are different

        final List<String> gridIds = _Lists.newArrayList();
        final LinkedHashMap<String, BS3Row> rowIds = _Maps.newLinkedHashMap();
        final LinkedHashMap<String, BS3Col> colIds = _Maps.newLinkedHashMap();
        final LinkedHashMap<String, FieldSet> fieldSetIds = _Maps.newLinkedHashMap();

        // fast (non-threadsafe) value reference
        final boolean[] duplicateIdDetected = {false}; 

        bs3Grid.visit(new BS3Grid.VisitorAdapter(){
            @Override
            public void visit(final BS3Row bs3Row) {
                final String id = bs3Row.getId();
                if(id == null) {
                    return;
                }
                if(gridIds.contains(id)) {
                    bs3Row.setMetadataError("There is another element in the grid with this id");
                    duplicateIdDetected[0] = true;
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
                    duplicateIdDetected[0] = true;
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
                    duplicateIdDetected[0] = true;
                    return;
                }
                fieldSetIds.put(id, fieldSet);
                gridIds.add(id);
            }
        });

        if(duplicateIdDetected[0]) {
            return false;
        }


        // ensure that there is exactly one col with the
        // unreferencedActions, unreferencedProperties and unreferencedCollections attribute set.


        final GridVisitorResult result = new GridVisitorResult();

        bs3Grid.visit(new BS3Grid.VisitorAdapter(){

            @Override
            public void visit(final BS3Col bs3Col) {
                if(isSet(bs3Col.isUnreferencedActions())) {
                    if(result.colForUnreferencedActionsRef != null) {
                        bs3Col.setMetadataError("More than one col with 'unreferencedActions' attribute set");
                    } else if(result.fieldSetForUnreferencedActionsRef != null) {
                        bs3Col.setMetadataError("Already found a fieldset with 'unreferencedActions' attribute set");
                    } else {
                        result.colForUnreferencedActionsRef=bs3Col;
                    }
                }
                if(isSet(bs3Col.isUnreferencedCollections())) {
                    if(result.colForUnreferencedCollectionsRef != null) {
                        bs3Col.setMetadataError("More than one col with 'unreferencedCollections' attribute set");
                    } else if(result.tabGroupForUnreferencedCollectionsRef != null) {
                        bs3Col.setMetadataError("Already found a tabgroup with 'unreferencedCollections' attribute set");
                    } else {
                        result.colForUnreferencedCollectionsRef = bs3Col;
                    }
                }
            }

            @Override
            public void visit(final FieldSet fieldSet) {
                if(isSet(fieldSet.isUnreferencedActions())) {
                    if(result.fieldSetForUnreferencedActionsRef != null) {
                        fieldSet.setMetadataError("More than one fieldset with 'unreferencedActions' attribute set");
                    } else if(result.colForUnreferencedActionsRef != null) {
                        fieldSet.setMetadataError("Already found a column with 'unreferencedActions' attribute set");
                    } else {
                        result.fieldSetForUnreferencedActionsRef = fieldSet;
                    }
                }
                if(isSet(fieldSet.isUnreferencedProperties())) {
                    if(result.fieldSetForUnreferencedPropertiesRef != null) {
                        fieldSet.setMetadataError("More than one column with 'unreferencedProperties' attribute set");
                    } else {
                        result.fieldSetForUnreferencedPropertiesRef = fieldSet;
                    }
                }
            }

            @Override
            public void visit(final BS3TabGroup bs3TabGroup) {
                if(isSet(bs3TabGroup.isUnreferencedCollections())) {
                    if(result.tabGroupForUnreferencedCollectionsRef != null) {
                        bs3TabGroup.setMetadataError("More than one tabgroup with 'unreferencedCollections' attribute set");
                    } else if(result.colForUnreferencedCollectionsRef != null) {
                        bs3TabGroup.setMetadataError("Already found a column with 'unreferencedCollections' attribute set");
                    } else {
                        result.tabGroupForUnreferencedCollectionsRef = bs3TabGroup;
                    }
                }
            }
        });

        if(result.colForUnreferencedActionsRef == null && result.fieldSetForUnreferencedActionsRef == null) {
            bs3Grid.getMetadataErrors().add("No column and also no fieldset found with the 'unreferencedActions' attribute set");
        }
        if(result.fieldSetForUnreferencedPropertiesRef == null) {
            bs3Grid.getMetadataErrors().add("No fieldset found with the 'unreferencedProperties' attribute set");
        }
        if(result.colForUnreferencedCollectionsRef == null && result.tabGroupForUnreferencedCollectionsRef == null) {
            bs3Grid.getMetadataErrors().add("No column and also no tabgroup found with the 'unreferencedCollections' attribute set");
        }

        if(     result.colForUnreferencedActionsRef == null && result.fieldSetForUnreferencedActionsRef == null ||
                result.fieldSetForUnreferencedPropertiesRef == null ||
                result.colForUnreferencedCollectionsRef == null && result.tabGroupForUnreferencedCollectionsRef == null) {
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
        final Map<String, Set<String>> boundAssociationIdsByFieldSetId = _Maps.newHashMap();

        // all those explicitly in the grid
        for (FieldSet fieldSet : fieldSetIds.values()) {
            final String fieldSetId = fieldSet.getId();
            Set<String> boundAssociationIds = boundAssociationIdsByFieldSetId.get(fieldSetId);
            if(boundAssociationIds == null) {
                boundAssociationIds = stream(fieldSet.getProperties())
                        .map(PropertyLayoutData::getId)
                        .collect(Collectors.toCollection(_Sets::newLinkedHashSet));
                boundAssociationIdsByFieldSetId.put(fieldSetId, boundAssociationIds);
            }
        }

        // 1-to-1-association Ids, that want to contribute to the 'metadata' FieldSet
        // but are unbound, because such a FieldSet is not defined by the given layout.
        val unboundMetadataContributingIds = _Sets.<String>newHashSet();

        // along with any specified by existing metadata
        for (OneToOneAssociation otoa : oneToOneAssociationById.values()) {
            final MemberOrderFacet memberOrderFacet = otoa.getFacet(MemberOrderFacet.class);
            if(memberOrderFacet != null) {
                val id = asId(memberOrderFacet.name());
                if(fieldSetIds.containsKey(id)) {
                    Set<String> boundAssociationIds =
                            boundAssociationIdsByFieldSetId.computeIfAbsent(id, k -> _Sets.newLinkedHashSet());
                    boundAssociationIds.add(otoa.getId());
                } else if(id.equals(MixinConstants.METADATA_LAYOUT_GROUPNAME)) {
                    unboundMetadataContributingIds.add(otoa.getId());
                }
            }
        }

        if(!missingPropertyIds.isEmpty()) {

            val unboundPropertyIds = _Lists.newArrayList(missingPropertyIds);

            for (final String fieldSetId : boundAssociationIdsByFieldSetId.keySet()) {
                val boundPropertyIds = boundAssociationIdsByFieldSetId.get(fieldSetId);
                unboundPropertyIds.removeAll(boundPropertyIds);
            }

            for (final String fieldSetId : boundAssociationIdsByFieldSetId.keySet()) {
                val fieldSet = fieldSetIds.get(fieldSetId);
                val associationIds =
                        boundAssociationIdsByFieldSetId.get(fieldSetId);

                val associations1To1 =
                        associationIds.stream()
                        .map(oneToOneAssociationById::get)
                        .filter(_NullSafe::isPresent)
                        .sorted(ObjectMember.Comparators.byMemberOrderSequence())
                        .collect(Collectors.toList());

                addPropertiesTo(fieldSet,
                        _Lists.map(associations1To1, ObjectAssociation::getId),
                        propertyLayoutDataById);
            }

            if(!unboundPropertyIds.isEmpty()) {
                val fieldSet = result.fieldSetForUnreferencedPropertiesRef;
                if(fieldSet != null) {
                    unboundPropertyIds.removeAll(unboundMetadataContributingIds);
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
            final List<OneToManyAssociation> sortedCollections = 
                    _Lists.map(missingCollectionIds, oneToManyAssociationById::get);
            {
                sortedCollections.sort(ObjectMember.Comparators.byMemberOrderSequence());
            }

            final List<String> sortedMissingCollectionIds = 
                    _Lists.map(sortedCollections, ObjectAssociation::getId);

            final BS3TabGroup bs3TabGroup = result.tabGroupForUnreferencedCollectionsRef;
            if(bs3TabGroup != null) {
                addCollectionsTo(bs3TabGroup, sortedMissingCollectionIds, objectSpec);
            } else {
                final BS3Col bs3Col = result.colForUnreferencedCollectionsRef;
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
        final List<String> associatedActionIds = _Lists.newArrayList();

        final List<ObjectAction> sortedPossiblyMissingActions = 
                _Lists.map(possiblyMissingActionIds, objectActionById::get);
        {
            sortedPossiblyMissingActions
            .sort(ObjectMember.Comparators.byMemberOrderSequence());
        }


        final List<String> sortedPossiblyMissingActionIds =
                _Lists.map(sortedPossiblyMissingActions, ObjectMember::getId);

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
            }
        }

        // ... the missing actions are those in the second tuple, excluding those associated (via @MemberOrder#name)
        // to a property or collection.
        final List<String> missingActionIds = _Lists.newArrayList(sortedPossiblyMissingActionIds);
        missingActionIds.removeAll(associatedActionIds);

        for (String surplusActionId : surplusActionIds) {
            actionLayoutDataById.get(surplusActionId).setMetadataError("No such action");
        }

        if(!missingActionIds.isEmpty()) {
            final BS3Col bs3Col = result.colForUnreferencedActionsRef;
            if(bs3Col != null) {
                addActionsTo(bs3Col, missingActionIds, actionLayoutDataById);
            } else {
                final FieldSet fieldSet = result.fieldSetForUnreferencedActionsRef;
                if(fieldSet != null) {
                    addActionsTo(fieldSet, missingActionIds, actionLayoutDataById);
                }
            }
        }

        return true;
    }

    private void addPropertiesTo(
            final FieldSet fieldSet,
            final List<String> propertyIds,
            final LinkedHashMap<String, PropertyLayoutData> propertyLayoutDataById) {

        final Set<String> existingIds =
                stream(fieldSet.getProperties())
                .map(PropertyLayoutData::getId)
                .collect(Collectors.toSet());

        for (final String propertyId : propertyIds) {
            if(!existingIds.contains(propertyId)) {
                final PropertyLayoutData propertyLayoutData = new PropertyLayoutData(propertyId);
                fieldSet.getProperties().add(propertyLayoutData);
                propertyLayoutData.setOwner(fieldSet);
                propertyLayoutDataById.put(propertyId, propertyLayoutData);
            }
        }
    }

    private void addCollectionsTo(
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

    private void addCollectionsTo(
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

    private void addActionsTo(
            final FieldSet fieldSet,
            final List<String> actionIds,
            final LinkedHashMap<String, ActionLayoutData> actionLayoutDataById) {
        for (String actionId : actionIds) {
            final ActionLayoutData actionLayoutData = new ActionLayoutData(actionId);
            addActionTo(fieldSet, actionLayoutData);
            actionLayoutDataById.put(actionId, actionLayoutData);
        }
    }

    private void addActionTo(
            final ActionLayoutDataOwner owner,
            final ActionLayoutData actionLayoutData) {
        List<ActionLayoutData> actions = owner.getActions();
        if(actions == null) {
            owner.setActions(actions = _Lists.newArrayList());
        }
        actions.add(actionLayoutData);
        actionLayoutData.setOwner(owner);
    }

    private static Boolean isSet(final Boolean flag) {
        return flag != null && flag;
    }

    private static String asId(final String str) {
        if(_Strings.isNullOrEmpty(str)) {
            return str;
        }
        final char c = str.charAt(0);
        return Character.toLowerCase(c) + str.substring(1).replaceAll("\\s+", "");
    }

    // -- DEPENDENCIES

    @Inject GridReaderUsingJaxb gridReader;


}
