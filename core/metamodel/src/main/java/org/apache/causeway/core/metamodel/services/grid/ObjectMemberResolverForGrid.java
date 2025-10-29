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
package org.apache.causeway.core.metamodel.services.grid;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.ActionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSElement.BSElementVisitor;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.applib.layout.grid.bootstrap.Size;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.functional.Either;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actions.layout.ActionPositionFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.CssClassFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.FaFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.HiddenFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberDescribedFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberNamedFacetForActionLayoutXml;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.causeway.core.metamodel.facets.collections.layout.CssClassFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.DefaultViewFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.HiddenFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.MemberDescribedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.MemberNamedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.PagedFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.SortedByFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.collections.layout.tabledec.TableDecoratorFacetForCollectionLayoutXml;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetForLayoutXml;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetForLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.BookmarkPolicyFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.CssClassFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.FaFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.ObjectDescribedFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.ObjectNamedFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.tabledec.TableDecoratorFacetForDomainObjectLayoutXml;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.CssClassFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.HiddenFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.LabelAtFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MemberDescribedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MemberNamedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.MultiLineFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.RenderedAdjustedFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.TypicalLengthFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.facets.properties.propertylayout.UnchangingFacetForPropertyLayoutXml;
import org.apache.causeway.core.metamodel.layout.LayoutFacetUtil.LayoutDataFactory;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import static org.apache.causeway.commons.internal.base._NullSafe.stream;
import static org.apache.causeway.core.metamodel.facetapi.FacetUtil.updateFacet;
import static org.apache.causeway.core.metamodel.facetapi.FacetUtil.updateFacetIfPresent;

import lombok.extern.slf4j.Slf4j;

/**
 * Associates domain object members into the various regions
 * of the grid.
 */
@Slf4j
record ObjectMemberResolverForGrid(
    GridLoadingContext context) {

    /**
     * Returns either a valid (left) or invalid (right) {@link BSGrid}
     */
    public Either<BSGrid, BSGrid> resolve(final BSGrid grid, final Class<?> domainClass) {
        final boolean valid = validateAndNormalize(grid, domainClass);
        if (valid) {
            overwriteFacets(grid, domainClass);
            if(log.isDebugEnabled()) {
                log.debug("Grid:\n\n{}\n\n", toXml(grid));
            }
            return Either.left(grid);
        }
        return Either.right(grid);
    }

    // -- HELPER

    private String toXml(final BSGrid grid) {
        return context().gridMarshaller(CommonMimeType.XML).orElseThrow()
            .marshal(grid, CommonMimeType.XML);
    }

    /**
     * We must ensure that all object members (properties, collections
     * and actions) are in the grid metadata, typically by deriving this information from other existing metadata
     * (eg facets from annotations) or just by applying default rules.
     */
    private boolean validateAndNormalize(
            final BSGrid bsGrid,
            final Class<?> domainClass) {

        var gridModelIfValid = GridInitializationModel.createFrom(bsGrid);
        if(!gridModelIfValid.isPresent()) return false; // only present if valid

        var gridModel = gridModelIfValid.get();
        var objSpec = context().specLoaderProvider().get().specForTypeElseFail(domainClass);

        var oneToOneAssociationById = ObjectMember.mapById(objSpec.streamProperties(MixedIn.INCLUDED));
        var oneToManyAssociationById = ObjectMember.mapById(objSpec.streamCollections(MixedIn.INCLUDED));
        var objectActionById = ObjectMember.mapById(objSpec.streamRuntimeActions(MixedIn.INCLUDED));

        var layoutDataFactory = new LayoutDataFactory(objSpec);

        // * left  ... those defined in the grid model but not available with the meta-model
        // * right ... those available with the meta-model but missing in the grid-model
        // (missing properties will be added to the first field-set of the specified column)

        var propertyDisjunction = gridModel.propertyDisjunction(oneToOneAssociationById.keySet());
        for (String leftPropertyId : propertyDisjunction.left()) {
            gridModel.propertyLayoutDataById.get(leftPropertyId)
                .forEach(it->it.setMetadataError("No such property"));
        }

        // catalog which associations are bound to an existing field-set
        // so that (below) we can determine which missing property IDs are not unbound vs
        // which should be included in the field-set that they are bound to.
        var boundAssociationIdsByFieldSetId = new HashMap<String, Set<String>>();

        for (var fieldSet : gridModel.fieldSets()) {
            var fieldSetId = GroupIdAndName.forFieldSet(fieldSet)
                .orElseThrow(()->_Exceptions.illegalArgument("invalid fieldSet detected, "
                        + "requires at least an id or a name"))
                .id();
            Set<String> boundAssociationIds = boundAssociationIdsByFieldSetId.get(fieldSetId);
            if(boundAssociationIds == null) {
                boundAssociationIds = stream(fieldSet.getProperties())
                        .map(PropertyLayoutData::getId)
                        .collect(Collectors.toCollection(_Sets::newLinkedHashSet));
                boundAssociationIdsByFieldSetId.put(fieldSetId, boundAssociationIds);
            }
        }

        // 1-to-1-association IDs, that want to contribute to the 'metadata' FieldSet
        // but are unbound, because such a FieldSet is not defined by the given layout.
        var unboundMetadataContributingIds = new HashSet<String>();

        // along with any specified by existing metadata
        for (final OneToOneAssociation oneToOneAssociation : oneToOneAssociationById.values()) {
            var layoutGroupFacet = oneToOneAssociation.getFacet(LayoutGroupFacet.class);
            if(layoutGroupFacet == null) {
                continue;
            }
            var id = layoutGroupFacet.getGroupId();
            if(gridModel.containsFieldSetId(id)) {
                Set<String> boundAssociationIds =
                        boundAssociationIdsByFieldSetId.computeIfAbsent(id, k -> _Sets.newLinkedHashSet());
                boundAssociationIds.add(oneToOneAssociation.getId());
            } else if(id.equals(LayoutConstants.FieldSetId.METADATA)) {
                unboundMetadataContributingIds.add(oneToOneAssociation.getId());
            }
        }

        if(!propertyDisjunction.right().isEmpty()) {
            var unboundPropertyIds = _Sets.newLinkedHashSet(propertyDisjunction.right());

            for (final String fieldSetId : boundAssociationIdsByFieldSetId.keySet()) {
                var boundPropertyIds = boundAssociationIdsByFieldSetId.get(fieldSetId);
                unboundPropertyIds.removeAll(boundPropertyIds);
            }

            for (final String fieldSetId : boundAssociationIdsByFieldSetId.keySet()) {
                var fieldSet = gridModel.getFieldSet(fieldSetId);
                var associationIds = boundAssociationIdsByFieldSetId.get(fieldSetId);

                var associations1To1Ids =
                        associationIds.stream()
                        .map(oneToOneAssociationById::get)
                        .filter(_NullSafe::isPresent)
                        .sorted(ObjectMember.byMemberOrderSequence(false))
                        .map(ObjectAssociation::getId)
                        .collect(Collectors.toList());

                addPropertiesTo(
                        fieldSet,
                        associations1To1Ids,
                        layoutDataFactory::createPropertyLayoutData,
                        gridModel.propertyLayoutDataById::putElement);
            }

            if(!unboundPropertyIds.isEmpty()) {
                var fieldSet = gridModel.nodeForUnreferencedProperties();
                unboundPropertyIds.removeAll(unboundMetadataContributingIds);

                // add unbound properties respecting configured sequence policy
                var sortedUnboundPropertyIds = _UnreferencedSequenceUtil
                    .sortProperties(context.causewayConfiguration(), unboundPropertyIds.stream()
                            .map(oneToOneAssociationById::get)
                            .filter(_NullSafe::isPresent));

                addPropertiesTo(
                        fieldSet,
                        sortedUnboundPropertyIds,
                        layoutDataFactory::createPropertyLayoutData,
                        gridModel.propertyLayoutDataById::putElement);
            }
        }

        // any missing collections will be added as tabs to a new TabGroup in the specified column
        var collectionDisjunction = gridModel.collectionDisjunction(oneToManyAssociationById.keySet());
        for (String leftCollectionId : collectionDisjunction.left()) {
            gridModel.collectionLayoutDataById.get(leftCollectionId)
                .forEach(it->it.setMetadataError("No such collection"));
        }

        if(!collectionDisjunction.right().isEmpty()) {

            // add missing collections respecting configured sequence policy
            var sortedMissingCollectionIds = _UnreferencedSequenceUtil
                    .sortCollections(context.causewayConfiguration(), collectionDisjunction.right().stream()
                            .map(oneToManyAssociationById::get)
                            .filter(_NullSafe::isPresent));

            gridModel.nodeForUnreferencedCollections()
            .accept(
                bsCol->{
                    addUnreferencedCollectionsTo(
                        bsCol,
                        sortedMissingCollectionIds,
                        layoutDataFactory::createCollectionLayoutData,
                        gridModel.collectionLayoutDataById::putElement);
                },
                bsTabGroup->{
                    addUnreferencedCollectionsTo(
                        bsTabGroup,
                        sortedMissingCollectionIds,
                        objSpec,
                        layoutDataFactory::createCollectionLayoutData);
                });
        }

        // any missing actions will be added as actions in the specified column
        var actionDisjunction = gridModel.actionDisjunction(objectActionById.keySet());
        var possiblyMissingActionIds = actionDisjunction.right();

        final List<String> associatedActionIds = _Lists.newArrayList();

        final List<ObjectAction> sortedPossiblyMissingActions =
                _Lists.map(possiblyMissingActionIds, objectActionById::get);

        sortedPossiblyMissingActions.sort(ObjectMember.byMemberOrderSequence(false));

        final List<String> sortedPossiblyMissingActionIds =
                _Lists.map(sortedPossiblyMissingActions, ObjectMember::getId);

        for (final String actionId : sortedPossiblyMissingActionIds) {
            var objectAction = objectActionById.get(actionId);

            var layoutGroupFacet = objectAction.getFacet(LayoutGroupFacet.class);
            if(layoutGroupFacet == null) continue;

            final String layoutGroupName = layoutGroupFacet.getGroupId();
            if (layoutGroupName == null) continue;

            if (oneToOneAssociationById.containsKey(layoutGroupName)) {
                associatedActionIds.add(actionId);

                if(layoutGroupFacet.isExplicitBinding()) {
                    var propertyLayoutDataList = gridModel.propertyLayoutDataById.get(layoutGroupName);
                    if(_NullSafe.isEmpty(propertyLayoutDataList)) {
                        log.warn(String.format("Could not find propertyLayoutData for layoutGroupName of '%s'", layoutGroupName));
                        continue;
                    }
                    var actionPositionFacet = objectAction.getFacet(ActionPositionFacet.class);
                    final ActionLayout.Position position = actionPositionFacet != null
                        ? actionPositionFacet.position()
                        : ActionLayout.Position.BELOW;
                    propertyLayoutDataList.forEach(propertyLayoutData->{
                        final ActionLayoutDataOwner owner = position == ActionLayout.Position.PANEL
                                    || position == ActionLayout.Position.PANEL_DROPDOWN
                            ? propertyLayoutData.owner()
                            : propertyLayoutData;
                        var actionLayoutData = new ActionLayoutData(actionId);
                        actionLayoutData.setPosition(position);
                        addActionTo(owner, actionLayoutData);
                    });
                }
                continue;
            }
            if (oneToManyAssociationById.containsKey(layoutGroupName)) {
                associatedActionIds.add(actionId);

                if(layoutGroupFacet.isExplicitBinding()) {
                    var collectionLayoutDataList = gridModel.collectionLayoutDataById.get(layoutGroupName);
                    if(_NullSafe.isEmpty(collectionLayoutDataList)) {
                        log.warn("failed to lookup CollectionLayoutData by layoutGroupName '{}'", layoutGroupName);
                    } else {
                        collectionLayoutDataList.forEach(collectionLayoutData->
                            addActionTo(collectionLayoutData, new ActionLayoutData(actionId)));
                    }
                }
                continue;
            }
            // if the @ActionLayout for the action references a field set (that has bound
            // associations), then don't mark it as missing, but instead explicitly add it to the
            // list of actions of that field-set.
            final Set<String> boundAssociationIds = boundAssociationIdsByFieldSetId.get(layoutGroupName);
            if(boundAssociationIds != null && !boundAssociationIds.isEmpty()) {

                associatedActionIds.add(actionId);

                final ActionLayoutData actionLayoutData = new ActionLayoutData(actionId);

                // since the action is to be associated with a fieldSet, the only available positions are PANEL and PANEL_DROPDOWN.
                // if the action already has a preference for PANEL, then preserve it, otherwise default to PANEL_DROPDOWN
                var actionPositionFacet = objectAction.getFacet(ActionPositionFacet.class);
                if(actionPositionFacet != null && actionPositionFacet.position() == ActionLayout.Position.PANEL) {
                    actionLayoutData.setPosition(ActionLayout.Position.PANEL);
                } else {
                    actionLayoutData.setPosition(ActionLayout.Position.PANEL_DROPDOWN);
                }

                final FieldSet fieldSet = gridModel.getFieldSet(layoutGroupName);
                addActionTo(fieldSet, actionLayoutData);
            }
        }

        // ... the missing actions are those in the second tuple, excluding those associated
        // (via @Action#associateWith) to a property or collection. (XXX comment might be outdated)
        final List<String> missingActionIds = _Lists.newArrayList(sortedPossiblyMissingActionIds);
        missingActionIds.removeAll(associatedActionIds);

        for (String leftActionId : actionDisjunction.left()) {
            gridModel.actionLayoutDataById.get(leftActionId)
                .forEach(it->it.setMetadataError("No such action"));
        }

        if(!missingActionIds.isEmpty()) {
            gridModel.nodeForUnreferencedActions()
                .accept(
                    bsCol->{
                        addActionsTo(
                            bsCol,
                            missingActionIds,
                            layoutDataFactory::createActionLayoutData,
                            gridModel.actionLayoutDataById::putElement);
                    },
                    fieldSet->{
                        addActionsTo(
                            fieldSet,
                            missingActionIds,
                            layoutDataFactory::createActionLayoutData,
                            gridModel.actionLayoutDataById::putElement);
                    });
        }

        {
            // bind actions closest to properties
            final Set<String> actionIdsAlreadyAdded = bsGrid.streamActionLayoutData()
                .map(ActionLayoutData::getId)
                .collect(Collectors.toCollection(HashSet::new));

            objSpec.streamProperties(MixedIn.INCLUDED)
                .forEach(property->{
                    _NullSafe.stream(gridModel.propertyLayoutDataById.get(property.getId()))
                        .forEach(pl->{
                            ObjectAction.Util.findForAssociation(objSpec, property)
                                .map(ObjectAction::getId)
                                .filter(id->!actionIdsAlreadyAdded.contains(id))
                                //.peek(actionIdsAlreadyAdded::add)
                                .map(ActionLayoutData::new)
                                .forEach(pl.getActions()::add);
                        });
                });
        }

        bsGrid.valid(true);
        return true;
    }

    private void addPropertiesTo(
            final FieldSet fieldSet,
            final Collection<String> propertyIds,
            final Function<String, PropertyLayoutData> layoutFactory,
            final BiConsumer<String, PropertyLayoutData> onNewLayoutData) {

        final Set<String> existingIds =
                stream(fieldSet.getProperties())
                .map(PropertyLayoutData::getId)
                .collect(Collectors.toSet());

        for (final String propertyId : propertyIds) {
            if(existingIds.contains(propertyId)) {
                continue;
            }
            var propertyLayoutData = layoutFactory.apply(propertyId);
            fieldSet.getProperties().add(propertyLayoutData);
            propertyLayoutData.owner(fieldSet);
            onNewLayoutData.accept(propertyId, propertyLayoutData);
        }
    }

    private void addUnreferencedCollectionsTo(
            final BSCol tabRowCol,
            final Collection<String> collectionIds,
            final Function<String, CollectionLayoutData> layoutFactory,
            final BiConsumer<String, CollectionLayoutData> onNewLayoutData) {

        for (final String collectionId : collectionIds) {
            var collectionLayoutData = layoutFactory.apply(collectionId);
            tabRowCol.getCollections().add(collectionLayoutData);
            onNewLayoutData.accept(collectionId, collectionLayoutData);
        }
    }

    private void addUnreferencedCollectionsTo(
            final BSTabGroup tabGroup,
            final Collection<String> collectionIds,
            final ObjectSpecification objectSpec,
            final Function<String, CollectionLayoutData> layoutFactory) {

        // prevent multiple tabs with the same name
        var tabsByName = new HashMap<String, BSTab>();
        tabGroup.getTabs().forEach(tab->tabsByName.put(tab.getName(), tab));

        for (final String collectionId : collectionIds) {
            var feature = objectSpec.getCollectionElseFail(collectionId);
            var featureCanonicalFriendlyName = feature.getCanonicalFriendlyName();

            final BSTab bsTab = tabsByName.computeIfAbsent(featureCanonicalFriendlyName, __->{
                var newTab = new BSTab();
                newTab.setName(featureCanonicalFriendlyName);
                tabGroup.getTabs().add(newTab);
                newTab.owner(tabGroup);
                return newTab;
            });

            final BSRow tabRow = new BSRow();
            bsTab.getRows().add(tabRow);

            final BSCol tabRowCol = new BSCol();
            tabRowCol.setSpan(12);
            tabRowCol.setSize(Size.MD);
            tabRow.getRowContents().add(tabRowCol);

            var collectionLayoutData = layoutFactory.apply(collectionId);
            tabRowCol.getCollections().add(collectionLayoutData);
        }
    }

    private void addActionsTo(
            final BSCol bsCol,
            final Collection<String> actionIds,
            final Function<String, ActionLayoutData> layoutFactory,
            final BiConsumer<String, ActionLayoutData> onNewLayoutData) {

        for (String actionId : actionIds) {
            var actionLayoutData = layoutFactory.apply(actionId);
            addActionTo(bsCol, actionLayoutData);
            onNewLayoutData.accept(actionId, actionLayoutData);
        }
    }

    private void addActionsTo(
            final FieldSet fieldSet,
            final Collection<String> actionIds,
            final Function<String, ActionLayoutData> layoutFactory,
            final BiConsumer<String, ActionLayoutData> onNewLayoutData) {

        for (String actionId : actionIds) {
            var actionLayoutData = layoutFactory.apply(actionId);
            addActionTo(fieldSet, actionLayoutData);
            onNewLayoutData.accept(actionId, actionLayoutData);
        }
    }

    private void addActionTo(
            final ActionLayoutDataOwner owner,
            final ActionLayoutData actionLayoutData) {
        owner.getActions().add(actionLayoutData);
        actionLayoutData.owner(owner);
    }

    /**
     * Overwrites (replaces) any existing facets in the metamodel with info taken from the grid.
     *
     * @implNote This code uses {@link FacetUtil#updateFacet(Facet)}
     * because the layout might be reloaded from XML if reloading is supported.
     */
    private void overwriteFacets(
            final BSGrid fcGrid,
            final Class<?> domainClass) {

        var objectSpec = context.specLoaderProvider().get().specForTypeElseFail(domainClass);

        var oneToOneAssociationById = ObjectMember.mapById(objectSpec.streamProperties(MixedIn.INCLUDED));
        var oneToManyAssociationById = ObjectMember.mapById(objectSpec.streamCollections(MixedIn.INCLUDED));
        var objectActionById = ObjectMember.mapById(objectSpec.streamRuntimeActions(MixedIn.INCLUDED));

        // governs, whether annotations win over XML grid, based on whether XML grid is fallback or 'explicit'
        var precedence = fcGrid.fallback()
                ? Facet.Precedence.LOW // fallback case: XML layout is overruled by layout from annotations
                : Facet.Precedence.HIGH; // non-fallback case: XML layout overrules layout from annotations

        final AtomicInteger propertySequence = new AtomicInteger(0);
        fcGrid.visit(new BSElementVisitor() {
            private int collectionSequence = 1;

            private int actionDomainObjectSequence = 1;
            private int actionPropertyGroupSequence = 1;
            private int actionPropertySequence = 1;
            private int actionCollectionSequence = 1;

            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {

                updateFacetIfPresent(
                        BookmarkPolicyFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence));
                updateFacetIfPresent(
                        CssClassFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence));
                updateFacetIfPresent(
                        FaFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence));
                updateFacetIfPresent(
                        ObjectDescribedFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence));
                updateFacetIfPresent(
                        ObjectNamedFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence));
                updateFacetIfPresent(
                        TableDecoratorFacetForDomainObjectLayoutXml
                            .create(domainObjectLayoutData, objectSpec, precedence));
            }

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {

                var actionLayoutDataOwner = actionLayoutData.owner();
                var objectAction = objectActionById.get(actionLayoutData.getId());
                if(objectAction == null) return;

                {
                    GroupIdAndName groupIdAndName = null;
                    int memberOrderSequence;
                    if(actionLayoutDataOwner instanceof FieldSet) {
                        var fieldSet = (FieldSet) actionLayoutDataOwner;
                        for (var propertyLayoutData : fieldSet.getProperties()) {
                            // any will do; choose the first one that we know is valid
                            if(oneToOneAssociationById.containsKey(propertyLayoutData.getId())) {
                                groupIdAndName = GroupIdAndName
                                        .forPropertyLayoutData(propertyLayoutData)
                                        .orElse(null);
                                break;
                            }
                        }
                        memberOrderSequence = actionPropertyGroupSequence++;
                    } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                        groupIdAndName = GroupIdAndName
                                .forPropertyLayoutData((PropertyLayoutData) actionLayoutDataOwner)
                                .orElse(null);
                        memberOrderSequence = actionPropertySequence++;
                    } else if(actionLayoutDataOwner instanceof CollectionLayoutData) {
                        groupIdAndName = GroupIdAndName
                                .forCollectionLayoutData((CollectionLayoutData) actionLayoutDataOwner)
                                .orElse(null);
                        memberOrderSequence = actionCollectionSequence++;
                    } else {
                        // don't add: any existing metadata should be preserved
                        groupIdAndName = null;
                        memberOrderSequence = actionDomainObjectSequence++;
                    }
                    updateFacet(
                            LayoutOrderFacetForLayoutXml.create(memberOrderSequence, objectAction, precedence));

                    //XXX hotfix: always override LayoutGroupFacetFromActionLayoutAnnotation, otherwise actions are not shown - don't know why
                    var precedenceHotfix = fcGrid.fallback()
                            ? Facet.Precedence.DEFAULT
                            : Facet.Precedence.HIGH;

                    updateFacetIfPresent(
                            LayoutGroupFacetForLayoutXml.create(groupIdAndName, objectAction, precedenceHotfix));
                }

                // fix up the action position if required
                if(actionLayoutDataOwner instanceof FieldSet) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.BELOW ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.RIGHT) {
                        actionLayoutData.setPosition(org.apache.causeway.applib.annotation.ActionLayout.Position.PANEL);
                    }
                } else if(actionLayoutDataOwner instanceof PropertyLayoutData) {
                    if(actionLayoutData.getPosition() == null ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.PANEL_DROPDOWN ||
                            actionLayoutData.getPosition() == org.apache.causeway.applib.annotation.ActionLayout.Position.PANEL) {
                        actionLayoutData.setPosition(org.apache.causeway.applib.annotation.ActionLayout.Position.BELOW);
                    }
                } else {
                    // doesn't do anything for DomainObject or Collection
                    actionLayoutData.setPosition(null);
                }

                updateFacetIfPresent(
                        ActionPositionFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence));

                updateFacetIfPresent(
                        CssClassFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence));

                updateFacetIfPresent(
                        FaFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence));

                updateFacetIfPresent(
                        MemberDescribedFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence));

                updateFacetIfPresent(
                        HiddenFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence));

                updateFacetIfPresent(
                        MemberNamedFacetForActionLayoutXml.create(actionLayoutData, objectAction, precedence));

                updateFacetIfPresent(
                    Optional.ofNullable(actionLayoutData)
                        .map(ActionLayoutData::getPromptStyle)
                        .map(promptStyle->new PromptStyleFacet("ActionLayoutXml", promptStyle, objectAction, precedence, true)));

            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                var oneToOneAssociation = oneToOneAssociationById.get(propertyLayoutData.getId());
                if(oneToOneAssociation == null) return;

                updateFacetIfPresent(
                        CssClassFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        MemberDescribedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        HiddenFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        LabelAtFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        MultiLineFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        MemberNamedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        Optional.ofNullable(propertyLayoutData)
                            .map(PropertyLayoutData::getPromptStyle)
                            .map(promptStyle->new PromptStyleFacet("PropertyLayoutXml", promptStyle, oneToOneAssociation, precedence, true)));

                updateFacetIfPresent(
                        RenderedAdjustedFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        UnchangingFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        TypicalLengthFacetForPropertyLayoutXml.create(propertyLayoutData, oneToOneAssociation, precedence));

                // Layout group-name based on owning property group, Layout sequence monotonically increasing
                // nb for any given field set the sequence won't reset to zero; however this is what we want so that
                // table columns are shown correctly (by fieldset, then property order within that fieldset).
                final FieldSet fieldSet = propertyLayoutData.owner();

                updateFacet(LayoutOrderFacetForLayoutXml.create(propertySequence.incrementAndGet(), oneToOneAssociation, precedence));

                updateFacetIfPresent(
                        LayoutGroupFacetForLayoutXml.create(fieldSet, oneToOneAssociation, precedence));
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                var oneToManyAssociation = oneToManyAssociationById.get(collectionLayoutData.getId());
                if(oneToManyAssociation == null) {
                    return;
                }

                updateFacetIfPresent(
                        CssClassFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacetIfPresent(
                        DefaultViewFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacetIfPresent(
                        TableDecoratorFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacetIfPresent(
                        MemberDescribedFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacetIfPresent(
                        HiddenFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacetIfPresent(
                        MemberNamedFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacetIfPresent(
                        PagedFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacetIfPresent(
                        SortedByFacetForCollectionLayoutXml
                            .create(collectionLayoutData, oneToManyAssociation, precedence));

                updateFacet(LayoutOrderFacetForLayoutXml
                        .create(collectionSequence++, oneToManyAssociation, precedence));
            }
        });
    }

}
