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

package org.apache.isis.core.metamodel.specloader.specimpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.internal.base._Lazy;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.collections._Multimaps;
import org.apache.isis.core.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.commons.internal.collections._Sets;
import org.apache.isis.core.commons.internal.collections._Streams;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.internal.ioc.BeanSort;
import org.apache.isis.core.commons.internal.ioc.ManagedBeanAdapter;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistry;
import org.apache.isis.core.config.beans.IsisBeanTypeRegistryHolder;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.commons.ToString;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.CollectionFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.navparent.NavigableParentFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.postprocessor.PostProcessor;
import org.apache.isis.core.security.authentication.AuthenticationSession;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2 @EqualsAndHashCode(of = "correspondingClass", callSuper = false)
public abstract class ObjectSpecificationAbstract extends FacetHolderImpl implements ObjectSpecification {

    private static class Subclasses {
        private final Set<ObjectSpecification> classes = _Sets.newConcurrentHashSet();

        public void addSubclass(final ObjectSpecification subclass) {
            classes.add(subclass);
        }

        public boolean hasSubclasses() {
            return !classes.isEmpty();
        }

        public Collection<ObjectSpecification> toCollection() {
            return Collections.unmodifiableSet(classes); //just a view, thread-safe only if classes is thread-safe
        }
    }

//XXX drop-in for the above, 
//    private static class Subclasses {
//        private final List<ObjectSpecification> classes = new ArrayList<>();
//
//        public void addSubclass(final ObjectSpecification subclass) {
//            if(classes.contains(subclass)) {
//                return;
//            }
//            classes.add(subclass);
//        }
//
//        public boolean hasSubclasses() {
//            return !classes.isEmpty();
//        }
//
//        public List<ObjectSpecification> toCollection() {
//            return Collections.unmodifiableList(classes);
//        }
//    }
    
    
    // -- fields

    //protected final ServiceInjector servicesInjector;

    private final PostProcessor postProcessor;
    private final FacetProcessor facetProcessor;

    // -- ASSOCIATIONS
    
    private final List<ObjectAssociation> associations = _Lists.newArrayList();
    
    // defensive immutable lazy copy of associations
    private final _Lazy<List<ObjectAssociation>> unmodifiableAssociations = 
            _Lazy.threadSafe(()->Collections.unmodifiableList(new ArrayList<>(associations)));
    
    // -- ACTIONS
    
    private final List<ObjectAction> objectActions = _Lists.newArrayList();
    
    /** not API, used for validation, when {@code @Action} is NOT mandatory */
    @Getter private final Set<Method> potentialOrphans = _Sets.newHashSet();

    // defensive immutable lazy copy of objectActions
    private final _Lazy<List<ObjectAction>> unmodifiableActions = 
            _Lazy.threadSafe(()->Collections.unmodifiableList(new ArrayList<>(objectActions)));

    // partitions and caches objectActions by type; updated in sortCacheAndUpdateActions()
    private final ListMultimap<ActionType, ObjectAction> objectActionsByType = 
            _Multimaps.newConcurrentListMultimap();
    
    // -- INTERFACES

    private final List<ObjectSpecification> interfaces = _Lists.newArrayList();
    
    // defensive immutable lazy copy of interfaces
    private final _Lazy<List<ObjectSpecification>> unmodifiableInterfaces = 
            _Lazy.threadSafe(()->Collections.unmodifiableList(new ArrayList<>(interfaces)));
    
    
    
    private final Subclasses directSubclasses = new Subclasses();
    // built lazily
    private Subclasses transitiveSubclasses;

    private final Class<?> correspondingClass;
    private final String fullName;
    private final String shortName;
    private final Identifier identifier;
    private final boolean isAbstract;

    // derived lazily, cached since immutable
    protected ObjectSpecId specId;

    private ObjectSpecification superclassSpec;

    private TitleFacet titleFacet;
    private IconFacet iconFacet;
    private NavigableParentFacet navigableParentFacet;
    private CssClassFacet cssClassFacet;

    private IntrospectionState introspectionState = IntrospectionState.NOT_INTROSPECTED;


    // -- Constructor
    public ObjectSpecificationAbstract(
            final Class<?> introspectedClass,
            final String shortName,
            final FacetProcessor facetProcessor,
            final PostProcessor postProcessor) {

        this.correspondingClass = introspectedClass;
        this.fullName = introspectedClass.getName();
        this.shortName = shortName;

        this.isAbstract = ClassExtensions.isAbstract(introspectedClass);

        this.identifier = Identifier.classIdentifier(introspectedClass);

        this.facetProcessor = facetProcessor;
        this.postProcessor = postProcessor;
    }

    // -- Stuff immediately derivable from class
    @Override
    public FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    @Override
    public ObjectSpecId getSpecId() {
        if(specId == null) {
            val objectSpecIdFacet = getFacet(ObjectSpecIdFacet.class);
            if(objectSpecIdFacet == null) {
                throw new IllegalStateException("could not find an ObjectSpecIdFacet for " + this.getFullIdentifier());
            }
            specId = objectSpecIdFacet.value();
        }
        return specId;
    }

    /**
     * As provided explicitly within the constructor.
     *
     * <p>
     * Not API, but <tt>public</tt> so that {@link FacetedMethodsBuilder} can
     * call it.
     */
    @Override
    public Class<?> getCorrespondingClass() {
        return correspondingClass;
    }

    @Override
    public String getShortIdentifier() {
        return shortName;
    }

    /**
     * The {@link Class#getName() (full) name} of the
     * {@link #getCorrespondingClass() class}.
     */
    @Override
    public String getFullIdentifier() {
        return fullName;
    }

    @Override
    public void introspectUpTo(final IntrospectionState upTo) {
        
        if(!isLessThan(upTo)) {
            return; // optimization
        }

        if(log.isDebugEnabled()) {
            log.debug("introspectingUpTo: {}, {}", getFullIdentifier(), upTo);
        }

        boolean revalidate = false;

        switch (introspectionState) {
        case NOT_INTROSPECTED:
            if(isLessThan(upTo)) {
                // set to avoid infinite loops
                this.introspectionState = IntrospectionState.TYPE_BEING_INTROSPECTED;
                introspectTypeHierarchy();
                updateFromFacetValues();
                this.introspectionState = IntrospectionState.TYPE_INTROSPECTED;
            }
            if(isLessThan(upTo)) {
                this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
                introspectMembers();
                this.introspectionState = IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED;
                revalidate = true;
            }
            // set to avoid infinite loops
            break;
        case TYPE_BEING_INTROSPECTED:
            // nothing to do
            break;
        case TYPE_INTROSPECTED:
            if(isLessThan(upTo)) {
                // set to avoid infinite loops
                this.introspectionState = IntrospectionState.MEMBERS_BEING_INTROSPECTED;
                introspectMembers();
                this.introspectionState = IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED;
                revalidate = true;
            }
            break;
        case MEMBERS_BEING_INTROSPECTED:
            // nothing to do
        case TYPE_AND_MEMBERS_INTROSPECTED:
            // nothing to do
            break;

        default:
            throw _Exceptions.unexpectedCodeReach();
        }

        if(revalidate) {
            getSpecificationLoader().revalidateIfNecessary();
        }
    }

    boolean isLessThan(IntrospectionState upTo) {
        return this.introspectionState.compareTo(upTo) < 0;
    }
    
    protected abstract void introspectTypeHierarchy();
    protected abstract void introspectMembers();

    protected void loadSpecOfSuperclass(final Class<?> superclass) {
        if (superclass == null) {
            return;
        }
        superclassSpec = getSpecificationLoader().loadSpecification(superclass);
        if (superclassSpec != null) {
            if (log.isDebugEnabled()) {
                log.debug("  Superclass {}", superclass.getName());
            }
            updateAsSubclassTo(superclassSpec);
        }
    }

    protected void updateInterfaces(final List<ObjectSpecification> interfaces) {
        synchronized(unmodifiableInterfaces) {
            this.interfaces.clear();
            this.interfaces.addAll(interfaces);
            unmodifiableInterfaces.clear();
        }
    }

    private void updateAsSubclassTo(final ObjectSpecification supertypeSpec) {
        if (!(supertypeSpec instanceof ObjectSpecificationAbstract)) {
            return;
        }
        // downcast required because addSubclass is (deliberately) not public
        // API
        val introspectableSpec = (ObjectSpecificationAbstract) supertypeSpec;
        introspectableSpec.updateSubclasses(this);
    }

    protected void updateAsSubclassTo(final List<ObjectSpecification> supertypeSpecs) {
        for (final ObjectSpecification supertypeSpec : supertypeSpecs) {
            updateAsSubclassTo(supertypeSpec);
        }
    }

    private void updateSubclasses(final ObjectSpecification subclass) {
        this.directSubclasses.addSubclass(subclass);
    }

    protected void sortAndUpdateAssociations(final List<ObjectAssociation> associations) {
        val orderedAssociations = Utils.sortAssociations(associations);
        synchronized (unmodifiableAssociations) {
            this.associations.clear();
            this.associations.addAll(orderedAssociations);
            unmodifiableAssociations.clear(); // invalidate
        }
    }

    protected void sortCacheAndUpdateActions(final List<ObjectAction> objectActions) {
        val orderedActions = Utils.sortActions(objectActions);
        synchronized (unmodifiableActions){
            this.objectActions.clear();
            this.objectActions.addAll(orderedActions);
            unmodifiableActions.clear(); // invalidate

            for (val actionType : ActionType.values()) {
                val objectActionForType = objectActionsByType.getOrElseNew(actionType);
                objectActionForType.clear();
                objectActions.stream()
                .filter(ObjectAction.Predicates.ofType(actionType))
                .forEach(objectActionForType::add);
            }
        }
    }


    private void updateFromFacetValues() {

        titleFacet = getFacet(TitleFacet.class);
        iconFacet = getFacet(IconFacet.class);
        navigableParentFacet = getFacet(NavigableParentFacet.class);
        cssClassFacet = getFacet(CssClassFacet.class);
    }


    protected void postProcess() {
        postProcessor.postProcess(this);
        updateFromFacetValues();
    }


    @Override
    public String getTitle(
            final ManagedObject contextAdapterIfAny,
            final ManagedObject targetAdapter) {
        
        if (titleFacet != null) {
            val titleString = titleFacet.title(contextAdapterIfAny, targetAdapter);
            if (!_Strings.isEmpty(titleString)) {
                return titleString;
            }
        }
        return (this.isManagedBean() ? "" : "Untitled ") + getSingularName();
    }


    @Override
    public String getIconName(final ManagedObject reference) {
        return iconFacet == null ? null : iconFacet.iconName(reference);
    }

    @Override
    public Object getNavigableParent(final Object object) {
        return navigableParentFacet == null
                ? null
                        : navigableParentFacet.navigableParent(object);
    }

    @Override
    public String getCssClass(final ManagedObject reference) {
        return cssClassFacet == null ? null : cssClassFacet.cssClass(reference);
    }

    // -- HIERARCHICAL
    
    /**
     * Determines if this class represents the same class, or a subclass, of the
     * specified class.
     *
     * <p>
     * cf {@link Class#isAssignableFrom(Class)}, though target and parameter are
     * the opposite way around, ie:
     *
     * <pre>
     * cls1.isAssignableFrom(cls2);
     * </pre>
     * <p>
     * is equivalent to:
     *
     * <pre>
     * spec2.isOfType(spec1);
     * </pre>
     *
     * @return whether {@code other} is assignable from {@code this}
     *
     */
    @Override
    public boolean isOfType(final ObjectSpecification other) {
        
        // do the comparison using value types because of a possible aliasing/race condition
        // in matchesParameterOf when building up contributed associations
        if (other.getSpecId().equals(this.getSpecId())) {
            return true;
        }
        
        val thisClass = this.getCorrespondingClass();
        val otherClass = other.getCorrespondingClass();
        
        return otherClass.isAssignableFrom(thisClass);
        
//XXX legacy of ...        
//        
//        for (val interfaceSpec : interfaces()) {
//            if (interfaceSpec.isOfType(other)) {
//                return true;
//            }
//        }
//
//        // this is a bit of a workaround; the metamodel doesn't have the interfaces for enums.
//        val correspondingClass = getCorrespondingClass();
//        val possibleSupertypeClass = other.getCorrespondingClass();
//        if(correspondingClass != null && possibleSupertypeClass != null &&
//                Enum.class.isAssignableFrom(correspondingClass) && possibleSupertypeClass.isInterface()) {
//            if(possibleSupertypeClass.isAssignableFrom(correspondingClass)) {
//                return true;
//            }
//        }
//
//        val superclassSpec = superclass();
//        return superclassSpec != null && superclassSpec.isOfType(other);
    }

    // -- NAME, DESCRIPTION, PERSISTABILITY
    
    /**
     * The name according to any available {@link NamedFacet},
     * but falling back to {@link #getFullIdentifier()} otherwise.
     */
    @Override
    public String getSingularName() {
        val namedFacet = getFacet(NamedFacet.class);
        return namedFacet != null? namedFacet.value() : this.getFullIdentifier();
    }

    /**
     * The pluralized name according to any available {@link PluralFacet},
     * else <tt>null</tt>.
     */
    @Override
    public String getPluralName() {
        val pluralFacet = getFacet(PluralFacet.class);
        return pluralFacet.value();
    }

    /**
     * The description according to any available {@link PluralFacet},
     * else empty string (<tt>""</tt>).
     */
    @Override
    public String getDescription() {
        val describedAsFacet = getFacet(DescribedAsFacet.class);
        val describedAs = describedAsFacet.value();
        return describedAs == null ? "" : describedAs;
    }

    /*
     * help is typically a reference (eg a URL) and so should not default to a
     * textual value if not set up
     */
    @Override
    public String getHelp() {
        val helpFacet = getFacet(HelpFacet.class);
        return helpFacet == null ? null : helpFacet.value();
    }


    // -- FACET HANDLING

    @Override
    public <Q extends Facet> Q getFacet(final Class<Q> facetType) {

        synchronized(unmodifiableInterfaces) {
        
            // lookup facet holder's facet
            val facets1 = _NullSafe.streamNullable(super.getFacet(facetType));
    
            // lookup all interfaces
            val facets2 = _NullSafe.stream(interfaces())
                    .filter(_NullSafe::isPresent) // just in case
                    .map(interfaceSpec->interfaceSpec.getFacet(facetType));
    
            // search up the inheritance hierarchy
            val facets3 = _NullSafe.streamNullable(superclass()) 
                    .map(superSpec->superSpec.getFacet(facetType));
    
            val facetsCombined = _Streams.concat(facets1, facets2, facets3);

            val notANoopFacetFilter = new NotANoopFacetFilter<Q>();
    
            return facetsCombined
                    .filter(notANoopFacetFilter)
                    .findFirst()
                    .orElse(notANoopFacetFilter.noopFacet);
        
        }
    }

    @Vetoed
    private static class NotANoopFacetFilter<Q extends Facet> implements Predicate<Q> {
        Q noopFacet;

        @Override
        public boolean test(Q facet) {
            if(facet==null) {
                return false;
            }
            if(!facet.isFallback()) {
                return true;
            }
            if(noopFacet == null) {
                noopFacet = facet;
            }
            return false;
        }
    }


    // -- DefaultValue - unused
    /**
     * @deprecated  - never called.
     * @return - always returns <tt>null</tt>
     */
    @Deprecated
    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public ObjectTitleContext createTitleInteractionContext(
            final AuthenticationSession session, 
            final InteractionInitiatedBy interactionMethod, 
            final ManagedObject targetObjectAdapter) {

        return new ObjectTitleContext(targetObjectAdapter, getIdentifier(), targetObjectAdapter.titleString(null),
                interactionMethod);
    }

    // -- SUPERCLASS, INTERFACES, SUBCLASSES, IS-ABSTRACT
    
    @Override
    public ObjectSpecification superclass() {
        return superclassSpec;
    }

    @Override
    public Collection<ObjectSpecification> interfaces() {
        return unmodifiableInterfaces.get();
    }

    @Override
    public Collection<ObjectSpecification> subclasses(final Depth depth) {
        if (depth == Depth.DIRECT) {
            return directSubclasses.toCollection();
        }

        // depth == Depth.TRANSITIVE)
        if (transitiveSubclasses == null) {
            transitiveSubclasses = transitiveSubclasses();
        }

        return transitiveSubclasses.toCollection();
    }

    private synchronized Subclasses transitiveSubclasses() {
        final Subclasses appendTo = new Subclasses();
        appendSubclasses(this, appendTo);
        transitiveSubclasses = appendTo;
        return transitiveSubclasses;
    }

    private void appendSubclasses(
            final ObjectSpecification objectSpecification,
            final Subclasses appendTo) {

        val directSubclasses = objectSpecification.subclasses(Depth.DIRECT);
        for (ObjectSpecification subclass : directSubclasses) {
            appendTo.addSubclass(subclass);
            appendSubclasses(subclass, appendTo);
        }

    }

    @Override
    public boolean hasSubclasses() {
        return directSubclasses.hasSubclasses();
    }

    @Override
    public final boolean isAbstract() {
        return isAbstract;
    }

    // -- ASSOCIATIONS

    @Override
    public Stream<ObjectAssociation> streamAssociations(final Contributed contributed) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        guardAgainstTooEarly_assoz(contributed);
        
        synchronized(unmodifiableAssociations) {
            return stream(unmodifiableAssociations.get())
                    .filter(ContributeeMember.Predicates.regularElse(contributed));    
        }
        
    }

    @Override
    public Optional<? extends ObjectMember> getMember(final String memberId) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        val objectAction = getObjectAction(memberId);
        if(objectAction.isPresent()) {
            return objectAction;
        }
        val association = getAssociation(memberId);
        if(association.isPresent()) {
            return association;
        }
        return Optional.empty();
    }


    /**
     * The association with the given {@link ObjectAssociation#getId() id}.
     *
     * <p>
     * This is overridable because {@link ObjectSpecificationOnContainer}
     * simply returns <tt>null</tt>.
     *
     * <p>
     * TODO put fields into hash.
     *
     * <p>
     * TODO: could this be made final? (ie does the framework ever call this
     * method for an {@link ObjectSpecificationOnContainer})
     */
    @Override
    public Optional<ObjectAssociation> getAssociation(final String id) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        return streamAssociations(Contributed.INCLUDED)
                .filter(objectAssociation->objectAssociation.getId().equals(id))
                .findFirst();
    }

    @Override
    public Stream<ObjectAction> streamObjectActions(final ActionType type, final Contributed contributed) {
        introspectUpTo(IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);

        guardAgainstTooEarly_contrib(contributed);

        return stream(objectActionsByType.get(type))
                .filter(ContributeeMember.Predicates.regularElse(contributed));
    }

    private Stream<ManagedBeanAdapter> streamManagedBeans() {
        return getMetaModelContext().getServiceRegistry().streamRegisteredBeans();
    }

    // -- CONTRIBUTEE ASSOCIATIONS (PROPERTIES AND COLLECTIONS)

    private List<ObjectAssociation> createContributeeAssociations() {
        if (isManagedBean() || isValue()) {
            return Collections.emptyList();
        }
        val contributeeAssociations = _Lists.<ObjectAssociation>newArrayList();
        streamManagedBeans()
        .forEach(serviceBean->forEachContributeeAssociation(serviceBean, contributeeAssociations::add));
        return contributeeAssociations;
    }

    private void forEachContributeeAssociation(
            ManagedBeanAdapter serviceBean, 
            Consumer<ObjectAssociation> onNewContributeeAssociation) {

        val serviceClass = serviceBean.getBeanClass();
        val specification = getSpecificationLoader().loadSpecification(serviceClass,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        
        if(specification==null) {
            throw _Exceptions.unrecoverableFormatted("failed to load specification for service %s", serviceClass);
        }
        
        if (specification == this) {
            return;
        }
        
        final Stream<ObjectAction> serviceActions = specification
                .streamObjectActions(ActionType.USER, Contributed.INCLUDED);

        serviceActions
                .filter(Predicates.isContributeeAssociation(this))
                .map(ObjectActionDefault.class::cast)
                .map(Factories.contributeeAssociation(serviceBean, this))
                .peek(facetProcessor::processMemberOrder)
                .forEach(onNewContributeeAssociation);
    }


    // -- mixin associations (properties and collections)

    private List<ObjectAssociation> createMixedInAssociations() {
        if (isManagedBean() || isValue()) {
            return Collections.emptyList();
        }

        val mixinTypes = getIsisBeanTypeRegistry().getMixinTypes();
        if(_NullSafe.isEmpty(mixinTypes)) {
            return Collections.emptyList();
        }

        val mixedInAssociations = _Lists.<ObjectAssociation>newArrayList();

        for (val mixinType : mixinTypes) {
            forEachMixedInAssociation(mixinType, mixedInAssociations::add);
        }
        return mixedInAssociations;
    }

    private void forEachMixedInAssociation(
            final Class<?> mixinType, 
            final Consumer<ObjectAssociation> onNewMixedInAssociation) {

        val specification = getSpecificationLoader().loadSpecification(mixinType,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        if (specification == this) {
            return;
        }
        val mixinFacet = specification.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            // this shouldn't happen; perhaps it would be more correct to throw an exception?
            return;
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return;
        }
        val mixinMethodName = mixinFacet.value();

        final Stream<ObjectAction> mixinActions = specification
                .streamObjectActions(ActionType.ALL, Contributed.INCLUDED);

        mixinActions
        .filter(Predicates::isMixedInAssociation)
        .map(ObjectActionDefault.class::cast)
        .map(Factories.mixedInAssociation(this, mixinType, mixinMethodName))
        .peek(facetProcessor::processMemberOrder)
        .forEach(onNewMixedInAssociation);

    }


    // -- contributee actions
    /**
     * All contributee actions (each wrapping a service's contributed action) for this spec.
     *
     * <p>
     * If this specification {@link #isManagedBean() is actually for} a service,
     * then returns an empty list.
     */
    private List<ObjectAction> createContributeeActions() {
        if (isManagedBean() || isValue()) {
            return Collections.emptyList();
        }
        val contributeeActions = _Lists.<ObjectAction>newArrayList();
        streamManagedBeans()
        .forEach(serviceBean->forEachContributeeAction(serviceBean, contributeeActions::add));
        return contributeeActions;
    }



    private void forEachContributeeAction(
            final Object servicePojo,
            final Consumer<ObjectAction> onNewContributeeAction) {

        if(log.isDebugEnabled()) {
            log.debug("{} : addContributeeActionsIfAny(...); servicePojo class is: {}", 
                    this.getFullIdentifier(), servicePojo.getClass().getName());
        }

        val serviceType = servicePojo.getClass();
        val specification = getSpecificationLoader().loadSpecification(serviceType,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        if (specification == this) {
            return;
        }

        final Stream<ObjectAction> serviceActions = specification
                .streamObjectActions(ActionType.ALL, Contributed.INCLUDED);

        serviceActions
        .filter(Predicates.isContributeeAction(this))
        .map(ObjectActionDefault.class::cast)
        .map(Factories.contributeeAction(this, servicePojo))
        .peek(facetProcessor::processMemberOrder)
        .forEach(onNewContributeeAction);

    }


    // -- mixin actions
    /**
     * All contributee actions (each wrapping a service's contributed action) for this spec.
     *
     * <p>
     * If this specification {@link #isManagedBean() is actually for} a service,
     * then returns an empty list.
     */
    private List<ObjectAction> createMixedInActions() {
        if (isManagedBean() || isValue() || isMixin()) {
            return Collections.emptyList();
        }

        val mixinTypes = getIsisBeanTypeRegistry().getMixinTypes();
        if(_NullSafe.isEmpty(mixinTypes)) {
            return Collections.emptyList();
        }

        val mixedInActions = _Lists.<ObjectAction>newArrayList();
        for (val mixinType : mixinTypes) {
            forEachMixedInAction(mixinType, mixedInActions::add);
        }
        return mixedInActions;
    }



    private void forEachMixedInAction(
            final Class<?> mixinType,
            final Consumer<ObjectAction> onNewMixedInAction) {

        val mixinSpec = getSpecificationLoader().loadSpecification(mixinType,
                IntrospectionState.TYPE_AND_MEMBERS_INTROSPECTED);
        if (mixinSpec == this) {
            return;
        }
        val mixinFacet = mixinSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            // this shouldn't happen; perhaps it would be more correct to throw an exception?
            return;
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return;
        }
        val mixinMethodName = mixinFacet.value();

        final Stream<ObjectAction> mixinActions = mixinSpec
                .streamObjectActions(ActionType.ALL, Contributed.INCLUDED);

        mixinActions
        .filter(Predicates::isMixedInAction)
        .map(ObjectActionDefault.class::cast)
        .map(Factories.mixedInAction(this, mixinType, mixinMethodName))
        .peek(facetProcessor::processMemberOrder)
        .forEach(onNewMixedInAction);

    }

    // -- VALIDITY
    
    @Override
    public Consent isValid(
            final ManagedObject targetAdapter, 
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return isValidResult(targetAdapter, interactionInitiatedBy).createConsent();
    }

    @Override
    public InteractionResult isValidResult(
            final ManagedObject targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        val validityContext =
                createValidityInteractionContext(
                        targetAdapter, interactionInitiatedBy);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    /**
     * Create an {@link InteractionContext} representing an attempt to save the
     * object.
     */
    @Override
    public ObjectValidityContext createValidityInteractionContext(
            final ManagedObject targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return new ObjectValidityContext(targetAdapter, getIdentifier(), interactionInitiatedBy);
    }

    protected BeanSort managedObjectSort; 

    @Override
    public BeanSort getBeanSort() {
        if(managedObjectSort==null) {
            managedObjectSort = sortOf(this);
        }
        return managedObjectSort;
    }

    // -- convenience isXxx (looked up from facets)
    @Override
    public boolean isImmutable() {
        return containsFacet(ImmutableFacet.class);
    }

    @Override
    public boolean isHidden() {
        return containsFacet(HiddenFacet.class);
    }

    @Override
    public boolean isParseable() {
        return containsFacet(ParseableFacet.class);
    }

    @Override
    public boolean isEncodeable() {
        return containsFacet(EncodableFacet.class);
    }

    @Override
    public boolean isParented() {
        return containsFacet(ParentedCollectionFacet.class);
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullIdentifier());
        return str.toString();
    }
    
    // -- GUARDS

    private boolean contributeeAndMixedInAssociationsAdded;
    private boolean contributeeAndMixedInActionsAdded;

    private void guardAgainstTooEarly_contrib(Contributed contributed) {
        // update our list of actions if requesting for contributed actions
        // and they have not yet been added
        // the "contributed.isIncluded()" guard is required because we cannot do this too early;
        // there must be a session available
        if(contributed.isIncluded() && !contributeeAndMixedInActionsAdded) {
            synchronized (unmodifiableActions) {
                val actions = _Lists.newArrayList(this.objectActions);
                if (isEntityOrViewModel()) {
                    actions.addAll(createContributeeActions());
                    actions.addAll(createMixedInActions());
                }
                sortCacheAndUpdateActions(actions);
                contributeeAndMixedInActionsAdded = true;
            }
        }
    }

    private void guardAgainstTooEarly_assoz(Contributed contributed) {
        // the "contributed.isIncluded()" guard is required because we cannot do this too early;
        // there must be a session available
        if(contributed.isIncluded() && !contributeeAndMixedInAssociationsAdded) {
            synchronized (unmodifiableAssociations) {
                val associations = _Lists.newArrayList(this.associations);
                if(isEntityOrViewModel()) {
                    associations.addAll(createContributeeAssociations());
                    associations.addAll(createMixedInAssociations());
                }
                sortAndUpdateAssociations(associations);
                contributeeAndMixedInAssociationsAdded = true;
            }
        }
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getMetaModelContext().getSpecificationLoader();
    }
    
    protected IsisBeanTypeRegistry getIsisBeanTypeRegistry() {
        return getMetaModelContext().getServiceRegistry()
                .lookupServiceElseFail(IsisBeanTypeRegistryHolder.class)
                .getIsisBeanTypeRegistry();
    }

    //TODO just make 'sort' a field of ObjectSpecification 
    protected BeanSort sortOf(ObjectSpecification spec) {

        if(isManagedBean()) { // <-- not a facet, because we get this information earlier (during class scanning)
            return BeanSort.MANAGED_BEAN_CONTRIBUTING;
        }
        if(containsFacet(ValueFacet.class)) {
            return BeanSort.VALUE;
        }
        if(containsFacet(ViewModelFacet.class)) {
            return BeanSort.VIEW_MODEL;
        }
        if(containsFacet(MixinFacet.class)) {
            return BeanSort.MIXIN;
        }
        if(containsFacet(CollectionFacet.class)) {
            return BeanSort.COLLECTION;
        }
        if(containsFacet(EntityFacet.class)) {
            return BeanSort.ENTITY;
        }

        return BeanSort.UNKNOWN;
    }



}
