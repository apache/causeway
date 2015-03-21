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

import java.util.*;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.NotPersistable;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.help.HelpFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.parented.ParentedFacet;
import org.apache.isis.core.metamodel.facets.object.dirty.ClearDirtyObjectFacet;
import org.apache.isis.core.metamodel.facets.object.dirty.IsDirtyObjectFacet;
import org.apache.isis.core.metamodel.facets.object.dirty.MarkDirtyObjectFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.metamodel.facets.object.objectspecid.ObjectSpecIdFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.interactions.InteractionContext;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.interactions.ObjectValidityContext;
import org.apache.isis.core.metamodel.layout.DeweyOrderSet;
import org.apache.isis.core.metamodel.spec.*;
import org.apache.isis.core.metamodel.spec.feature.*;
import org.apache.isis.core.metamodel.specloader.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;

public abstract class ObjectSpecificationAbstract extends FacetHolderImpl implements ObjectSpecification {

    private final static Logger LOG = LoggerFactory.getLogger(ObjectSpecificationAbstract.class);

    private static class SubclassList {
        private final List<ObjectSpecification> classes = Lists.newArrayList();

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

    private final DeploymentCategory deploymentCategory;
    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final ServicesProvider servicesProvider;
    private final ObjectInstantiator objectInstantiator;
    private final SpecificationLoader specificationLookup;
    
    private final FacetProcessor facetProcessor;
    
    /**
     * Only populated once {@link #introspectTypeHierarchyAndMembers()} is called.
     */
    protected Properties metadataProperties;

    protected final ObjectMemberContext objectMemberContext;


    private final List<ObjectAssociation> associations = Lists.newArrayList();
    private final List<ObjectAction> objectActions = Lists.newArrayList();
    // partitions and caches objectActions by type; updated in sortCacheAndUpdateActions()
    private final Map<ActionType, List<ObjectAction>> objectActionsByType = createObjectActionsByType();

    private static Map<ActionType, List<ObjectAction>> createObjectActionsByType() {
        final Map<ActionType, List<ObjectAction>> map = Maps.newHashMap();
        for (final ActionType type : ActionType.values()) {
            map.put(type, Lists.<ObjectAction>newArrayList());
        }
        return map;
    }

    private boolean contributeeAssociationsAdded;
    private boolean contributeeActionsAdded;


    private final List<ObjectSpecification> interfaces = Lists.newArrayList();
    private final SubclassList subclasses = new SubclassList();

    private final Class<?> correspondingClass;
    private final String fullName;
    private final String shortName;
    private final Identifier identifier;
    private final boolean isAbstract;
    // derived lazily, cached since immutable
    private ObjectSpecId specId;

    private ObjectSpecification superclassSpec;

    private Persistability persistability = Persistability.USER_PERSISTABLE;

    private MarkDirtyObjectFacet markDirtyObjectFacet;
    private ClearDirtyObjectFacet clearDirtyObjectFacet;
    private IsDirtyObjectFacet isDirtyObjectFacet;

    private TitleFacet titleFacet;
    private IconFacet iconFacet;
    private CssClassFacet cssClassFacet;

    private IntrospectionState introspected = IntrospectionState.NOT_INTROSPECTED;

    // //////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////

    public ObjectSpecificationAbstract(
            final Class<?> introspectedClass, 
            final String shortName,
            final SpecificationContext specificationContext, 
            final ObjectMemberContext objectMemberContext) {

        this.correspondingClass = introspectedClass;
        this.fullName = introspectedClass.getName();
        this.shortName = shortName;
        
        this.isAbstract = ClassExtensions.isAbstract(introspectedClass);
        this.identifier = Identifier.classIdentifier(introspectedClass);

        this.deploymentCategory = specificationContext.getDeploymentCategory();
        this.authenticationSessionProvider = specificationContext.getAuthenticationSessionProvider();
        this.servicesProvider = specificationContext.getServicesProvider();
        this.objectInstantiator = specificationContext.getObjectInstantiator();
        this.specificationLookup = specificationContext.getSpecificationLookup();
        this.facetProcessor = specificationContext.getFacetProcessor();
        
        this.objectMemberContext = objectMemberContext;
    }

    
    protected DeploymentCategory getDeploymentCategory() {
        return deploymentCategory;
    }

    // //////////////////////////////////////////////////////////////////////
    // Stuff immediately derivable from class
    // //////////////////////////////////////////////////////////////////////

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
            if(facet != null) {
                specId = facet.value();
            }
        }
        return specId;
    }
    
    /**
     * As provided explicitly within the
     * {@link #ObjectSpecificationAbstract(Class, String, org.apache.isis.core.metamodel.spec.SpecificationContext, org.apache.isis.core.metamodel.spec.feature.ObjectMemberContext)}
     * constructor}.
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


    // //////////////////////////////////////////////////////////////////////
    // Introspection (part 1)
    // //////////////////////////////////////////////////////////////////////

    public abstract void introspectTypeHierarchyAndMembers();

    /**
     * Intended to be called within {@link #introspectTypeHierarchyAndMembers()}
     * .
     */
    protected void updateSuperclass(final Class<?> superclass) {
        if (superclass == null) {
            return;
        }
        superclassSpec = getSpecificationLookup().loadSpecification(superclass);
        if (superclassSpec != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  Superclass " + superclass.getName());
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
        this.subclasses.addSubclass(subclass);
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
                objectActionForType.addAll(Collections2.filter(objectActions, ObjectAction.Predicates.ofType(type)));
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Introspection (part 2)
    // //////////////////////////////////////////////////////////////////////

    public void updateFromFacetValues() {
        clearDirtyObjectFacet = getFacet(ClearDirtyObjectFacet.class);
        markDirtyObjectFacet = getFacet(MarkDirtyObjectFacet.class);
        isDirtyObjectFacet = getFacet(IsDirtyObjectFacet.class);

        titleFacet = getFacet(TitleFacet.class);
        iconFacet = getFacet(IconFacet.class);
        cssClassFacet = getFacet(CssClassFacet.class);

        this.persistability = determinePersistability();
    }

    private Persistability determinePersistability() {
        final NotPersistableFacet notPersistableFacet = getFacet(NotPersistableFacet.class);
        if (notPersistableFacet == null) {
            return Persistability.USER_PERSISTABLE;
        }
        final NotPersistable.By initiatedBy = notPersistableFacet.value();
        if (initiatedBy == NotPersistable.By.USER_OR_PROGRAM) {
            return Persistability.TRANSIENT;
        } else if (initiatedBy == NotPersistable.By.USER) {
            return Persistability.PROGRAM_PERSISTABLE;
        } else {
            return Persistability.USER_PERSISTABLE;
        }
    }

    /**
     * Intended to be called (if at all) within {@link #updateFromFacetValues()}
     * .
     */
    protected void setClearDirtyObjectFacet(final ClearDirtyObjectFacet clearDirtyObjectFacet) {
        this.clearDirtyObjectFacet = clearDirtyObjectFacet;
    }


    // //////////////////////////////////////////////////////////////////////
    // Title, Icon
    // //////////////////////////////////////////////////////////////////////

    @Override
    public String getTitle(final ObjectAdapter targetAdapter, final Localization localization) {
        return getTitle(null, targetAdapter, localization);
    }

    @Override
    public String getTitle(ObjectAdapter contextAdapterIfAny, ObjectAdapter targetAdapter, Localization localization) {
        if (titleFacet != null) {
            final String titleString = titleFacet.title(contextAdapterIfAny, targetAdapter, localization);
            if (titleString != null && !titleString.equals("")) {
                return titleString;
            }
        }
        return (this.isService() ? "" : "Untitled ") + getSingularName();
    }


    @Override
    public String getIconName(final ObjectAdapter reference) {
        return iconFacet == null ? null : iconFacet.iconName(reference);
    }

    @Deprecated
    @Override
    public String getCssClass() {
        return getCssClass(null);
    }

    @Override
    public String getCssClass(final ObjectAdapter reference) {
        return cssClassFacet == null ? null : cssClassFacet.cssClass(reference);
    }

    // //////////////////////////////////////////////////////////////////////
    // Specification
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Instance getInstance(final ObjectAdapter adapter) {
        return adapter;
    }

    // //////////////////////////////////////////////////////////////////////
    // Hierarchical
    // //////////////////////////////////////////////////////////////////////

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
        return superclassSpec != null ? superclassSpec.isOfType(specification) : false;
    }

    // //////////////////////////////////////////////////////////////////////
    // Name, Description, Persistability
    // //////////////////////////////////////////////////////////////////////

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

    @Override
    public Persistability persistability() {
        return persistability;
    }

    // //////////////////////////////////////////////////////////////////////
    // Dirty object support
    // //////////////////////////////////////////////////////////////////////

    @Override
    public boolean isDirty(final ObjectAdapter object) {
        return isDirtyObjectFacet == null ? false : isDirtyObjectFacet.invoke(object);
    }

    @Override
    public void clearDirty(final ObjectAdapter object) {
        if (clearDirtyObjectFacet != null) {
            clearDirtyObjectFacet.invoke(object);
        }
    }

    @Override
    public void markDirty(final ObjectAdapter object) {
        if (markDirtyObjectFacet != null) {
            markDirtyObjectFacet.invoke(object);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Facet Handling
    // //////////////////////////////////////////////////////////////////////

    @Override
    public <Q extends Facet> Q getFacet(final Class<Q> facetType) {
        final Q facet = super.getFacet(facetType);
        Q noopFacet = null;
        if (isNotANoopFacet(facet)) {
            return facet;
        } else {
            noopFacet = facet;
        }
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
                } else {
                    if (noopFacet == null) {
                        noopFacet = interfaceFacet;
                    }
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
        }
        return noopFacet;
    }

    private boolean isNotANoopFacet(final Facet facet) {
        return facet != null && !facet.isNoop();
    }

    // //////////////////////////////////////////////////////////////////////
    // DefaultValue
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Object getDefaultValue() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////////
    // Identifier
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    // //////////////////////////////////////////////////////////////////
    // create InteractionContext
    // //////////////////////////////////////////////////////////////////

    @Override
    public ObjectTitleContext createTitleInteractionContext(final AuthenticationSession session, final InteractionInvocationMethod interactionMethod, final ObjectAdapter targetObjectAdapter) {
        return new ObjectTitleContext(getDeploymentCategory(), session, interactionMethod, targetObjectAdapter, getIdentifier(), targetObjectAdapter.titleString());
    }

    // //////////////////////////////////////////////////////////////////////
    // Superclass, Interfaces, Subclasses, isAbstract
    // //////////////////////////////////////////////////////////////////////

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
        return subclasses.toList();
    }

    @Override
    public boolean hasSubclasses() {
        return subclasses.hasSubclasses();
    }

    @Override
    public final boolean isAbstract() {
        return isAbstract;
    }

    // //////////////////////////////////////////////////////////////////////
    // Associations
    // //////////////////////////////////////////////////////////////////////

    @Override
    public List<ObjectAssociation> getAssociations(final Contributed contributed) {
        // the "contributed.isIncluded()" guard is required because we cannot do this too early;
        // there must be a session available
        if(contributed.isIncluded() && !contributeeAssociationsAdded) {
            synchronized (this.associations) {
                List<ObjectAssociation> associations = Lists.newArrayList(this.associations);
                associations.addAll(createContributeeAssociations());
                sortAndUpdateAssociations(associations);
                contributeeAssociationsAdded = true;
            }
        }
        final List<ObjectAssociation> associations = Lists.newArrayList(this.associations);
        return Lists.newArrayList(Iterables.filter(
                associations, ContributeeMember.Predicates.regularElse(contributed)));
    }


    private static ThreadLocal<Boolean> invalidatingCache = new ThreadLocal<Boolean>() {
        protected Boolean initialValue() {
            return Boolean.FALSE;
        };
    };

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
        if(!getDeploymentCategory().isProduction()) {
            // automatically refresh if not in production
            // (better support for jrebel)
            
            LOG.warn("Could not find association with id '" + id + "'; invalidating cache automatically");
            if(!invalidatingCache.get()) {
                // make sure don't go into an infinite loop, though.
                try {
                    invalidatingCache.set(true);
                    getSpecificationLookup().invalidateCache(getCorrespondingClass());
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
        throw new ObjectSpecificationException("No association called '" + id + "' in '" + getSingularName() + "'");
    }

    private ObjectAssociation getAssociationWithId(final String id) {
        for (final ObjectAssociation objectAssociation : getAssociations(Contributed.INCLUDED)) {
            if (objectAssociation.getId().equals(id)) {
                return objectAssociation;
            }
        }
        return null;
    }

    @Deprecated
    @Override
    public List<ObjectAssociation> getAssociations(Filter<ObjectAssociation> filter) {
        return getAssociations(Contributed.INCLUDED, filter);
    }

    @Override
    public List<ObjectAssociation> getAssociations(Contributed contributed, final Filter<ObjectAssociation> filter) {
        final List<ObjectAssociation> allAssociations = getAssociations(contributed);
        return Lists.newArrayList(
                Iterables.filter(allAssociations, Filters.asPredicate(filter)));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public List<OneToOneAssociation> getProperties(Contributed contributed) {
        final List list = getAssociations(contributed, ObjectAssociation.Filters.PROPERTIES);
        return list;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<OneToManyAssociation> getCollections(Contributed contributed) {
        final List list = getAssociations(contributed, ObjectAssociation.Filters.COLLECTIONS);
        return list;
    }

    // //////////////////////////////////////////////////////////////////////
    // getObjectActions
    // //////////////////////////////////////////////////////////////////////

    @Override
    public List<ObjectAction> getObjectActions(
            final List<ActionType> types,
            final Contributed contributed, 
            final Filter<ObjectAction> filter) {

        // update our list of actions if requesting for contributed actions
        // and they have not yet been added
        // the "contributed.isIncluded()" guard is required because we cannot do this too early;
        // there must be a session available
        if(contributed.isIncluded() && !contributeeActionsAdded) {
            synchronized (this.objectActions) {
                final List<ObjectAction> actions = Lists.newArrayList(this.objectActions);
                actions.addAll(createContributeeActions());
                sortCacheAndUpdateActions(actions);
                contributeeActionsAdded = true;
            }
        }

        final List<ObjectAction> actions = Lists.newArrayList();
        for (final ActionType type : types) {
            final Collection<ObjectAction> filterActions =
                    Collections2.filter(objectActionsByType.get(type), Filters.asPredicate(filter));
            actions.addAll(filterActions);
        }
        return Lists.newArrayList(
                Iterables.filter(
                        actions,
                        ContributeeMember.Predicates.regularElse(contributed)));
    }

    @Override
    public List<ObjectAction> getObjectActions(
            final Contributed contributed) {
        return getObjectActions(ActionType.ALL, contributed, Filters.<ObjectAction>any());
    }

    @Override
    public List<ObjectAction> getObjectActions(
            final ActionType type, 
            final Contributed contributed, 
            final Filter<ObjectAction> filter) {
        return getObjectActions(Collections.singletonList(type), contributed, filter);
    }

    // //////////////////////////////////////////////////////////////////////
    // sorting
    // //////////////////////////////////////////////////////////////////////
    
    protected List<ObjectAssociation> sortAssociations(final List<ObjectAssociation> associations) {
        final DeweyOrderSet orderSet = DeweyOrderSet.createOrderSet(associations);
        final MemberGroupLayoutFacet memberGroupLayoutFacet = this.getFacet(MemberGroupLayoutFacet.class);
        
        if(memberGroupLayoutFacet != null) {
            final List<String> groupOrder = Lists.newArrayList();
            groupOrder.addAll(memberGroupLayoutFacet.getLeft());
            groupOrder.addAll(memberGroupLayoutFacet.getMiddle());
            groupOrder.addAll(memberGroupLayoutFacet.getRight());
            
            orderSet.reorderChildren(groupOrder);
        }
        final List<ObjectAssociation> orderedAssociations = Lists.newArrayList();
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
        final List<ObjectAction> orderedActions = Lists.newArrayList();
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
                final List<ObjectAction> actions = Lists.newArrayList();
                sortActions(set, actions);
                actionsToAppendTo.addAll(actions);
            } else {
                throw new UnknownTypeException(element);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // getServiceActionsReturning
    // //////////////////////////////////////////////////////////////////////

    @Override
    public List<ObjectAction> getServiceActionsReturning(final List<ActionType> types) {
        final List<ObjectAction> serviceActions = Lists.newArrayList();
        final List<ObjectAdapter> services = getServicesProvider().getServices();
        for (final ObjectAdapter serviceAdapter : services) {
            appendServiceActionsReturning(serviceAdapter, types, serviceActions);
        }
        return serviceActions;
    }

    private void appendServiceActionsReturning(final ObjectAdapter serviceAdapter, final List<ActionType> types, final List<ObjectAction> relatedActionsToAppendTo) {
        final List<ObjectAction> matchingActionsToAppendTo = Lists.newArrayList();
        for (final ActionType type : types) {
            final List<ObjectAction> serviceActions = serviceAdapter.getSpecification().getObjectActions(type, Contributed.INCLUDED, Filters.<ObjectAction>any());
            for (final ObjectAction serviceAction : serviceActions) {
                addIfReturnsSubtype(serviceAction, matchingActionsToAppendTo);
            }
        }
        relatedActionsToAppendTo.addAll(matchingActionsToAppendTo);
    }

    private void addIfReturnsSubtype(final ObjectAction serviceAction, final List<ObjectAction> matchingActionsToAppendTo) {
        final ObjectSpecification returnType = serviceAction.getReturnType();
        if (returnType == null) {
            return;
        }
        if (returnType.isParentedOrFreeCollection()) {
            final TypeOfFacet facet = serviceAction.getFacet(TypeOfFacet.class);
            if (facet != null) {
                final ObjectSpecification elementType = facet.valueSpec();
                addIfReturnsSubtype(serviceAction, elementType, matchingActionsToAppendTo);
            }
        } else {
            addIfReturnsSubtype(serviceAction, returnType, matchingActionsToAppendTo);
        }
    }

    private void addIfReturnsSubtype(final ObjectAction serviceAction, final ObjectSpecification actionType, final List<ObjectAction> matchingActionsToAppendTo) {
        if (actionType.isOfType(this)) {
            matchingActionsToAppendTo.add(serviceAction);
        }
    }

    
    // //////////////////////////////////////////////////////////////////////
    // contributee associations (properties and collections)
    // //////////////////////////////////////////////////////////////////////

    private List<ObjectAssociation> createContributeeAssociations() {
        if (isService() || isValue()) {
            return Collections.emptyList();
        }
        
        final List<ObjectAssociation> contributeeAssociations = Lists.newArrayList();
        final List<ObjectAdapter> services = getServicesProvider().getServices();
        for (final ObjectAdapter serviceAdapter : services) {
            addContributeeAssociationsIfAny(serviceAdapter, contributeeAssociations);
        }
        return contributeeAssociations;
    }

    private void addContributeeAssociationsIfAny(
            final ObjectAdapter serviceAdapter, 
            final List<ObjectAssociation> contributeeAssociationsToAppendTo) {
        final ObjectSpecification specification = serviceAdapter.getSpecification();
        if (specification == this) {
            return;
        }
        final List<ObjectAssociation> contributeeAssociations = createContributeeAssociations(serviceAdapter);
        contributeeAssociationsToAppendTo.addAll(contributeeAssociations);
    }

    /**
     * Synthesises {@link ObjectAssociation}s from matching {@link ObjectAction}s of any of the services
     * that accept one parameter
     */
    private List<ObjectAssociation> createContributeeAssociations(final ObjectAdapter serviceAdapter) {
        
        final ObjectSpecification specification = serviceAdapter.getSpecification();
        final List<ObjectAction> serviceActions = specification.getObjectActions(ActionType.USER, Contributed.INCLUDED, Filters.<ObjectAction>any());
        
        final List<ObjectActionImpl> contributedActions = Lists.newArrayList();
        for (final ObjectAction serviceAction : serviceActions) {
            if (isAlwaysHidden(serviceAction)) {
                continue;
            }
            final NotContributedFacet notContributed = serviceAction.getFacet(NotContributedFacet.class);
            if(notContributed != null && notContributed.toAssociations()) {
                continue;
            }
            if(!serviceAction.hasReturn()) {
                continue;
            }
            if (serviceAction.getParameterCount() != 1 || contributeeParameterMatchOf(serviceAction) == -1) {
                continue;
            }
            if(!(serviceAction instanceof ObjectActionImpl)) {
                continue;
            }
            if(!serviceAction.getSemantics().isSafe()) {
                continue;
            }
            contributedActions.add((ObjectActionImpl) serviceAction);
        }
        
        return Lists.newArrayList(Iterables.transform(contributedActions, createContributeeAssociationFunctor(serviceAdapter, this)));
    }


    private Function<ObjectActionImpl, ObjectAssociation> createContributeeAssociationFunctor(
            final ObjectAdapter serviceAdapter, final ObjectSpecification contributeeType) {
        return new Function<ObjectActionImpl, ObjectAssociation>(){
            @Override
            public ObjectAssociation apply(ObjectActionImpl input) {
                final ObjectSpecification returnType = input.getReturnType();
                final ObjectAssociationAbstract association = returnType.isNotCollection() 
                        ? new OneToOneAssociationContributee(serviceAdapter, input, contributeeType, objectMemberContext) 
                        : new OneToManyAssociationContributee(serviceAdapter, input, contributeeType, objectMemberContext);
                facetProcessor.processMemberOrder(metadataProperties, association);
                return association;
            }
        };
    }


    // //////////////////////////////////////////////////////////////////////
    // contributee actions
    // //////////////////////////////////////////////////////////////////////

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
        final List<ObjectAction> contributeeActions = Lists.newArrayList();
            
        final List<ObjectAdapter> services = getServicesProvider().getServices();
        for (final ObjectAdapter serviceAdapter : services) {
            addContributeeActionsIfAny(serviceAdapter, contributeeActions);
        }
        return contributeeActions;
    }

    private void addContributeeActionsIfAny(
            final ObjectAdapter serviceAdapter, 
            final List<ObjectAction> contributeeActionsToAppendTo) {
        final ObjectSpecification specification = serviceAdapter.getSpecification();
        if (specification == this) {
            return;
        }
        final List<ObjectAction> contributeeActions = Lists.newArrayList();
        final List<ObjectAction> serviceActions = specification.getObjectActions(ActionType.ALL, Contributed.INCLUDED, Filters.<ObjectAction>any());
        for (final ObjectAction serviceAction : serviceActions) {
            if (isAlwaysHidden(serviceAction)) {
                continue;
            }
            final NotContributedFacet notContributed = serviceAction.getFacet(NotContributedFacet.class);
            if(notContributed != null && notContributed.toActions()) {
                continue;
            }
            if(!(serviceAction instanceof ObjectActionImpl)) {
                continue;
            }
            final ObjectActionImpl contributedAction = (ObjectActionImpl) serviceAction;
        
            // see if qualifies by inspecting all parameters
            final int contributeeParam = contributeeParameterMatchOf(contributedAction);
            if (contributeeParam != -1) {
                ObjectActionContributee contributeeAction = 
                        new ObjectActionContributee(serviceAdapter, contributedAction, contributeeParam, this, objectMemberContext);
                facetProcessor.processMemberOrder(metadataProperties, contributeeAction);
                contributeeActions.add(contributeeAction);
            }
        }
        contributeeActionsToAppendTo.addAll(contributeeActions);
    }
    
    private boolean isAlwaysHidden(final FacetHolder holder) {
        final HiddenFacet hiddenFacet = holder.getFacet(HiddenFacet.class);
        return hiddenFacet != null && hiddenFacet.when() == When.ALWAYS && hiddenFacet.where() == Where.ANYWHERE;
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

    // //////////////////////////////////////////////////////////////////////
    // validity
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Consent isValid(final ObjectAdapter inObject) {
        return isValidResult(inObject).createConsent();
    }

    /**
     * TODO: currently this method is hard-coded to assume all interactions are
     * initiated {@link InteractionInvocationMethod#BY_USER by user}.
     */
    @Override
    public InteractionResult isValidResult(final ObjectAdapter targetObjectAdapter) {
        final ObjectValidityContext validityContext = createValidityInteractionContext(deploymentCategory, getAuthenticationSession(), InteractionInvocationMethod.BY_USER, targetObjectAdapter);
        return InteractionUtils.isValidResult(this, validityContext);
    }

    /**
     * Create an {@link InteractionContext} representing an attempt to save the
     * object.
     */
    @Override
    public ObjectValidityContext createValidityInteractionContext(DeploymentCategory deploymentCategory, final AuthenticationSession session, final InteractionInvocationMethod interactionMethod, final ObjectAdapter targetObjectAdapter) {
        return new ObjectValidityContext(deploymentCategory, session, interactionMethod, targetObjectAdapter, getIdentifier());
    }

    // //////////////////////////////////////////////////////////////////////
    // convenience isXxx (looked up from facets)
    // //////////////////////////////////////////////////////////////////////

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
        return containsFacet(ParentedFacet.class);
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

    // //////////////////////////////////////////////////////////////////////
    // misc
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Object createObject() {
        throw new UnsupportedOperationException(getFullIdentifier());
    }

    @Override
    public ObjectAdapter initialize(ObjectAdapter objectAdapter) {
        return objectAdapter;
    }

    // //////////////////////////////////////////////////////////////////////
    // toString
    // //////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullIdentifier());
        return str.toString();
    }

    // //////////////////////////////////////////////////////////////////////
    // Convenience
    // //////////////////////////////////////////////////////////////////////

    /**
     * convenience method to return the current {@link AuthenticationSession}
     * from the {@link #getAuthenticationSessionProvider() injected}
     * {@link AuthenticationSessionProvider}.
     */
    protected final AuthenticationSession getAuthenticationSession() {
        return getAuthenticationSessionProvider().getAuthenticationSession();
    }

    // //////////////////////////////////////////////////////////////////////
    // Dependencies (injected in constructor)
    // //////////////////////////////////////////////////////////////////////

    protected AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    public ServicesProvider getServicesProvider() {
        return servicesProvider;
    }

    public ObjectInstantiator getObjectInstantiator() {
        return objectInstantiator;
    }

    public SpecificationLoader getSpecificationLookup() {
        return specificationLookup;
    }


}
