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

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3RowContent;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Tab;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3TabGroup;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.services.grid.GridNormalizerServiceAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GridNormalizerServiceBS3 extends GridNormalizerServiceAbstract<BS3Grid> {

    public static final String TNS = "http://isis.apache.org/applib/layout/grid/bootstrap3";
    public static final String SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/grid/bootstrap3/bootstrap3.xsd";

    public GridNormalizerServiceBS3() {
        super(BS3Grid.class, TNS, SCHEMA_LOCATION);
    }


    @Override
    protected boolean validateAndDerive(
            final Grid grid,
            final Map<String, OneToOneAssociation> oneToOneAssociationById,
            final Map<String, OneToManyAssociation> oneToManyAssociationById,
            final Map<String, ObjectAction> objectActionById, final ObjectSpecification objectSpec) {

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
                surplusAndMissing(propertyIds.keySet(),  oneToOneAssociationById.keySet());
        final List<String> surplusPropertyIds = propertyIdTuple.first;
        final List<String> missingPropertyIds = propertyIdTuple.second;

        for (String surplusPropertyId : surplusPropertyIds) {
            propertyIds.get(surplusPropertyId).setMetadataError("No such property");
        }

        if(!missingPropertyIds.isEmpty()) {
            final FieldSet fieldSet = fieldSetForUnreferencedPropertiesRef.get();
            if(fieldSet != null) {
                addMissingPropertiesTo(fieldSet, missingPropertyIds);
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
            final BS3TabGroup bs3TabGroup = tabGroupForUnreferencedCollectionsRef.get();
            if(bs3TabGroup != null) {
                addMissingCollectionsTo(bs3TabGroup, missingCollectionIds, objectSpec);
            } else {
                final BS3Col bs3Col = colForUnreferencedCollectionsRef.get();
                if(bs3Col != null) {
                    addMissingCollectionsTo(bs3Col, missingCollectionIds);
                }
            }
        }

        // any missing actions will be added as actions in the specified column
        final Tuple<List<String>> actionIdTuple =
                surplusAndMissing(actionIds.keySet(), objectActionById.keySet());
        final List<String> surplusActionIds = actionIdTuple.first;

        // ... the missing actions are those in the second tuple, excluding those bound via @MemberOrder#name
        // to a property or collection.
        final List<String> missingActionIds =
                FluentIterable.from(actionIdTuple.second)
                        .filter(new Predicate<String>() {
                            @Override public boolean apply(@Nullable final String actionId) {
                                final ObjectAction oa = objectActionById.get(actionId);
                                final MemberOrderFacet memberOrderFacet = oa.getFacet(MemberOrderFacet.class);
                                if(memberOrderFacet == null) {
                                    return true;
                                }
                                final String memberOrderName = memberOrderFacet.name();
                                if (memberOrderName == null) {
                                    return true;
                                }
                                return  !oneToOneAssociationById.containsKey(memberOrderName) &&
                                        !oneToManyAssociationById.containsKey(memberOrderName);
                            }
                        })
                        .toList();

        for (String surplusActionId : surplusActionIds) {
            actionIds.get(surplusActionId).setMetadataError("No such action");
        }

        if(!missingActionIds.isEmpty()) {
            final BS3Col bs3Col = colForUnreferencedActionsRef.get();
            if(bs3Col != null) {
                addMissingActionsTo(bs3Col, missingActionIds);
            } else {
                final FieldSet fieldSet = fieldSetForUnreferencedActionsRef.get();
                if(fieldSet != null) {
                    addMissingActionsTo(fieldSet, missingActionIds);
                }
            }
        }

        return true;
    }

    protected void addMissingPropertiesTo(
            final FieldSet fieldSet,
            final List<String> missingPropertyIds) {
        for (final String propertyId : missingPropertyIds) {
            fieldSet.getProperties().add(new PropertyLayoutData(propertyId));
        }
    }

    protected void addMissingActionsTo(final BS3Col bs3Col, final List<String> missingActionIds) {
        for (String actionId : missingActionIds) {
            List<ActionLayoutData> actions = bs3Col.getActions();
            if(actions == null) {
                actions = Lists.newArrayList();
                bs3Col.setActions(actions);
            }
            actions.add(new ActionLayoutData(actionId));
        }
    }

    protected void addMissingActionsTo(final FieldSet fieldSet, final List<String> missingActionIds) {
        List<ActionLayoutData> actions = fieldSet.getActions();
        for (String actionId : missingActionIds) {
            actions.add(new ActionLayoutData(actionId));
        }
    }

    protected void addMissingCollectionsTo(
            final BS3Col tabRowCol,
            final List<String> missingCollectionIds) {
        for (final String collectionId : missingCollectionIds) {
            final CollectionLayoutData layoutMetadata = new CollectionLayoutData(collectionId);
            layoutMetadata.setDefaultView("table");
            tabRowCol.getCollections().add(layoutMetadata);
        }
    }

    protected void addMissingCollectionsTo(
            final BS3TabGroup tabGroup,
            final List<String> missingCollectionIds,
            final ObjectSpecification objectSpec) {
        for (final String collectionId : missingCollectionIds) {
            final BS3Tab bs3Tab = new BS3Tab();
            bs3Tab.setName(objectSpec.getAssociation(collectionId).getName());
            tabGroup.getTabs().add(bs3Tab);
            bs3Tab.setOwner(tabGroup);

            final BS3Row tabRow = new BS3Row();
            tabRow.setOwner(bs3Tab);
            bs3Tab.getRows().add(tabRow);

            final BS3Col tabRowCol = new BS3Col();
            tabRowCol.setSpan(12);
            tabRowCol.setSize(BS3RowContent.Size.MD);
            tabRowCol.setOwner(tabRow);
            tabRow.getCols().add(tabRowCol);

            final CollectionLayoutData layoutMetadata = new CollectionLayoutData(collectionId);
            layoutMetadata.setDefaultView("table");
            tabRowCol.getCollections().add(layoutMetadata);
        }
    }

    private static Boolean isSet(final Boolean flag) {
        return flag != null && flag;
    }

}
