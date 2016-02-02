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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.layout.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.bootstrap3.BS3TabGroup;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.Grid;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.applib.layout.fixedcols.FCColumn;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.services.grid.GridNormalizerServiceAbstract;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GridNormalizerServiceBS3 extends GridNormalizerServiceAbstract<BS3Grid> {

    public static final String TNS = "http://isis.apache.org/schema/applib/layout/bootstrap3";
    public static final String SCHEMA_LOCATION = "http://isis.apache.org/schema/applib/layout/bootstrap3/bootstrap3.xsd";

    public GridNormalizerServiceBS3() {
        super(BS3Grid.class, TNS, SCHEMA_LOCATION);
    }


    @Override
    protected boolean validateAndDerive(
            final Grid grid,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById) {

        final BS3Grid bs3Grid = (BS3Grid) grid;

        final LinkedHashMap<String, PropertyLayoutData> propertyIds = bs3Grid.getAllPropertiesById();
        final LinkedHashMap<String, CollectionLayoutData> collectionIds = bs3Grid.getAllCollectionsById();
        final LinkedHashMap<String, ActionLayoutData> actionIds = bs3Grid.getAllActionsById();


        // find all row and col ids
        // ensure that all Ids are different

        final LinkedHashMap<String, BS3Row> rowIds = Maps.newLinkedHashMap();
        final LinkedHashMap<String, BS3Col> colIds = Maps.newLinkedHashMap();

        final AtomicReference<Boolean> duplicateIdDetected = new AtomicReference<>(false);

        bs3Grid.visit(new BS3Grid.VisitorAdapter(){
            @Override
            public void visit(final BS3Row bs3Row) {
                final String id = bs3Row.getId();
                if(id == null) {
                    return;
                }
                if(rowIds.containsKey(id) || colIds.containsKey(id)) {
                    bs3Row.setMetadataError("There is another col with this id");
                    duplicateIdDetected.set(true);
                    return;
                }
                rowIds.put(id, bs3Row);
            }

            @Override
            public void visit(final BS3Col bs3Col) {
                final String id = bs3Col.getId();
                if(id == null) {
                    return;
                }
                if(rowIds.containsKey(id) || colIds.containsKey(id)) {
                    bs3Col.setMetadataError("There is another col with this id");
                    duplicateIdDetected.set(true);
                    return;
                }
                colIds.put(id, bs3Col);
            }
        });

        if(duplicateIdDetected.get()) {
            return false;
        }


        // ensure that there is exactly one col with the
        // unreferencedActions, unreferencedProperties and unreferencedCollections attribute set.

        final AtomicReference<BS3Col> colForUnreferencedActionsRef = new AtomicReference<>();
        final AtomicReference<BS3Col> colForUnreferencedPropertiesRef = new AtomicReference<>();
        final AtomicReference<FieldSet> fieldSetForUnreferencedPropsRef = new AtomicReference<>();
        final AtomicReference<BS3Col> colForUnreferencedCollectionsRef = new AtomicReference<>();

        bs3Grid.visit(new BS3Grid.VisitorAdapter(){
            @Override
            public void visit(final BS3Col bs3Col) {
                if(bs3Col.isUnreferencedActions()) {
                    if(colForUnreferencedActionsRef.get() != null) {
                        bs3Col.setMetadataError("More than one col with 'unreferencedActions' attribute set");
                    } else {
                        colForUnreferencedActionsRef.set(bs3Col);
                    }
                }
                if(bs3Col.isUnreferencedProperties()) {
                    if(colForUnreferencedPropertiesRef.get() != null) {
                        bs3Col.setMetadataError("More than one col with 'unreferencedProperties' attribute set");
                    } else {
                        final List<FieldSet> fieldSets = bs3Col.getFieldSets();
                        for (FieldSet fieldSet : fieldSets) {
                            if(fieldSet.getName().equals(MemberGroupLayoutFacet.DEFAULT_GROUP)) {
                                fieldSetForUnreferencedPropsRef.set(fieldSet);
                            }
                        }
                        colForUnreferencedPropertiesRef.set(bs3Col);
                    }
                }
                if(bs3Col.isUnreferencedCollections()) {
                    if(colForUnreferencedCollectionsRef.get() != null) {
                        bs3Col.setMetadataError("More than one col with 'unreferencedCollections' attribute set");
                    } else {
                        colForUnreferencedCollectionsRef.set(bs3Col);
                    }
                }
            }
        });

        if(     colForUnreferencedActionsRef.get() == null ||
                colForUnreferencedPropertiesRef.get() == null ||
                colForUnreferencedCollectionsRef.get() == null) {
            return false;
        }


        // add missing properties will be added to the first fieldset of the specified column
        final Tuple<List<String>> propertyIdTuple =
                surplusAndMissing(propertyIds.keySet(), oneToOneAssociationById.keySet());
        final List<String> surplusPropertyIds = propertyIdTuple.first;
        final List<String> missingPropertyIds = propertyIdTuple.second;

        for (String surplusPropertyId : surplusPropertyIds) {
            propertyIds.get(surplusPropertyId).setMetadataError("No such property");
        }

        if(!missingPropertyIds.isEmpty()) {
            final BS3Col bs3Col = colForUnreferencedPropertiesRef.get();
            if(bs3Col != null) {
                // ensure that there is a field set to use, else create
                boolean wasSet = fieldSetForUnreferencedPropsRef.compareAndSet(null, new FieldSet(MemberGroupLayoutFacet.DEFAULT_GROUP));
                final FieldSet fieldSetForUnref = fieldSetForUnreferencedPropsRef.get();
                if(wasSet) {
                    fieldSetForUnref.setOwner(bs3Col);
                    bs3Col.getFieldSets().add(fieldSetForUnref);
                }
                for (final String propertyId : missingPropertyIds) {
                    bs3Col.getFieldSets().get(0).getProperties().add(new PropertyLayoutData(propertyId));
                }
            }
        }

        // any missing collections will be added as tabs to a new TabGroup in the specified column
        final Tuple<List<String>> collectionIdTuple =
                surplusAndMissing(collectionIds.keySet(), oneToManyAssociationById.keySet());
        final List<String> surplusCollectionIds = collectionIdTuple.first;
        final List<String> missingCollectionIds = collectionIdTuple.second;

        for (String surplusCollectionId : surplusCollectionIds) {
            collectionIds.get(surplusCollectionId).setMetadataError("No such collection");
        }

        if(!missingCollectionIds.isEmpty()) {
            final BS3Col bs3Col = colForUnreferencedCollectionsRef.get();
            if(bs3Col != null) {
                final BS3TabGroup tabGroup = new BS3TabGroup();
                tabGroup.setOwner(bs3Col);
                bs3Col.getTabGroups().add(tabGroup);
                for (final String collectionId : missingCollectionIds) {
                    final BS3Tab bs3Tab = new BS3Tab();
                    tabGroup.getTabs().add(bs3Tab);
                    FCColumn left = new FCColumn(12);
                    bs3Tab.setOwner(tabGroup);
                    final CollectionLayoutData layoutMetadata = new CollectionLayoutData(collectionId);
                    layoutMetadata.setDefaultView("table");
                    left.getCollections().add(layoutMetadata);
                }
            }
        }

        // any missing actions will be added as actions in the specified column
        final Tuple<List<String>> actionIdTuple =
                surplusAndMissing(actionIds.keySet(), objectActionById.keySet());
        final List<String> surplusActionIds = actionIdTuple.first;
        final List<String> missingActionIds = actionIdTuple.second;

        for (String surplusActionId : surplusActionIds) {
            actionIds.get(surplusActionId).setMetadataError("No such action");
        }

        if(!missingActionIds.isEmpty()) {
            final BS3Col bs3Col = colForUnreferencedActionsRef.get();
            if(bs3Col != null) {
                for (String actionId : missingActionIds) {
                    List<ActionLayoutData> actions = bs3Col.getActions();
                    if(actions == null) {
                        actions = Lists.newArrayList();
                        bs3Col.setActions(actions);
                    }
                    actions.add(new ActionLayoutData(actionId));
                }
            }
        }

        return true;
    }

}
