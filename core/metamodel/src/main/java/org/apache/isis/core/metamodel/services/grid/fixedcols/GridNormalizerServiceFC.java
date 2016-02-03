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
package org.apache.isis.core.metamodel.services.grid.fixedcols;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.Grid;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.applib.layout.fixedcols.FCColumn;
import org.apache.isis.applib.layout.fixedcols.FCGrid;
import org.apache.isis.applib.layout.fixedcols.FCTab;
import org.apache.isis.applib.layout.fixedcols.FCTabGroup;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.services.grid.GridNormalizerServiceAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GridNormalizerServiceFC extends GridNormalizerServiceAbstract {

    public static final String TNS = "http://isis.apache.org/schema/applib/layout/fixedcols";
    public static final String SCHEMA_LOCATION = "http://isis.apache.org/schema/applib/layout/fixedcols/fixedcols.xsd";

    public GridNormalizerServiceFC() {
        super(FCGrid.class, TNS, SCHEMA_LOCATION);
    }


    /**
     * Ensures that all object members (properties, collections and actions) are in the metadata.
     *
     * <p>
     *     If they are missing then they will be added to default tabs (created on the fly if need be).
     * </p>
     */
    @Override
    protected boolean validateAndDerive(
            final Grid grid,
            final Map oneToOneAssociationById,
            final Map oneToManyAssociationById,
            final Map objectActionById, final ObjectSpecification objectSpec) {

        final FCGrid fcGrid = (FCGrid) grid;

        final LinkedHashMap<String, PropertyLayoutData> propertyIds = fcGrid.getAllPropertiesById();
        final LinkedHashMap<String, CollectionLayoutData> collectionIds = fcGrid.getAllCollectionsById();
        final LinkedHashMap<String, ActionLayoutData> actionIds = fcGrid.getAllActionsById();

        final AtomicReference<FieldSet> defaultFieldSetRef = new AtomicReference<>();
        final AtomicReference<FCColumn> firstColumnRef = new AtomicReference<>();
        final AtomicReference<FCTabGroup> lastTabGroupRef = new AtomicReference<>();

        // capture the first column, and also
        // capture the first property group (if any) with the default name ('General')
        fcGrid.visit(new FCGrid.VisitorAdapter() {
            @Override
            public void visit(final FCColumn fcColumn) {
                firstColumnRef.compareAndSet(null, fcColumn);
            }
            @Override
            public void visit(final FieldSet fieldSet) {
                if(MemberGroupLayoutFacet.DEFAULT_GROUP.equals(fieldSet.getName())) {
                    defaultFieldSetRef.compareAndSet(null, fieldSet);
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
            boolean wasSet = defaultFieldSetRef.compareAndSet(null, new FieldSet(MemberGroupLayoutFacet.DEFAULT_GROUP));
            final FieldSet defaultFieldSet = defaultFieldSetRef.get();
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
            while(fcGrid.getTabGroups().size() < 2) {
                final FCTabGroup tabGroup = new FCTabGroup();
                fcGrid.getTabGroups().add(tabGroup);
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
                List<ActionLayoutData> actions = fcGrid.getActions();
                if(actions == null) {
                    actions = Lists.newArrayList();
                    fcGrid.setActions(actions);
                }
                actions.add(new ActionLayoutData(actionId));
            }
        }
        return true;
    }


}
