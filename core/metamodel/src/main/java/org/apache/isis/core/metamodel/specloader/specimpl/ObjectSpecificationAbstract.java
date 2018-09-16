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

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Collections2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
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
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.layout.DeweyOrderSet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

public abstract class ObjectSpecificationAbstract extends FacetHolderImpl implements ObjectSpecification {

    private final static Logger LOG = LoggerFactory.getLogger(ObjectSpecificationAbstract.class);

    private static class SubclassList {
        private final List<ObjectSpecification> classes = _Lists.newArrayList();

        public void addSubclass(final ObjectSpecification subclass) {
            if(classes.contains(subclass)) {
                return;
            }
            classes.add(subclass);
        }

        public boolean hasSubclasses() {
            return !classes.isEmpty();
        }

        public List<ObjectSpecification> toList() {
            return Collections.unmodifiableList(classes);
        }
    }

    // -- fields

    protected final ServicesInjector servicesInjector;

    private final DeploymentCategory deploymentCategory;
    private final SpecificationLoader specificationLoader;
    private final FacetProcessor facetProcessor;

    private final List<ObjectAssociation> associations = _Lists.newArrayList(); 
    private final List<ObjectAction> objectActions = _Lists.newArrayList();
    // partitions and caches objectActions by type; updated in sortCacheAndUpdateActions()
    private final Map<ActionType, List<ObjectAction>> objectActionsByType = createObjectActionsByType();

    private static Map<ActionType, List<ObjectAction>> createObjectActionsByType() {
        final Map<ActionType, List<ObjectAction>> map = _Maps.newHashMap();
        for (final ActionType type : ActionType.values()) {
            map.put(type, _Lists.<ObjectAction>newArrayList());
        }
        return map;
    }

    private final List<ObjectSpecification> interfaces = _Lists.newArrayList();
    private final SubclassList directSubclasses = new SubclassList();
    // built lazily
    private SubclassList transitiveSubclasses;

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

    private IntrospectionState introspected = IntrospectionState.NOT_INTROSPECTED;


    // -- Constructor
    public ObjectSpecificationAbstract(
            final Class<?> introspectedClass,
            final String shortName,
            final ServicesInjector servicesInjector,
            final FacetProcessor facetProcessor) {

        this.correspondingClass = introspectedClass;
        this.fullName = introspectedClass.getName();
        this.shortName = shortName;

        this.isAbstract = ClassExtensions.isAbstract(introspectedClass);
        this.identifier = Identifier.classIdentifier(introspectedClass);

        this.servicesInjector = servicesInjector;
        this.facetProcessor = facetProcessor;

        this.specificationLoader = servicesInjector.getSpecificationLoader();
        this.deploymentCategory = servicesInjector.getDeploymentCategoryProvider().getDeploymentCategory();
    }




    // -- Stuff immediately derivable from class
    @Override
    public FeatureType getFeatureType() {
        return FeatureType.OBJECT;
    }

    @Override
    public ObjectSpecId getSpecId() {
        if(specId == null) {
            final ObjectSpecIdFacet facet = getFacet(ObjectSpecIdFacet.class);
            if(facet == null) {
                throw new IllegalStateException("could not find an ObjectSpecIdFacet for " + this.getFullIdentifier());
            }
            specId = facet.value();
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


    public enum IntrospectionState {
        NOT_INTROSPECTED,
        BEING_INTROSPECTED,
        INTROSPECTED,
    }

    /**
     * Only if {@link #setIntrospectionState(org.apache.isis.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract.IntrospectionState)}
     * has been called (should be called within {@link #updateFromFacetValues()}.
     */
    public IntrospectionState getIntrospectionState() {
        return introspected;
    }

    public void setIntrospectionState(IntrospectionState introspectationState) {
        this.introspected = introspectationState;
    }

    protected boolean isNotIntrospected() {
        return !(getIntrospectionState() == IntrospectionState.INTROSPECTED);
    }



    // -- Introspection (part 1)

    public abstract void introspectTypeHierarchyAndMembers();

    /**
     * Intended to be called within {@link #introspectTypeHierarchyAndMembers()}
     * .
     */
    protected void updateSuperclass(final Class<?> superclass) {
        if (superclass == null) {
            return;
        }
        superclassSpec = getSpecificationLoader().loadSpecification(superclass);
        if (superclassSpec != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  Superclass {}", superclass.getName());
            }
            updateAsSubclassTo(superclassSpec);
        }
    }

    /**
     * Intended to be called within {@link #introspectTypeHierarchyAndMembers()}
     * .
     */
    protected void updateInterfaces(final List<ObjectSpecification> interfaces) {
        this.interfaces.clear();
        this.interfaces.addAll(interfaces);
    }

    /**
     * Intended to be called within {@link #introspectTypeHierarchyAndMembers()}
     * .
     */
    protected void updateAsSubclassTo(final ObjectSpecification supertypeSpec) {
        if (!(supertypeSpec instanceof ObjectSpecificationAbstract)) {
            return;
        }
        // downcast required because addSubclass is (deliberately) not public
        // API
        final ObjectSpecificationAbstract introspectableSpec = (ObjectSpecificationAbstract) supertypeSpec;
        introspectableSpec.updateSubclasses(this);
    }

    /**
     * Intended to be called within {@link #introspectTypeHierarchyAndMembers()}
     * .
     */
    protected void updateAsSubclassTo(final List<ObjectSpecification> supertypeSpecs) {
        for (final ObjectSpecification supertypeSpec : supertypeSpecs) {
            updateAsSubclassTo(supertypeSpec);
        }
    }

    private void updateSubclasses(final ObjectSpecification subclass) {
        this.directSubclasses.addSubclass(subclass);
    }

    protected void sortAndUpdateAssociations(final List<ObjectAssociation> associations) {
        final List<ObjectAssociation> orderedAssociations = sortAssociations(associations);
        synchronized (this.associations) {
            this.associations.clear();
            this.associations.addAll(orderedAssociations);
        }
    }

    protected void sortCacheAndUpdateActions(final List<ObjectAction> objectActions) {
        final List<ObjectAction> orderedActions = sortActions(objectActions);
        synchronized (this.objectActions){
            this.objectActions.clear();
            this.objectActions.addAll(orderedActions);

            for (final ActionType type : ActionType.values()) {
                final List<ObjectAction> objectActionForType = objectActionsByType.get(type);
                objectActionForType.clear();
                objectActionForType.addAll(Collections2
                        .filter(objectActions, ObjectAction.Predicates.ofType(type)::test));
            }
        }
    }

    // -- Introspection (part 2)

    public void updateFromFacetValues() {

        titleFacet = getFacet(TitleFacet.class);
        iconFacet = getFacet(IconFacet.class);
        navigableParentFacet = getFacet(NavigableParentFacet.class);
        cssClassFacet = getFacet(CssClassFacet.class);
    }

    // -- Title, Icon

    @Override
    public String getTitle(
            ObjectAdapter contextAdapterIfAny,
            ObjectAdapter targetAdapter) {
        if (titleFacet != null) {
            final String titleString = titleFacet.title(contextAdapterIfAny, targetAdapter);
            if (!_Strings.isEmpty(titleString)) {
                return titleString;
            }
        }
        return (this.isService() ? "" : "Untitled ") + getSingularName();
    }


    @Override
    public String getIconName(final ObjectAdapter reference) {
        return iconFacet == null ? null : iconFacet.iconName(reference);
    }

    @Override
    public Object getNavigableParent(final Object object) {
        return navigableParentFacet == null
                ? null
                        : navigableParentFacet.navigableParent(object);
    }

    @Override
    public String getCssClass(final ObjectAdapter reference) {
        return cssClassFacet == null ? null : cssClassFacet.cssClass(reference);
    }


    // -- Hierarchical
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
     * <p>
     * Callable after {@link #introspectTypeHierarchyAndMembers()} has been
     * called.
     */
    @Override
    public boolean isOfType(final ObjectSpecification specification) {
        // do the comparison using value types because of a possible aliasing/race condition
        // in matchesParameterOf when building up contributed associations
        if (specification.getSpecId().equals(this.getSpecId())) {
            return true;
        }
        for (final ObjectSpecification interfaceSpec : interfaces()) {
            if (interfaceSpec.isOfType(specification)) {
                return true;
            }
        }
        final ObjectSpecification superclassSpec = superclass();
        return superclassSpec != null && superclassSpec.isOfType(specification);
    }



    // -- Name, Description, Persistability
    /**
     * The name according to any available {@link org.apache.isis.core.metamodel.facets.all.named.NamedFacet},
     * but falling back to {@link #getFullIdentifier()} otherwise.
     */
    @Override
    public String getSingularName() {
        final NamedFacet namedFacet = getFacet(NamedFacet.class);
        return namedFacet != null? namedFacet.value() : this.getFullIdentifier();
    }

    /**
     * The pluralized name according to any available {@link org.apache.isis.core.metamodel.facets.object.plural.PluralFacet},
     * else <tt>null</tt>.
     */
    @Override
    public String getPluralName() {
        final PluralFacet pluralFacet = getFacet(PluralFacet.class);
        return pluralFacet.value();
    }

    /**
     * The description according to any available {@link org.apache.isis.core.metamodel.facets.object.plural.PluralFacet},
     * else empty string (<tt>""</tt>).
     */
    @Override
    public String getDescription() {
        final DescribedAsFacet describedAsFacet = getFacet(DescribedAsFacet.class);
        final String describedAs = describedAsFacet.value();
        return describedAs == null ? "" : describedAs;
    }

    /*
     * help is typically a reference (eg a URL) and so should not default to a
     * textual value if not set up
     */
    @Override
    public String getHelp() {
        final HelpFacet helpFacet = getFacet(HelpFacet.class);
        return helpFacet == null ? null : helpFacet.value();
    }




    // -- Facet Handling

    @Override
    public <Q extends Facet> Q getFacet(final Class<Q> facetType) {
        final Q facet = super.getFacet(facetType);
        if (isNotANoopFacet(facet)) {
            return facet;
        }
        Q noopFacet = facet; // might be null
        if (interfaces() != null) {
            final List<ObjectSpecification> interfaces = interfaces();
            for (int i = 0; i < interfaces.size(); i++) {
                final ObjectSpecification interfaceSpec = interfaces.get(i);
                if (interfaceSpec == null) {
                    // HACK: shouldn't happen, but occurring on occasion when
                    // running
                    // XATs under JUnit4. Some sort of race condition?
                    continue;
                }
                final Q interfaceFacet = interfaceSpec.getFacet(facetType);
                if (isNotANoopFacet(interfaceFacet)) {
                    return interfaceFacet;
                }
                if (noopFacet == null) {
                    noopFacet = interfaceFacet; // might be null
                }
            }
        }
        // search up the inheritance hierarchy
        final ObjectSpecification superSpec = superclass();
        if (superSpec != null) {
            final Q superClassFacet = superSpec.getFacet(facetType);
            if (isNotANoopFacet(superClassFacet)) {
                return superClassFacet;
            }
            // TODO: should we perhaps default the noopFacet here as we do in the previous two cases?
        }
        return noopFacet;
    }

    private boolean isNotANoopFacet(final Facet facet) {
        return facet != null && !facet.isNoop();
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


    // -- Identifier
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }



    // -- createTitleInteractionContext
    @Override
    public ObjectTitleContext createTitleInteractionContext(final AuthenticationSession session, final InteractionInitiatedBy interactionMethod, final ObjectAdapter targetObjectAdapter) {
        return new ObjectTitleContext(targetObjectAdapter, getIdentifier(), targetObjectAdapter.titleString(null),
                interactionMethod);
    }



    // -- Superclass, Interfaces, Subclasses, isAbstract
    @Override
    public ObjectSpecification superclass() {
        return superclassSpec;
    }

    @Override
    public List<ObjectSpecification> interfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    @Override
    public List<ObjectSpecification> subclasses() {
        return subclasses(Depth.DIRECT);
    }

    @Override
    public List<ObjectSpecification> subclasses(final Depth depth) {
        if (depth == Depth.DIRECT) {
            return directSubclasses.toList();
        }

        // depth == Depth.TRANSITIVE)
        if (transitiveSubclasses == null) {
            transitiveSubclasses = transitiveSubclasses();
        }

        return transitiveSubclasses.toList();
    }

    private synchronized SubclassList transitiveSubclasses() {
        final SubclassList appendTo = new SubclassList();
        appendSubclasses(this, appendTo);
        transitiveSubclasses = appendTo;
        return transitiveSubclasses;
    }

    private SubclassList appendSubclasses(
            final ObjectSpecification objectSpecification,
            final SubclassList appendTo) {

        final List<ObjectSpecification> directSubclasses = objectSpecification.subclasses(Depth.DIRECT);
        for (ObjectSpecification subclass : directSubclasses) {
            appendTo.addSubclass(subclass);
            appendSubclasses(subclass, appendTo);
        }

        return appendTo;
    }

    @Override
    public boolean hasSubclasses() {
        return directSubclasses.hasSubclasses();
    }

    @Override
    public final boolean isAbstract() {
        return isAbstract;
    }

    // -- Associations
    
    @Override
    public Stream<ObjectAssociation> streamAssociations(final Contributed contributed) {
        
        guardAgainstTooEarly_assoz(contributed);
        
        return stream(this.associations)
        .filter(ContributeeMember.Predicates.regularElse(contributed));
    }


    private static ThreadLocal<Boolean> invalidatingCache = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        };
    };

    @Override
    public ObjectMember getMember(final String memberId) {
        final ObjectAction objectAction = getObjectAction(memberId);
        if(objectAction != null) {
            return objectAction;
        }
        final ObjectAssociation association = getAssociation(memberId);
        if(association != null) {
            return association;
        }
        return null;
    }


    /**
     * The association with the given {@link ObjectAssociation#getId() id}.
     *
     * <p>
     * This is overridable because {@link org.apache.isis.core.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList}
     * simply returns <tt>null</tt>.
     *
     * <p>
     * TODO put fields into hash.
     *
     * <p>
     * TODO: could this be made final? (ie does the framework ever call this
     * method for an {@link org.apache.isis.core.metamodel.specloader.specimpl.standalonelist.ObjectSpecificationOnStandaloneList})
     */
    @Override
    public ObjectAssociation getAssociation(final String id) {
        ObjectAssociation oa = getAssociationWithId(id);
        if(oa != null) {
            return oa;
        }
        if(! deploymentCategory.isProduction()) {
            // automatically refresh if not in production
            // (better support for jrebel)

            LOG.warn("Could not find association with id '{}'; invalidating cache automatically", id);
            if(!invalidatingCache.get()) {
                // make sure don't go into an infinite loop, though.
                try {
                    invalidatingCache.set(true);
                    getSpecificationLoader().invalidateCache(getCorrespondingClass());
                } finally {
                    invalidatingCache.set(false);
                }
            } else {
                LOG.warn("... already invalidating cache earlier in stacktrace, so skipped this time");
            }
            oa = getAssociationWithId(id);
            if(oa != null) {
                return oa;
            }
        }
        throw new ObjectSpecificationException(
                String.format("No association called '%s' in '%s'", id, getSingularName()));
    }

    private ObjectAssociation getAssociationWithId(final String id) {
        return streamAssociations(Contributed.INCLUDED)
            .filter(objectAssociation->objectAssociation.getId().equals(id))
            .findFirst()
            .orElse(null);
    }

    @Override
    public Stream<ObjectAction> streamObjectActions(final ActionType type, final Contributed contributed) {
        
        guardAgainstTooEarly_contrib(contributed);
        
        return stream(objectActionsByType.get(type))
                .filter(ContributeeMember.Predicates.regularElse(contributed));
    }

    // -- sorting

    protected List<ObjectAssociation> sortAssociations(final List<ObjectAssociation> associations) {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(associations);
        final List<ObjectAssociation> orderedAssociations = _Lists.newArrayList();
        sortAssociations(orderSet, orderedAssociations);
        return orderedAssociations;
    }

    private static void sortAssociations(final DeweyOrderSet orderSet, final List<ObjectAssociation> associationsToAppendTo) {
        for (final Object element : orderSet) {
            if (element instanceof OneToManyAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof OneToOneAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof DeweyOrderSet) {
                // just flatten.
                DeweyOrderSet childOrderSet = (DeweyOrderSet) element;
                sortAssociations(childOrderSet, associationsToAppendTo);
            } else {
                throw new UnknownTypeException(element);
            }
        }
    }

    protected static List<ObjectAction> sortActions(final List<ObjectAction> actions) {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(actions);
        final List<ObjectAction> orderedActions = _Lists.newArrayList();
        sortActions(orderSet, orderedActions);
        return orderedActions;
    }

    private static void sortActions(final DeweyOrderSet orderSet, final List<ObjectAction> actionsToAppendTo) {
        for (final Object element : orderSet) {
            if(element instanceof ObjectAction) {
                final ObjectAction objectAction = (ObjectAction) element;
                actionsToAppendTo.add(objectAction);
            }
            else if (element instanceof DeweyOrderSet) {
                final DeweyOrderSet set = ((DeweyOrderSet) element);
                final List<ObjectAction> actions = _Lists.newArrayList();
                sortActions(set, actions);
                actionsToAppendTo.addAll(actions);
            } else {
                throw new UnknownTypeException(element);
            }
        }
    }

    private Iterable<Object> getServicePojos() {
        return getServicesInjector().getRegisteredServices();
    }



    // -- contributee associations (properties and collections)

    private List<ObjectAssociation> createContributeeAssociations() {
        if (isService() || isValue()) {
            return Collections.emptyList();
        }

        final List<ObjectAssociation> contributeeAssociations = _Lists.newArrayList();
        for (final Object servicePojo : getServicePojos()) {
            addContributeeAssociationsIfAny(servicePojo, contributeeAssociations);
        }
        return contributeeAssociations;
    }

    private void addContributeeAssociationsIfAny(
            final Object servicePojo, final List<ObjectAssociation> contributeeAssociationsToAppendTo) {
        final Class<?> serviceClass = servicePojo.getClass();
        final ObjectSpecification specification = specificationLoader.loadSpecification(serviceClass);
        if (specification == this) {
            return;
        }
        final List<ObjectAssociation> contributeeAssociations = createContributeeAssociations(servicePojo);
        contributeeAssociationsToAppendTo.addAll(contributeeAssociations);
    }

    private boolean canAdd(ObjectAction serviceAction) {
        if (isAlwaysHidden(serviceAction)) {
            return false;
        }
        final NotContributedFacet notContributed = serviceAction.getFacet(NotContributedFacet.class);
        if(notContributed != null && notContributed.toAssociations()) {
            return false;
        }
        if(!serviceAction.hasReturn()) {
            return false;
        }
        if (serviceAction.getParameterCount() != 1 || contributeeParameterMatchOf(serviceAction) == -1) {
            return false;
        }
        if(!(serviceAction instanceof ObjectActionDefault)) {
            return false;
        }
        if(!serviceAction.getSemantics().isSafeInNature()) {
            return false;
        }
        return true;
    }
    
    private List<ObjectAssociation> createContributeeAssociations(final Object servicePojo) {
        final Class<?> serviceClass = servicePojo.getClass();
        final ObjectSpecification specification = specificationLoader.loadSpecification(serviceClass);
        final Stream<ObjectAction> serviceActions = specification
                .streamObjectActions(ActionType.USER, Contributed.INCLUDED);
        
        return serviceActions
            .filter(this::canAdd)
            .map(serviceAction->(ObjectActionDefault) serviceAction)
            .map(createContributeeAssociationFunctor(servicePojo, this))
            .collect(Collectors.toList());
  
    }

    private Function<ObjectActionDefault, ObjectAssociation> createContributeeAssociationFunctor(
            final Object servicePojo,
            final ObjectSpecification contributeeType) {
        
        return new Function<ObjectActionDefault, ObjectAssociation>(){
            @Override
            public ObjectAssociation apply(ObjectActionDefault input) {
                final ObjectSpecification returnType = input.getReturnType();
                final ObjectAssociationAbstract association = createObjectAssociation(input, returnType);
                facetProcessor.processMemberOrder(association);
                return association;
            }

            private ObjectAssociationAbstract createObjectAssociation(
                    final ObjectActionDefault input,
                    final ObjectSpecification returnType) {
                if (returnType.isNotCollection()) {
                    return new OneToOneAssociationContributee(servicePojo, input, contributeeType,
                            servicesInjector);
                } else {
                    return new OneToManyAssociationContributee(servicePojo, input, contributeeType,
                            servicesInjector);
                }
            }
        };
    }



    // -- mixin associations (properties and collections)

    private List<ObjectAssociation> createMixedInAssociations() {
        if (isService() || isValue()) {
            return Collections.emptyList();
        }

        final Set<Class<?>> mixinTypes = AppManifest.Registry.instance().getMixinTypes();
        if(mixinTypes == null) {
            return Collections.emptyList();
        }

        final List<ObjectAssociation> mixedInAssociations = _Lists.newArrayList();

        for (final Class<?> mixinType : mixinTypes) {
            addMixedInAssociationsIfAny(mixinType, mixedInAssociations);
        }
        return mixedInAssociations;
    }

    private void addMixedInAssociationsIfAny(
            final Class<?> mixinType, final List<ObjectAssociation> toAppendTo) {

        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(mixinType);
        if (specification == this) {
            return;
        }
        final MixinFacet mixinFacet = specification.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            // this shouldn't happen; perhaps it would be more correct to throw an exception?
            return;
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return;
        }

        final Stream<ObjectActionDefault> mixinActions = objectActionsOf(specification);
        
        mixinActions
        .filter((ObjectActionDefault input) -> {
            final NotContributedFacet notContributedFacet = input.getFacet(NotContributedFacet.class);
            if (notContributedFacet == null || !notContributedFacet.toActions()) {
                return false;
            }
            if(input.getParameterCount() != 0) {
                return false;
            }
            if(!input.getSemantics().isSafeInNature()) {
                return false;
            }
            return true;
        })
        .map(createMixedInAssociationFunctor(this, mixinType, mixinFacet.value()))
        .forEach(toAppendTo::add);

    }

    private Stream<ObjectActionDefault> objectActionsOf(final ObjectSpecification specification) {
        return specification.streamObjectActions(ActionType.ALL, Contributed.INCLUDED)
                .map(a->(ObjectActionDefault)a);
    }

    private Function<ObjectActionDefault, ObjectAssociation> createMixedInAssociationFunctor(
            final ObjectSpecification mixedInType,
            final Class<?> mixinType,
            final String mixinMethodName) {
        return new Function<ObjectActionDefault, ObjectAssociation>(){
            @Override
            public ObjectAssociation apply(final ObjectActionDefault mixinAction) {
                final ObjectAssociationAbstract association = createObjectAssociation(mixinAction);
                facetProcessor.processMemberOrder(association);
                return association;
            }

            ObjectAssociationAbstract createObjectAssociation(
                    final ObjectActionDefault mixinAction) {
                final ObjectSpecification returnType = mixinAction.getReturnType();
                if (returnType.isNotCollection()) {
                    return new OneToOneAssociationMixedIn(
                            mixinAction, mixedInType, mixinType, mixinMethodName, servicesInjector);
                } else {
                    return new OneToManyAssociationMixedIn(
                            mixinAction, mixedInType, mixinType, mixinMethodName, servicesInjector);
                }
            }
        };
    }



    // -- contributee actions
    /**
     * All contributee actions (each wrapping a service's contributed action) for this spec.
     *
     * <p>
     * If this specification {@link #isService() is actually for} a service,
     * then returns an empty list.
     */
    protected List<ObjectAction> createContributeeActions() {
        if (isService() || isValue()) {
            return Collections.emptyList();
        }
        final List<ObjectAction> contributeeActions = _Lists.newArrayList();

        for (final Object servicePojo : getServicePojos()) {
            addContributeeActionsIfAny(servicePojo, contributeeActions);
        }
        return contributeeActions;
    }

    private boolean canAddContributee(final ObjectAction serviceAction) {
        if (isAlwaysHidden(serviceAction)) {
            return false;
        }
        final NotContributedFacet notContributed = serviceAction.getFacet(NotContributedFacet.class);
        if(notContributed != null && notContributed.toActions()) {
            return false;
        }
        if(!(serviceAction instanceof ObjectActionDefault)) {
            return false;
        }
        return true;
    }
    
    private void addContributeeActionsIfAny(
            final Object servicePojo,
            final List<ObjectAction> contributeeActionsToAppendTo) {
        final Class<?> serviceType = servicePojo.getClass();
        final ObjectSpecification specification = getSpecificationLoader().loadSpecification(serviceType);
        if (specification == this) {
            return;
        }
        
        final Stream<ObjectAction> serviceActions = specification
                .streamObjectActions(ActionType.ALL, Contributed.INCLUDED);
        
        serviceActions
        .filter(this::canAddContributee)
        .map(serviceAction->(ObjectActionDefault) serviceAction)
        .forEach(contributedAction->{
            
            // see if qualifies by inspecting all parameters
            final int contributeeParam = contributeeParameterMatchOf(contributedAction);
            if(contributeeParam == -1) {
                return;
            }
            
            ObjectActionContributee contributeeAction =
                    new ObjectActionContributee(servicePojo, contributedAction, contributeeParam, this,
                            servicesInjector);
            facetProcessor.processMemberOrder(contributeeAction);
            contributeeActionsToAppendTo.add(contributeeAction);
        });
        
    }

    private boolean isAlwaysHidden(final FacetHolder holder) {
        final HiddenFacet hiddenFacet = holder.getFacet(HiddenFacet.class);
        return hiddenFacet != null && hiddenFacet.where() == Where.ANYWHERE;
    }



    /**
     * @param serviceAction - number of the parameter that matches, or -1 if none.
     */
    private int contributeeParameterMatchOf(final ObjectAction serviceAction) {
        final List<ObjectActionParameter> params = serviceAction.getParameters();
        for (final ObjectActionParameter param : params) {
            if (isOfType(param.getSpecification())) {
                return param.getNumber();
            }
        }
        return -1;
    }


    // -- mixin actions
    /**
     * All contributee actions (each wrapping a service's contributed action) for this spec.
     *
     * <p>
     * If this specification {@link #isService() is actually for} a service,
     * then returns an empty list.
     */
    protected List<ObjectAction> createMixedInActions() {
        if (isService() || isValue()) {
            return Collections.emptyList();
        }
        final Set<Class<?>> mixinTypes = AppManifest.Registry.instance().getMixinTypes();
        if(mixinTypes == null) {
            return Collections.emptyList();
        }

        final List<ObjectAction> mixedInActions = _Lists.newArrayList();

        for (final Class<?> mixinType : mixinTypes) {
            addMixedInActionsIfAny(mixinType, mixedInActions);
        }
        return mixedInActions;
    }
    
    private boolean canAddMixin(ObjectAction mixinTypeAction) {
        if (isAlwaysHidden(mixinTypeAction)) {
            return false;
        }
        if(!(mixinTypeAction instanceof ObjectActionDefault)) {
            return false;
        }
        final ObjectActionDefault mixinAction = (ObjectActionDefault) mixinTypeAction;
        final NotContributedFacet notContributedFacet = mixinAction.getFacet(NotContributedFacet.class);
        if(notContributedFacet != null && notContributedFacet.toActions()) {
            return false;
        }
        return true;
    }

    private void addMixedInActionsIfAny(
            final Class<?> mixinType,
            final List<ObjectAction> mixedInActionsToAppendTo) {

        final ObjectSpecification mixinSpec = getSpecificationLoader().loadSpecification(mixinType);
        if (mixinSpec == this) {
            return;
        }
        final MixinFacet mixinFacet = mixinSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            // this shouldn't happen; perhaps it would be more correct to throw an exception?
            return;
        }
        if(!mixinFacet.isMixinFor(getCorrespondingClass())) {
            return;
        }

        final Stream<ObjectAction> mixinActions = mixinSpec
                .streamObjectActions(ActionType.ALL, Contributed.INCLUDED);
        
        mixinActions
        .filter(this::canAddMixin)
        .forEach(mixinTypeAction->{
            ObjectActionMixedIn mixedInAction =
                    new ObjectActionMixedIn(mixinType, mixinFacet.value(), (ObjectActionDefault)mixinTypeAction, this, servicesInjector);
            facetProcessor.processMemberOrder(mixedInAction);
            mixedInActionsToAppendTo.add(mixedInAction);
        });
        
    }



    // -- validity
    @Override
    public Consent isValid(final ObjectAdapter targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return isValidResult(targetAdapter, interactionInitiatedBy).createConsent();
    }

    @Override
    public InteractionResult isValidResult(
            final ObjectAdapter targetAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ObjectValidityContext validityContext =
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
            final ObjectAdapter targetAdapter, final InteractionInitiatedBy interactionInitiatedBy) {
        return new ObjectValidityContext(targetAdapter, getIdentifier(), interactionInitiatedBy);
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
    public boolean isValue() {
        return containsFacet(ValueFacet.class);
    }

    @Override
    public boolean isParented() {
        return containsFacet(ParentedCollectionFacet.class);
    }

    @Override
    public boolean isParentedOrFreeCollection() {
        return containsFacet(CollectionFacet.class);
    }

    @Override
    public boolean isNotCollection() {
        return !isParentedOrFreeCollection();
    }

    @Override
    public boolean isValueOrIsParented() {
        return isValue() || isParented();
    }

    @Override
    public boolean isPersistenceCapable() {
        return containsFacet(JdoPersistenceCapableFacet.class);
    }

    @Override
    public boolean isPersistenceCapableOrViewModel() {
        return isViewModel() || isPersistenceCapable();
    }

    // -- toString

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
            synchronized (this.objectActions) {
                final List<ObjectAction> actions = _Lists.newArrayList(this.objectActions);
                final boolean containsMixin = containsDoOpFacet(MixinFacet.class);
                final boolean containsDomainService = containsDoOpFacet(DomainServiceFacet.class);
                final boolean isService = isService();
                if (containsMixin || containsDomainService || isService) {
                    // don't contribute to mixins themselves!
                    // don't contribute to services either
                    // - isService() is sufficient check for internal services registered directly with ServicesInjector
                    // - checking for DomainServiceFacet is for application services (isService() may not have been called, for these)
                } else {
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
            synchronized (this.associations) {
                List<ObjectAssociation> associations = _Lists.newArrayList(this.associations);
                associations.addAll(createContributeeAssociations());
                associations.addAll(createMixedInAssociations());
                sortAndUpdateAssociations(associations);
                contributeeAndMixedInAssociationsAdded = true;
            }
        }
    }

    // -- Dependencies (injected in constructor)
    private ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }



}
