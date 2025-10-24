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
package org.apache.causeway.core.metamodel.services.grid.bootstrap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.ActionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTabGroup;
import org.apache.causeway.applib.layout.grid.bootstrap.Size;
import org.apache.causeway.applib.services.grid.GridMarshallerService;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.functions._Functions;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.GroupIdAndName;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.layout.LayoutFacetUtil.LayoutDataFactory;
import org.apache.causeway.core.metamodel.services.grid.GridSystemServiceAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link org.apache.causeway.applib.services.grid.GridSystemService} using DTOs based on
 * <a href="https://getbootstrap.com>Bootstrap</a> design system.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".GridSystemServiceBootstrap")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Bootstrap")
@Slf4j
public class GridSystemServiceBootstrap
extends GridSystemServiceAbstract<BSGrid> {

    public static final String TNS = "https://causeway.apache.org/applib/layout/grid/bootstrap3";
    public static final String SCHEMA_LOCATION = "https://causeway.apache.org/applib/layout/grid/bootstrap3/bootstrap3.xsd";

    /**
     * SPI to customize layout fallback behavior on a per class basis.
     */
    public static interface FallbackLayoutDataSource {
        /**
         * Implementing beans may provide custom defaults (for specific types) if required.<br>
         * Implementing beans may chose to be indifferent by returning an empty {@link Try}.
         */
        Try<String> tryLoadAsStringUtf8(Class<?> domainClass);
    }

    @Inject @Lazy // circular dependency (late binding)
    @Setter @Accessors(chain = true) // JUnit support
    private GridMarshallerService<BSGrid> marshaller;

    private final CausewayConfiguration config;
    private final Can<FallbackLayoutDataSource> fallbackLayoutDataSources;

    @Inject
    public GridSystemServiceBootstrap(
            final CausewayConfiguration causewayConfiguration,
            final Provider<SpecificationLoader> specLoaderProvider,
            final TranslationService translationService,
            final JaxbService jaxbService,
            final MessageService messageService,
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final List<FallbackLayoutDataSource> fallbackLayoutDataSources) {
        super(specLoaderProvider, translationService, jaxbService, messageService, causewaySystemEnvironment);
        this.config = causewayConfiguration;
        this.fallbackLayoutDataSources = Can.ofCollection(fallbackLayoutDataSources);
    }

    @Override
    public Class<BSGrid> gridImplementation() {
        return BSGrid.class;
    }

    @Override
    public String tns() {
        return TNS;
    }

    @Override
    public String schemaLocation() {
        return SCHEMA_LOCATION;
    }

    @Override
    public BSGrid defaultGrid(final Class<?> domainClass) {
        final Try<String> content = loadFallbackLayoutAsStringUtf8(domainClass);
        try {
            return content.getValue()
                    .flatMap(xml -> marshaller.unmarshal(domainClass, xml, CommonMimeType.XML)
                        .getValue())
                    .filter(BSGrid.class::isInstance)
                    .map(BSGrid.class::cast)
                    .map(_Functions.peek(bsGrid -> bsGrid.setFallback(true)))
                    .orElseGet(() -> fallback(domainClass));
        } catch (final Exception e) {
            return fallback(domainClass);
        }
    }

    private Try<String> loadFallbackLayoutAsStringUtf8(final Class<?> domainClass) {
        return fallbackLayoutDataSources.stream()
            .map(ds->ds.tryLoadAsStringUtf8(domainClass))
            .filter(tried->tried.getValue().isPresent())
            .findFirst()
            .orElseGet(()->{
                return Try.call(()->_Resources.loadAsStringUtf8(GridSystemServiceBootstrap.class, "GridFallbackLayout.xml"));
            });
    }

    //
    // only ever called if fail to load GridFallbackLayout.xml,
    // which *really* shouldn't happen
    //
    private BSGrid fallback(final Class<?> domainClass) {
        final BSGrid bsGrid = new BSGrid(domainClass);
        bsGrid.setFallback(true);

        final BSRow headerRow = new BSRow();
        bsGrid.getRows().add(headerRow);
        final BSCol headerRowCol = new BSCol();
        headerRowCol.setSpan(12);
        headerRowCol.setUnreferencedActions(true);
        headerRowCol.setDomainObject(new DomainObjectLayoutData());
        headerRow.getCols().add(headerRowCol);

        final BSRow propsRow = new BSRow();
        bsGrid.getRows().add(propsRow);

        // if no layout hints
        addFieldSetsToColumn(propsRow, 4, Arrays.asList("General"), true);

        final BSCol col = new BSCol();
        col.setUnreferencedCollections(true);
        col.setSpan(12);
        propsRow.getCols().add(col);

        return bsGrid;
    }

    static void addFieldSetsToColumn(
            final BSRow propsRow,
            final int span,
            final List<String> memberGroupNames,
            final boolean unreferencedProperties) {

        if(span > 0 || unreferencedProperties) {
            final BSCol col = new BSCol();
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

    @Override
    protected boolean validateAndNormalize(
            final Grid grid,
            final Class<?> domainClass) {

        var bsGrid = (BSGrid) grid;

        var gridModelIfValid = GridInitializationModel.createFrom(bsGrid);
        if(!gridModelIfValid.isPresent()) return false; // only present if valid

        var gridModel = gridModelIfValid.get();
        var objSpec = specLoaderProvider.get().specForTypeElseFail(domainClass);

        var oneToOneAssociationById = ObjectMember.mapById(objSpec.streamProperties(MixedIn.INCLUDED));
        var oneToManyAssociationById = ObjectMember.mapById(objSpec.streamCollections(MixedIn.INCLUDED));
        var objectActionById = ObjectMember.mapById(objSpec.streamRuntimeActions(MixedIn.INCLUDED));

        var layoutDataFactory = LayoutDataFactory.of(objSpec);

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
                    .sortProperties(config, unboundPropertyIds.stream()
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
                    .sortCollections(config, collectionDisjunction.right().stream()
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
                            ? propertyLayoutData.getOwner()
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

        bsGrid.setNormalized(true);
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
            propertyLayoutData.setOwner(fieldSet);
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
                newTab.setOwner(tabGroup);
                return newTab;
            });

            final BSRow tabRow = new BSRow();
            bsTab.getRows().add(tabRow);

            final BSCol tabRowCol = new BSCol();
            tabRowCol.setSpan(12);
            tabRowCol.setSize(Size.MD);
            tabRow.getCols().add(tabRowCol);

            var collectionLayoutData = layoutFactory.apply(collectionId);
            tabRowCol.getCollections().add(collectionLayoutData);
        }
    }

    protected void addActionsTo(
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

        List<ActionLayoutData> actions = owner.getActions();
        if(actions == null) {
            owner.setActions(actions = _Lists.newArrayList());
        }
        actions.add(actionLayoutData);
        actionLayoutData.setOwner(owner);
    }

}
