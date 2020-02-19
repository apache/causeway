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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Repository;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.services.ApplicationFeaturesInitConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.SingleIntValueFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ContributeeMember;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Repository
@Named("isisMetaModel.ApplicationFeatureRepositoryDefault")
@Log4j2
public class ApplicationFeatureRepositoryDefault implements ApplicationFeatureRepository {

    private final IsisConfiguration configuration;
    private final ServiceRegistry serviceRegistry;
    private final SpecificationLoader specificationLoader;
    private final ApplicationFeatureFactory applicationFeatureFactory;
    
    // -- caches
    final SortedMap<ApplicationFeatureId, ApplicationFeature> packageFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> classFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> memberFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> propertyFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> collectionFeatures = _Maps.newTreeMap();
    private final SortedMap<ApplicationFeatureId, ApplicationFeature> actionFeatures = _Maps.newTreeMap();

    // -- init

    @Inject
    public ApplicationFeatureRepositoryDefault(IsisConfiguration configuration, ServiceRegistry serviceRegistry,
            SpecificationLoader specificationLoader, ApplicationFeatureFactory applicationFeatureFactory) {
        this.configuration = configuration;
        this.serviceRegistry = serviceRegistry;
        this.specificationLoader = specificationLoader;
        this.applicationFeatureFactory = applicationFeatureFactory;
    }

    @PostConstruct
    public void init() {
        if(isEagerInitialize()) {
            initializeIfRequired();
        }
    }

    private boolean isEagerInitialize() {
        ApplicationFeaturesInitConfiguration setting = configuration.getCore().getRuntimeServices().getApplicationFeatures().getInit();
        return setting == ApplicationFeaturesInitConfiguration.EAGER || setting == ApplicationFeaturesInitConfiguration.EAGERLY;
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
        final Collection<ObjectSpecification> specifications = primeMetaModel();
        for (val spec : specifications) {
            createApplicationFeaturesFor(spec);
        }
    }

    private Collection<ObjectSpecification> primeMetaModel() {
        serviceRegistry.streamRegisteredBeans()
        .forEach(bean->specificationLoader.loadSpecification(bean.getBeanClass()));
        return specificationLoader.snapshotSpecifications();
    }

    void createApplicationFeaturesFor(final ObjectSpecification spec) {
        
        if (exclude(spec)) {
            return;
        }

        final List<ObjectAssociation> properties = spec.streamAssociations(Contributed.INCLUDED)
                .filter(ObjectAssociation.Predicates.PROPERTIES)
                .collect(Collectors.toList());
        final List<ObjectAssociation> collections = spec.streamAssociations(Contributed.INCLUDED)
                .filter(ObjectAssociation.Predicates.COLLECTIONS)
                .collect(Collectors.toList());
        final List<ObjectAction> actions = spec.streamObjectActions(Contributed.INCLUDED)
                .collect(Collectors.toList());

        if (properties.isEmpty() && collections.isEmpty() && actions.isEmpty()) {
            return;
        }

        final String fullIdentifier = spec.getFullIdentifier();
        final ApplicationFeatureId classFeatureId = ApplicationFeatureId.newClass(fullIdentifier);

        // add class to our map
        // (later on it may get removed if the class turns out to have no features,
        // but we require it in the map for the next bit).
        final ApplicationFeature classFeature = newFeature(classFeatureId);
        classFeatures.put(classFeatureId, classFeature);

        // add members
        boolean addedMembers = false;
        for (final ObjectAssociation property : properties) {
            final Class<?> returnType = correspondingClassFor(property.getSpecification());
            final Integer maxLength = returnType == String.class ? valueOf(property, MaxLengthFacet.class) : null;
            final Integer typicalLength = returnType == String.class ? valueOf(property, TypicalLengthFacet.class) : null;
            final boolean derived = !property.containsNonFallbackFacet(PropertySetterFacet.class);
            final boolean contributed = property instanceof ContributeeMember;
            addedMembers = newProperty(classFeatureId, property, returnType, contributed, maxLength, typicalLength, derived) || addedMembers;
        }
        for (final ObjectAssociation collection : collections) {
            final boolean derived = !(collection.containsNonFallbackFacet(CollectionAddToFacet.class) || collection.containsNonFallbackFacet(CollectionRemoveFromFacet.class));
            final Class<?> elementType = correspondingClassFor(collection.getSpecification());
            final boolean contributed = collection instanceof ContributeeMember;
            addedMembers = newCollection(classFeatureId, collection, elementType, contributed, derived) || addedMembers;
        }
        for (final ObjectAction action : actions) {
            final Class<?> returnType = correspondingClassFor(action.getReturnType());
            final SemanticsOf actionSemantics = action.getSemantics();
            final boolean contributed = action instanceof ContributeeMember;
            addedMembers = newAction(classFeatureId, action, returnType, contributed, actionSemantics) || addedMembers;
        }

        if (!addedMembers) {
            // remove this class feature, since it turned out to have no members
            classFeatures.remove(classFeatureId);
            return;
        }

        // leave the class as is and (as there were indeed members for this class)
        // and add all of its parent packages
        final ApplicationFeatureId classParentPackageId = addClassParent(classFeatureId);
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
        final ApplicationFeatureId parentPackageId = classFeatureId.getParentPackageId();

        final ApplicationFeature parentPackage = findPackageElseCreate(parentPackageId);

        parentPackage.addToContents(classFeatureId);
        return parentPackageId;
    }

    void addParents(final ApplicationFeatureId classOrPackageId) {
        final ApplicationFeatureId parentPackageId = classOrPackageId.getParentPackageId();
        if (parentPackageId == null) {
            return;
        }

        final ApplicationFeature parentPackage = findPackageElseCreate(parentPackageId);

        // add this feature as part of the contents of its parent
        parentPackage.addToContents(classOrPackageId);

        // and recurse up
        addParents(parentPackageId);
    }

    private ApplicationFeature findPackageElseCreate(final ApplicationFeatureId parentPackageId) {
        ApplicationFeature parentPackage = findPackage(parentPackageId);
        if (parentPackage == null) {
            parentPackage = newPackage(parentPackageId);
        }
        return parentPackage;
    }

    private ApplicationFeature newPackage(final ApplicationFeatureId packageId) {
        final ApplicationFeature parentPackage = newFeature(packageId);
        packageFeatures.put(packageId, parentPackage);
        return parentPackage;
    }

    private boolean newProperty(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final Class<?> returnType,
            final boolean contributed,
            final Integer maxLength, final Integer typicalLength,
            final boolean derived) {
        return newMember(classFeatureId, objectMember, ApplicationMemberType.PROPERTY, returnType, contributed, derived, maxLength, typicalLength, null);
    }

    private boolean newCollection(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final Class<?> returnType,
            final boolean contributed,
            final boolean derived) {
        return newMember(classFeatureId, objectMember, ApplicationMemberType.COLLECTION, returnType, contributed, derived, null, null, null);
    }

    private boolean newAction(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final Class<?> returnType,
            final boolean contributed,
            final SemanticsOf actionSemantics) {
        return newMember(classFeatureId, objectMember, ApplicationMemberType.ACTION, returnType, contributed, null, null, null, actionSemantics);
    }

    private boolean newMember(
            final ApplicationFeatureId classFeatureId,
            final ObjectMember objectMember,
            final ApplicationMemberType memberType,
            final Class<?> returnType,
            final boolean contributed,
            final Boolean derived,
            final Integer maxLength, final Integer typicalLength,
            final SemanticsOf actionSemantics) {
        if (objectMember.isAlwaysHidden()) {
            return false;
        }
        newMember(classFeatureId, objectMember.getId(), memberType, returnType, contributed, derived, maxLength, typicalLength, actionSemantics);
        return true;
    }

    private void newMember(
            final ApplicationFeatureId classFeatureId,
            final String memberId,
            final ApplicationMemberType memberType,
            final Class<?> returnType,
            final boolean contributed,
            final Boolean derived,
            final Integer maxLength, final Integer typicalLength,
            final SemanticsOf actionSemantics) {
        final ApplicationFeatureId featureId = ApplicationFeatureId.newMember(classFeatureId.getFullyQualifiedName(), memberId);

        final ApplicationFeature memberFeature = newFeature(featureId);
        memberFeature.setMemberType(memberType);

        memberFeature.setReturnTypeName(returnType != null ? returnType.getSimpleName() : null);
        memberFeature.setContributed(contributed);
        memberFeature.setDerived(derived);
        memberFeature.setPropertyMaxLength(maxLength);
        memberFeature.setPropertyTypicalLength(typicalLength);
        memberFeature.setActionSemantics(actionSemantics);

        memberFeatures.put(featureId, memberFeature);

        // also cache per memberType
        featuresMapFor(memberType).put(featureId, memberFeature);

        final ApplicationFeature classFeature = findClass(classFeatureId);
        classFeature.addToMembers(featureId, memberType);
    }

    private SortedMap<ApplicationFeatureId, ApplicationFeature> featuresMapFor(final ApplicationMemberType memberType) {
        switch (memberType) {
        case PROPERTY:
            return propertyFeatures;
        case COLLECTION:
            return collectionFeatures;
        default: // case ACTION:
            return actionFeatures;
        }
    }

    private ApplicationFeature newFeature(final ApplicationFeatureId featureId) {
        final ApplicationFeature feature = applicationFeatureFactory.newApplicationFeature();
        feature.setFeatureId(featureId);
        return feature;
    }


    protected boolean exclude(final ObjectSpecification spec) {
        
        val excluded = spec.isAbstract() ||
                spec.getBeanSort().isUnknown() ||
                isBuiltIn(spec) ||
                isHidden(spec);
        
        if(excluded && log.isDebugEnabled()) {
            log.debug("{} excluded because: abstract:{} unknown-sort:{} builtIn:{} hidden:{}", 
                    spec.getCorrespondingClass().getSimpleName(),
                    spec.isAbstract(),
                    spec.getBeanSort().isUnknown(),
                    isBuiltIn(spec),
                    isHidden(spec)
                    );
        }
        
        return excluded;
    }

//XXX[2286] .. replaced by check 'spec.getBeanSort().isUnknown()'
//    /**
//     * Ignore the (strict) super-classes of any services.
//     * <p>
//     * For example, we want to ignore <code>ExceptionRecognizerComposite</code> 
//     * because there is no service of that type (only of subtypes of that).
//     * </p>
//     */
//    private boolean isSuperClassOfService(final ObjectSpecification spec) {
//
//        val specClass = spec.getCorrespondingClass();
//        
//        // is this class a supertype or the actual type of one of the services?
//        boolean serviceCls = false;
//        for (final ManagedBeanAdapter bean : registeredServices.get()) {
//            final Class<?> serviceClass = bean.getBeanClass();
//            if (specClass.isAssignableFrom(serviceClass)) {
//                serviceCls = true;
//            }
//        }
//        if (!serviceCls) {
//            return false;
//        }
//
//        // yes it is.  In which case, is it the actual concrete class of one of those services?
//        for (final Object registeredService : registeredServices.get()) {
//            final Class<?> serviceClass = registeredService.getClass();
//            if (serviceClass.isAssignableFrom(specClass)) {
//                return false;
//            }
//        }
//        // couldn't find a service of exactly this type, so ignore the spec.
//        return true;
//    }

    protected boolean isHidden(final ObjectSpecification spec) {
        final HiddenFacet facet = spec.getFacet(HiddenFacet.class);
        return facet != null &&
                !facet.isFallback() &&
                (facet.where() == Where.EVERYWHERE || facet.where() == Where.ANYWHERE);
    }

    protected boolean isBuiltIn(final ObjectSpecification spec) {
        final String className = spec.getFullIdentifier();
        return className.startsWith("java") || className.startsWith("org.joda");
    }


    // -- packageFeatures, classFeatures, memberFeatures

    public ApplicationFeature findFeature(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        switch (featureId.getType()) {
        case PACKAGE:
            return findPackage(featureId);
        case CLASS:
            return findClass(featureId);
        case MEMBER:
            return findMember(featureId);
        }
        throw new IllegalArgumentException("Feature has unknown feature type " + featureId.getType());
    }


    public ApplicationFeature findPackage(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        return packageFeatures.get(featureId);
    }


    public ApplicationFeature findClass(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        return classFeatures.get(featureId);
    }


    public ApplicationFeature findMember(final ApplicationFeatureId featureId) {
        initializeIfRequired();
        return memberFeatures.get(featureId);
    }



    // -- allFeatures, allPackages, allClasses, allMembers

    public Collection<ApplicationFeature> allFeatures(final ApplicationFeatureType featureType) {
        initializeIfRequired();
        if (featureType == null) {
            return Collections.emptyList();
        }
        switch (featureType) {
        case PACKAGE:
            return allPackages();
        case CLASS:
            return allClasses();
        case MEMBER:
            return allMembers();
        }
        throw new IllegalArgumentException("Unknown feature type " + featureType);
    }


    public Collection<ApplicationFeature> allPackages() {
        initializeIfRequired();
        return packageFeatures.values();
    }


    public Collection<ApplicationFeature> allClasses() {
        initializeIfRequired();
        return classFeatures.values();
    }


    public Collection<ApplicationFeature> allMembers() {
        initializeIfRequired();
        return memberFeatures.values();
    }


    public Collection<ApplicationFeature> allProperties() {
        initializeIfRequired();
        return propertyFeatures.values();
    }


    public Collection<ApplicationFeature> allCollections() {
        initializeIfRequired();
        return collectionFeatures.values();
    }


    public Collection<ApplicationFeature> allActions() {
        initializeIfRequired();
        return actionFeatures.values();
    }


    // -- packageNames, packageNamesContainingClasses, classNamesContainedIn, memberNamesOf
    @Override 
    public SortedSet<String> packageNames() {
        initializeIfRequired();
        return stream(allFeatures(ApplicationFeatureType.PACKAGE))
                .map(ApplicationFeature.Functions.GET_FQN)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override 
    public SortedSet<String> packageNamesContainingClasses(final ApplicationMemberType memberType) {
        initializeIfRequired();
        final Collection<ApplicationFeature> packages = allFeatures(ApplicationFeatureType.PACKAGE);

        return stream(packages)
                .filter(ApplicationFeature.Predicates.packageContainingClasses(memberType, this))
                .map(ApplicationFeature.Functions.GET_FQN)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override 
    public SortedSet<String> classNamesContainedIn(final String packageFqn, final ApplicationMemberType memberType) {
        initializeIfRequired();
        final ApplicationFeatureId packageId = ApplicationFeatureId.newPackage(packageFqn);
        final ApplicationFeature pkg = findPackage(packageId);
        if (pkg == null) {
            return Collections.emptySortedSet();
        }
        final SortedSet<ApplicationFeatureId> contents = pkg.getContents();
        return contents.stream()
                .filter(ApplicationFeatureId.Predicates.isClassContaining(memberType, this))
                .map(ApplicationFeatureId.Functions.GET_CLASS_NAME)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override 
    public SortedSet<String> classNamesRecursivelyContainedIn(final String packageFqn) {
        initializeIfRequired();
        final ApplicationFeatureId packageId = ApplicationFeatureId.newPackage(packageFqn);
        final ApplicationFeature pkg = findPackage(packageId);
        if (pkg == null) {
            return Collections.emptySortedSet();
        }
        final Set<ApplicationFeatureId> classIds = this.classFeatures.keySet();
        return classIds.stream()
                .filter(ApplicationFeatureId.Predicates.isClassRecursivelyWithin(packageId))
                .map(ApplicationFeatureId.Functions.GET_CLASS_NAME)
                .collect(_Sets.toUnmodifiableSorted());
    }

    @Override 
    public SortedSet<String> memberNamesOf(
            final String packageFqn,
            final String className,
            final ApplicationMemberType memberType) {
        initializeIfRequired();
        final ApplicationFeatureId classId = ApplicationFeatureId.newClass(packageFqn + "." + className);
        final ApplicationFeature cls = findClass(classId);
        if (cls == null) {
            return Collections.emptySortedSet();
        }
        final SortedSet<ApplicationFeatureId> featureIds = cls.membersOf(memberType);
        return featureIds.stream()
                .map(ApplicationFeatureId.Functions.GET_MEMBER_NAME)
                .collect(_Sets.toUnmodifiableSorted());
    }






}