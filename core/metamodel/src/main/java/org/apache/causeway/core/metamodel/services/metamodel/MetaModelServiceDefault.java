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
package org.apache.causeway.core.metamodel.services.metamodel;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.metamodel.Config;
import org.apache.causeway.applib.services.metamodel.DomainMember;
import org.apache.causeway.applib.services.metamodel.DomainModel;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.metamodel.objgraph.ObjectGraph;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacet;
import org.apache.causeway.core.metamodel.services.metamodel.MetaModelAnnotator.ExporterConfig;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.schema.metamodel.v2.MetamodelDto;

import org.jspecify.annotations.NonNull;

/**
 * Default implementation of {@link MetaModelService}.
 *
 * @since 1.x revised for 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".MetaModelServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class MetaModelServiceDefault implements MetaModelService {

    @Inject private SpecificationLoader specificationLoader;
    @Inject private GridService gridService;

    @Override
    public Optional<LogicalType> lookupLogicalTypeByName(final @Nullable String logicalTypeName) {
        return specificationLoader.specForLogicalTypeName(logicalTypeName)
                .map(ObjectSpecification::logicalType);
    }

    @Override
    public Can<LogicalType> logicalTypeAndAliasesFor(final LogicalType logicalType) {
        Set<LogicalType> logicalTypes = new TreeSet<>();
        specificationLoader.specForLogicalType(logicalType)
                .ifPresent(objectSpecification -> {
                    logicalTypes.add(logicalType);
                    objectSpecification.getAliases().stream().forEach(logicalTypes::add);
                });
        return Can.ofCollection(logicalTypes);
    }

    @Override
    public Can<LogicalType> logicalTypeAndAliasesFor(final @Nullable String logicalTypeName) {
        return lookupLogicalTypeByName(logicalTypeName)
                .map(this::logicalTypeAndAliasesFor)
                .orElse(Can.empty());
    }

    @Override
    public Optional<LogicalType> lookupLogicalTypeByClass(final @Nullable Class<?> domainType) {
        return specificationLoader.specForType(domainType)
                .map(ObjectSpecification::logicalType);
    }

    @Override
    public void rebuild(final Class<?> domainType) {

        gridService.remove(domainType);
        specificationLoader.reloadSpecification(domainType);
    }

    @Override
    public DomainModel getDomainModel() {

        var specifications = specificationLoader.snapshotSpecifications();

        final List<DomainMember> rows = _Lists.newArrayList();
        for (final ObjectSpecification spec : specifications) {
            if (exclude(spec)) {
                continue;
            }

            spec.streamProperties(MixedIn.INCLUDED)
            .filter(otoa->!exclude(otoa))
            .forEach(otoa->rows.add(new DomainMemberDefault(spec, otoa)));

            spec.streamCollections(MixedIn.INCLUDED)
            .filter(otma->!exclude(otma))
            .forEach(otma->rows.add(new DomainMemberDefault(spec, otma)));

            spec.streamAnyActions(MixedIn.INCLUDED)
            .filter(action->!exclude(action))
            .forEach(action->rows.add(new DomainMemberDefault(spec, action)));
        }

        Collections.sort(rows);

        return new DomainModelDefault(rows);
    }

    protected boolean exclude(final OneToOneAssociation property) {
        return false;
    }

    protected boolean exclude(final OneToManyAssociation collection) {
        return false;
    }

    protected boolean exclude(final ObjectAction action) {
        return false;
    }

    protected boolean exclude(final ObjectSpecification spec) {
        return isBuiltIn(spec) || spec.isAbstract() || spec.isMixin();
    }

    protected boolean isBuiltIn(final ObjectSpecification spec) {
        final String className = spec.getFullIdentifier();
        return className.startsWith("java");
    }

    @Override
    public BeanSort sortOf(
            final @Nullable Class<?> domainType, final Mode mode) {
        if(domainType == null) {
            return null;
        }
        final ObjectSpecification objectSpec = specificationLoader.specForType(domainType).orElse(null);
        if(objectSpec == null) {
            return BeanSort.UNKNOWN;
        }

        if(objectSpec.getBeanSort().isUnknown()
                && !(mode == Mode.RELAXED)) {

            throw new IllegalArgumentException(String.format(
                    "Unable to determine what sort of domain object this is: '%s'. Originating domainType: '%s'",
                    objectSpec.getFullIdentifier(),
                    domainType.getName()
                    ));
        }

        return objectSpec.getBeanSort();

    }

    @Override
    public BeanSort sortOf(final Bookmark bookmark, final Mode mode) {
        if(bookmark == null) {
            return null;
        }

        final Class<?> domainType;
        switch (mode) {
        case RELAXED:
            domainType = specificationLoader.specForBookmark(bookmark)
                .map(ObjectSpecification::getCorrespondingClass)
                .orElse(null);
            break;

        case STRICT:
            // fall through to...
        default:
            domainType = specificationLoader.specForBookmark(bookmark)
                .map(ObjectSpecification::getCorrespondingClass)
                .orElseThrow(()->_Exceptions
                        .noSuchElement("Cannot resolve logical type name %s to a java class",
                                bookmark.logicalTypeName()));
            break;
        }
        return sortOf(domainType, mode);
    }

    @Override
    public CommandDtoProcessor commandDtoProcessorFor(final String memberIdentifier) {
        final ApplicationFeatureId featureId = ApplicationFeatureId
                .newFeature(ApplicationFeatureSort.MEMBER, memberIdentifier);

        final String logicalTypeName = featureId.getLogicalTypeName();
        if(_Strings.isNullOrEmpty(logicalTypeName)) {
            return null;
        }

        final ObjectSpecification spec = specificationLoader.specForLogicalTypeName(logicalTypeName).orElse(null);
        if(spec == null) {
            return null;
        }
        final ObjectMember objectMemberIfAny = spec.getMember(featureId.getLogicalMemberName()).orElse(null);
        if (objectMemberIfAny == null) {
            return null;
        }
        final CommandPublishingFacet commandPublishingFacet = objectMemberIfAny.getFacet(CommandPublishingFacet.class);
        if(commandPublishingFacet == null) {
            return null;
        }
        return commandPublishingFacet.getProcessor();
    }

    @Override
    public MetamodelDto exportMetaModel(final Config config) {

        /*TODO[CAUSEWAY-3206] refactor: ideally config would provide the list, but unfortunately
         * MetaModelAnnotator type is not know to Config, which lives in applib.
         */
        var metaModelAnnotators = _Lists.<MetaModelAnnotator>newArrayList();
        if(config.isIncludeTitleAnnotations()) {
            metaModelAnnotators.add(new TitleAnnotator(new ExporterConfig(){}));
        }
        if(config.isIncludeShadowedFacets()) {
            metaModelAnnotators.add(new ShadowedFactetAttributeAnnotator(new ExporterConfig(){}));
        }
        return new MetaModelExporter(specificationLoader, metaModelAnnotators)
                .exportMetaModel(config);
    }

    @Override
    public ObjectGraph exportObjectGraph(final @NonNull BiPredicate<BeanSort, LogicalType> filter) {
        var objectSpecs = specificationLoader
                .snapshotSpecifications()
                .stream()
                .filter(spec->filter.test(spec.getBeanSort(), spec.logicalType()))
                .collect(Collectors.toList());
        return ObjectGraph
                .create(new _ObjectGraphFactory(objectSpecs));
    }

}
