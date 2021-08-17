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
package org.apache.isis.core.metamodel.services.appfeat;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import lombok.val;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.*;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.services.ApplicationFeaturesInitConfiguration;
import org.apache.isis.core.metamodel.events.MetamodelEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleIntValueFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.springframework.lang.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Service
@Named("isis.metamodel.ApplicationFeatureRepositoryDefault")
@Log4j2
public class ApplicationFeatureRepositoryDefault
implements ApplicationFeatureRepository {

    // -- caches
    private Map<String, ApplicationFeatureId> featureIdentifiersByName;
    final SortedMap<ApplicationFeatureId, ApplicationFeature> namespaceFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> typeFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> memberFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> propertyFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> collectionFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> actionFeatures = _Maps.newTreeMap();

    private final IsisConfiguration configuration;
    private final SpecificationLoader specificationLoader;

    @Inject
    public ApplicationFeatureRepositoryDefault(
            IsisConfiguration configuration,
            SpecificationLoader specificationLoader) {
        this.configuration = configuration;
        this.specificationLoader = specificationLoader;
    }

    // -- init
    @Order(PriorityPrecedence.MIDPOINT)
    @EventListener(MetamodelEvent.class)
    public void onMetamodelEvent(MetamodelEvent event) {
        if (event.isPostMetamodel()
                && isEagerInitialize()) {
            initializeIfRequired();
        }
    }

    private boolean isEagerInitialize() {
        ApplicationFeaturesInitConfiguration setting =
                configuration.getCore().getRuntimeServices().getApplicationFeatures().getInit();
        return setting == ApplicationFeaturesInitConfiguration.EAGER
                || setting == ApplicationFeaturesInitConfiguration.EAGERLY;
    }

    // -- initializeIfRequired

    enum InitializationState {
        NOT_INITIALIZED,
        INITIALIZED
    }
    private InitializationState initializationState = InitializationState.NOT_INITIALIZED;

    private synchronized void initializeIfRequired() {
        if(initializationState == InitializationState.INITIALIZED) {
            return;
        }
        initializationState = InitializationState.INITIALIZED;

        for (val spec : specificationLoader.snapshotSpecifications()) {
            createApplicationFeaturesFor(spec);
        }

        val featuresByName = new HashMap<String, ApplicationFeatureId>();
        visitFeatureIdentifierByName(namespaceFeatures, featuresByName::put);
        visitFeatureIdentifierByName(typeFeatures, featuresByName::put);
        visitFeatureIdentifierByName(memberFeatures, featuresByName::put);
        this.featureIdentifiersByName = Collections.unmodifiableMap(featuresByName);
    }

    private void visitFeatureIdentifierByName(
            final Map<ApplicationFeatureId, ApplicationFeature> map,
            final BiConsumer<String, ApplicationFeatureId> onEntry) {
        map.forEach((k, v)->onEntry.accept(k.getFullyQualifiedName(), k));
    }

    void createApplicationFeaturesFor(final ObjectSpecification spec) {

        if (exclude(spec)) {
            return;
        }

        final List<ObjectAssociation> properties = spec.streamProperties(MixedIn.INCLUDED)
                .collect(Collectors.toList());
        final List<ObjectAssociation> collections = spec.streamCollections(MixedIn.INCLUDED)
                .collect(Collectors.toList());
        final List<ObjectAction> actions = spec.streamAnyActions(MixedIn.INCLUDED)
                .collect(Collectors.toList());

        if (properties.isEmpty() && collections.isEmpty() && actions.isEmpty()) {
            return;
        }


        final String logicalTypeName = spec.getLogicalTypeName();
        final ApplicationFeatureId typeFeatureId = ApplicationFeatureId.newType(logicalTypeName);

        // add class to our map
        // (later on it may get removed if the class turns out to have no features,
        // but we require it in the map for the next bit).
        final ApplicationFeature typeFeature = newApplicationFeature(typeFeatureId);
        typeFeatures.put(typeFeatureId, typeFeature);

        // add members
        boolean addedMembers = false;
        for (final ObjectAssociation property : properties) {
            final Class<?> returnType = correspondingClassFor(property.getSpecification());
            final Integer maxLength = returnType == String.class ? valueOf(property, MaxLengthFacet.class) : null;
            final Integer typicalLength = returnType == String.class ? valueOf(property, TypicalLengthFacet.class) : null;
            final boolean derived = !property.containsNonFallbackFacet(PropertySetterFacet.class);
            addedMembers = newProperty(typeFeatureId, property, returnType, maxLength, typicalLength, derived) || addedMembers;
        }
        for (final ObjectAssociation collection : collections) {
            final boolean derived = false;
            final Class<?> elementType = correspondingClassFor(collection.getSpecification());
            addedMembers = newCollection(typeFeatureId, collection, elementType, derived) || addedMembers;
        }
        for (final ObjectAction action : actions) {
            final Class<?> returnType = correspondingClassFor(action.getReturnType());
            final SemanticsOf actionSemantics = action.getSemantics();
            addedMembers = newAction(typeFeatureId, action, returnType, actionSemantics) || addedMembers;
        }

        if (!addedMembers) {
            // remove this class feature, since it turned out to have no members
            typeFeatures.remove(typeFeatureId);
            return;
        }

        // leave the class as is and (as there were indeed members for this class)
        // add all of its parent packages
        final ApplicationFeatureId classParentPackageId = addClassParent(typeFeatureId);
        addParents(classParentPackageId);
    }

    private static Class<?> correspondingClassFor(final ObjectSpecification objectSpec) {
        return objectSpec != null ? objectSpec.getCorrespondingClass() : null;
    }

    private static Integer valueOf(
            final FacetHolder facetHolder,
            final Class<? extends SingleIntValueFacet> cls) {
        final SingleIntValueFacet facet = facetHolder.getFacet(cls);
        return facet != null ? facet.value() : null;
    }


    ApplicationFeatureId addClassParent(final ApplicationFeatureId classFeatureId) {
        final ApplicationFeatureId parentPackageId = classFeatureId.getParentNamespaceFeatureId();
        final ApplicationFeatureDefault parentPackage = (ApplicationFeatureDefault)findPackageElseCreate(parentPackageId);

        parentPackage.addToContents(classFeatureId);
        return parentPackageId;
    }

    void addParents(final ApplicationFeatureId classOrPackageId) {
        final ApplicationFeatureId parentPackageId = classOrPackageId.getParentNamespaceFeatureId();
        if (parentPackageId == null) {
            return;
        }

        final ApplicationFeatureDefault parentPackage = (ApplicationFeatureDefault)findPackageElseCreate(parentPackageId);

        // add this feature as part of the contents of its parent
        parentPackage.addToContents(classOrPackageId);

        // and recurse up
        addParents(parentPackageId);
    }

    private ApplicationFeature findPackageElseCreate(final ApplicationFeatureId parentPackageId) {
        ApplicationFeature parentPackage = findNamespace(parentPackageId);
        if (parentPackage == null) {
            parentPackage = newPackage(parentPackageId);
        }
        return parentPackage;
    }

    private ApplicationFeature newPackage(final ApplicationFeatureId packageId) {
        final ApplicationFeature parentPackage = newApplicationFeature(packageId);
        namespaceFeatures.put(packageId, parentPackage);
        return parentPackage;
    }

    private boolean newProperty(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final Class<?> returnType,
            final Integer maxLength,
            final Integer typicalLength,
            final boolean derived) {
        return newMember(classFeatureId, objectMember, ApplicationMemberSort.PROPERTY, returnType, derived, maxLength, typicalLength, null);
    }

    private boolean newCollection(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final Class<?> returnType,
            final boolean derived) {
        return newMember(classFeatureId, objectMember, ApplicationMemberSort.COLLECTION, returnType, derived, null, null, null);
    }

    private boolean newAction(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final Class<?> returnType,
            final SemanticsOf actionSemantics) {
        return newMember(classFeatureId, objectMember, ApplicationMemberSort.ACTION, returnType, null, null, null, actionSemantics);
    }

    private boolean newMember(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final ApplicationMemberSort memberSort,
            final Class<?> returnType,
            final Boolean derived,
            final Integer maxLength,
            final Integer typicalLength,
            final SemanticsOf actionSemantics) {
        if (objectMember.isAlwaysHidden()) {
            return false;
        }
        newMember(classFeatureId, objectMember.getId(), memberSort, returnType, derived, maxLength, typicalLength, actionSemantics);
        return true;
    }

    private void newMember(
            final ApplicationFeatureId typeFeatureId,
            final String memberId,
            final @NonNull ApplicationMemberSort memberSort,
            final @Nullable Class<?> returnType,
            final Boolean derived,
            final @Nullable Integer maxLength, final @Nullable Integer typicalLength,
            final @Nullable SemanticsOf actionSemantics) {
        final ApplicationFeatureId featureId = ApplicationFeatureId.newMember(typeFeatureId.getFullyQualifiedName(), memberId);

        final ApplicationFeatureDefault memberFeature =
                (ApplicationFeatureDefault)newApplicationFeature(featureId);
        memberFeature.setMemberSort(Optional.of(memberSort));

        memberFeature.setActionReturnType(Optional.ofNullable(returnType));
        memberFeature.setActionSemantics(Optional.ofNullable(actionSemantics));
        memberFeature.setPropertyOrCollectionDerived(Boolean.TRUE.equals(derived));
        memberFeature.setPropertyMaxLength(maxLength!=null ? OptionalInt.of(maxLength) : OptionalInt.empty());
        memberFeature.setPropertyTypicalLength(typicalLength!=null ? OptionalInt.of(typicalLength) : OptionalInt.empty());

        memberFeatures.put(featureId, memberFeature);

        // also cache per memberSort
        featuresMapFor(memberSort).put(featureId, memberFeature);

        final ApplicationFeatureDefault typeFeature = (ApplicationFeatureDefault)findLogicalType(typeFeatureId);
        typeFeature.addToMembers(featureId, memberSort);
    }

    private SortedMap<ApplicationFeatureId, ApplicationFeature> featuresMapFor(final ApplicationMemberSort memberSort) {
        switch (memberSort) {
        case PROPERTY:
            return propertyFeatures;
        case COLLECTION:
            return collectionFeatures;
        default: // case ACTION:
            return actionFeatures;
        }
    }

    protected boolean exclude(final ObjectSpecification spec) {

        val excluded = spec.isMixin()
                || spec.isAbstract()
                || spec.getBeanSort().isVetoed()
                || spec.getBeanSort().isUnknown()
                || isBuiltIn(spec)
                || isHidden(spec);

        if(excluded && log.isDebugEnabled()) {
            log.debug("{} excluded because: abstract:{} vetoed:{} unknown-sort:{} builtIn:{} hidden:{}",
                    spec.getCorrespondingClass().getSimpleName(),
                    spec.isAbstract(),
                    spec.getBeanSort().isVetoed(),
                    spec.getBeanSort().isUnknown(),
                    isBuiltIn(spec),
                    isHidden(spec)
                    );
        }

        return excluded;
    }

    protected boolean isHidden(final ObjectSpecification spec) {
        return HiddenFacet.isAlwaysHidden(spec);
    }

    protected boolean isBuiltIn(final ObjectSpecification spec) {
        final String className = spec.getFullIdentifier();
        return className.startsWith("java") || className.startsWith("org.joda");
    }

    // -- FACTORY

    @Override
    public ApplicationFeature newApplicationFeature(ApplicationFeatureId featId) {
        return new ApplicationFeatureDefault(featId); // value type
    }

    // -- packageFeatures, classFeatures, memberFeatures

    @Override
    public ApplicationFeature findFeature(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        switch (featureId.getSort()) {
        case NAMESPACE:
            return findNamespace(featureId);
        case TYPE:
            return findLogicalType(featureId);
        case MEMBER:
            return findMember(featureId);
        }
        throw _Exceptions.illegalArgument("Feature of unknown sort '%s'", featureId.getSort());
    }


    public ApplicationFeature findNamespace(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        return namespaceFeatures.get(featureId);
    }


    public ApplicationFeature findLogicalType(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        return typeFeatures.get(featureId);
    }


    public ApplicationFeature findMember(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        return memberFeatures.get(featureId);
    }



    // -- allFeatures, allPackages, allClasses, allMembers

    public Collection<ApplicationFeature> allFeatures(final ApplicationFeatureSort featureType) {
        initializeIfRequired();
        if (featureType == null) {
            return Collections.emptyList();
        }
        switch (featureType) {
        case NAMESPACE:
            return allNamespaces();
        case TYPE:
            return allTypes();
        case MEMBER:
            return allMembers();
        }
        throw new IllegalArgumentException("Unknown feature type " + featureType);
    }

    @Override
    public Collection<ApplicationFeature> allNamespaces() {
        initializeIfRequired();
        return namespaceFeatures.values();
    }

    @Override
    public Collection<ApplicationFeature> allTypes() {
        initializeIfRequired();
        return typeFeatures.values();
    }

    @Override
    public Collection<ApplicationFeature> allMembers() {
        initializeIfRequired();
        return memberFeatures.values();
    }

    @Override
    public Collection<ApplicationFeature> allProperties() {
        initializeIfRequired();
        return propertyFeatures.values();
    }

    @Override
    public Collection<ApplicationFeature> allCollections() {
        initializeIfRequired();
        return collectionFeatures.values();
    }

    @Override
    public Collection<ApplicationFeature> allActions() {
        initializeIfRequired();
        return actionFeatures.values();
    }

    @Override
    public Map<String, ApplicationFeatureId> getFeatureIdentifiersByName() {
        initializeIfRequired();
        return featureIdentifiersByName;
    }


}
