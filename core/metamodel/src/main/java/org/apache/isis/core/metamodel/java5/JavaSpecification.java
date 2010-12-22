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

package org.apache.isis.core.metamodel.java5;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.profiles.Perspective;
import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.filters.AbstractFilter;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.commons.lang.ListUtils;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.AdapterMap;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.QuerySubmitter;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.exceptions.ReflectionException;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.ImperativeFacetUtils;
import org.apache.isis.core.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.naming.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.facets.object.callbacks.CallbackUtils;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.facets.object.dirty.ClearDirtyObjectFacet;
import org.apache.isis.core.metamodel.facets.object.dirty.IsDirtyObjectFacet;
import org.apache.isis.core.metamodel.facets.object.dirty.MarkDirtyObjectFacet;
import org.apache.isis.core.metamodel.facets.object.ident.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.ident.plural.PluralFacet;
import org.apache.isis.core.metamodel.facets.object.ident.plural.PluralFacetInferred;
import org.apache.isis.core.metamodel.facets.object.ident.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.notpersistable.InitiatedBy;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.metamodel.facets.ordering.OrderSet;
import org.apache.isis.core.metamodel.peer.FacetedMethod;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;
import org.apache.isis.core.metamodel.spec.IntrospectableSpecificationAbstract;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;
import org.apache.isis.core.metamodel.spec.ObjectInstantiator;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorDefault;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.internal.ObjectActionImpl;
import org.apache.isis.core.metamodel.specloader.internal.OneToManyAssociationImpl;
import org.apache.isis.core.metamodel.specloader.internal.OneToOneAssociationImpl;
import org.apache.isis.core.metamodel.util.NameUtils;

public class JavaSpecification extends IntrospectableSpecificationAbstract implements DebugInfo, FacetHolder {

    private final static Logger LOG = Logger.getLogger(JavaSpecification.class);

    private static String determineShortName(final Class<?> introspectedClass) {
        String name = introspectedClass.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    // //////////////////////////////////////////////////////////////
    // fields
    // //////////////////////////////////////////////////////////////

    private final ObjectReflectorDefault reflector;
    private final AdapterMap adapterMap;
    private final DependencyInjector dependencyInjector;
    private final QuerySubmitter querySubmitter;

    private boolean isService;

    private IconFacet iconFacet;
    private TitleFacet titleFacet;

    /**
     * Lazily built by {@link #getMember(Method)}.
     */
    private Map<Method, ObjectMember> membersByMethod = null;

    private JavaIntrospector introspector;

    // //////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////

    public JavaSpecification(final Class<?> cls, final ObjectReflectorDefault reflector,
        final AuthenticationSessionProvider authenticationSessionProvider, final ServicesProvider servicesProvider,
        final AdapterMap adapterManager, final ObjectInstantiator objectInstantiator,
        final DependencyInjector dependencyInjector, final QuerySubmitter querySubmitter) {
        super(cls, determineShortName(cls), authenticationSessionProvider, servicesProvider, objectInstantiator,
            reflector);
        this.reflector = reflector;
        this.introspector = new JavaIntrospector(cls, this, reflector);

        this.adapterMap = adapterManager;
        this.dependencyInjector = dependencyInjector;
        this.querySubmitter = querySubmitter;
    }

    // //////////////////////////////////////////////////////////////////////
    // introspect (part 1)
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void introspectTypeHierarchyAndMembers() {
        if (introspector == null) {
            throw new ReflectionException("Introspection already taken place, cannot introspect again");
        }

        // class
        introspector.introspectClass();

        // names
        addNamedFacetAndPluralFacetIfRequired();

        // superclass
        final Class<?> superclass = getIntrospectedClass().getSuperclass();
        setSuperclass(superclass);

        // go no further if required
        final boolean skipFurtherIntrospection =
            JavaClassUtils.isJavaClass(getIntrospectedClass()) || isAppLibValue(getIntrospectedClass());
        if (skipFurtherIntrospection) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("skipping introspection of interfaces, properties, actions and interfaces for "
                    + getFullName() + " (java.xxx or applib value class)");
            }
            return;
        }

        // walk superinterfaces

        // REVIEW: the processing here isn't quite the same as with superclasses,
        // in that with superclasses the superclass adds this type as its subclass,
        // whereas here this type defines itself as the subtype.
        // it'd be nice to push the responsibility for adding subclasses to
        // the interface type... needs some tests around it, though, before
        // making that refactoring.
        final Class<?>[] interfaceTypes = getIntrospectedClass().getInterfaces();
        List<ObjectSpecification> interfaceSpecList = Lists.newArrayList();
        for (Class<?> interfaceType : interfaceTypes) {
            Class<?> substitutedInterfaceType = getClassSubstitutor().getClass(interfaceType);
            if (substitutedInterfaceType != null) {
                ObjectSpecification interfaceSpec =
                    getSpecificationLookup().loadSpecification(substitutedInterfaceType);
                interfaceSpecList.add(interfaceSpec);
            }
        }
        addAsSubclassTo(interfaceSpecList);
        addInterfaces(interfaceSpecList);

        // properties and collections
        introspector.introspectAssociations();
        final OrderSet associationOrderSet = introspector.getAssociationOrderSet();
        if (associationOrderSet != null) {
            addAssociations(asAssociations(associationOrderSet));
        }

        // actions
        introspector.introspectActions();
        OrderSet actionOrderSet = introspector.getActionOrderSet();
        addObjectActions(asObjectActions(actionOrderSet));
    }

    private void addNamedFacetAndPluralFacetIfRequired() {
        NamedFacet namedFacet = getFacet(NamedFacet.class);
        if (namedFacet == null) {
            namedFacet = new NamedFacetInferred(NameUtils.naturalName(getShortName()), this);
            addFacet(namedFacet);
        }

        PluralFacet pluralFacet = getFacet(PluralFacet.class);
        if (pluralFacet == null) {
            pluralFacet = new PluralFacetInferred(NameUtils.pluralName(namedFacet.value()), this);
            addFacet(pluralFacet);
        }
    }

    /**
     * TODO: review this, should be more general and check for value facet, surely?
     */
    private boolean isAppLibValue(Class<?> type) {
        return type.getName().startsWith("org.apache.isis.applib.value.");
    }

    private List<ObjectAssociation> asAssociations(final OrderSet orderSet) {
        final List<ObjectAssociation> associations = Lists.newArrayList();
        for (Object element : orderSet) {
            if (element instanceof FacetedMethod) {
                FacetedMethod facetMethod = (FacetedMethod) element;
                if (facetMethod.getFeatureType().isCollection()) {
                    associations.add(createCollection(facetMethod));
                } else if (facetMethod.getFeatureType().isProperty()) {
                    associations.add(createProperty(facetMethod));
                }
            } else if (element instanceof OrderSet) {
                // Not supported at present
            } else {
                throw new UnknownTypeException(element);
            }
        }

        return associations;
    }

    private List<ObjectAction> asObjectActions(final OrderSet orderSet) {
        final List<ObjectAction> actions = Lists.newArrayList();
        for (Object element : orderSet) {
            if (element instanceof FacetedMethod) {
                final FacetedMethod facetedMethod = (FacetedMethod) element;
                if (facetedMethod.getFeatureType().isAction()) {
                    actions.add(createAction(facetedMethod));
                }
            } else if (element instanceof OrderSet) {
                final OrderSet set = ((OrderSet) element);
                actions.add(createObjectActionSet(set));
            } else {
                throw new UnknownTypeException(element);
            }
        }
        return actions;
    }

    private OneToOneAssociationImpl createProperty(final FacetedMethod facetedMethod) {
        return new OneToOneAssociationImpl(facetedMethod, getAuthenticationSessionProvider(), getSpecificationLookup(),
            getAdapterMap(), getQuerySubmitter());
    }

    private OneToManyAssociationImpl createCollection(final FacetedMethod facetedMethod) {
        return new OneToManyAssociationImpl(facetedMethod, getAuthenticationSessionProvider(), getSpecificationLookup(),
            getAdapterMap(), getQuerySubmitter());
    }

    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        return new ObjectActionImpl(facetedMethod, getAuthenticationSessionProvider(), getSpecificationLookup(),
            getAdapterMap(), getServicesProvider(), getQuerySubmitter());
    }

    private ObjectActionSet createObjectActionSet(final OrderSet set) {
        return new ObjectActionSet("", set.getGroupFullName(), asObjectActions(set));
    }

    // //////////////////////////////////////////////////////////////////////
    // introspect (part 2)
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void completeIntrospection() {
        setClearDirtyObjectFacet(getFacet(ClearDirtyObjectFacet.class));
        setMarkDirtyObjectFacet(getFacet(MarkDirtyObjectFacet.class));
        setIsDirtyObjectFacet(getFacet(IsDirtyObjectFacet.class));

        iconFacet = getFacet(IconFacet.class);

        NamedFacet namedFacet = getFacet(NamedFacet.class);
        setSingularName(namedFacet.value());

        PluralFacet pluralFacet = getFacet(PluralFacet.class);
        setPluralName(pluralFacet.value());

        final DescribedAsFacet describedAsFacet = getFacet(DescribedAsFacet.class);
        setDescribedAs(describedAsFacet.value());

        final NotPersistableFacet notPersistableFacet = getFacet(NotPersistableFacet.class);
        final InitiatedBy initiatedBy = notPersistableFacet.value();
        Persistability persistability;
        if (initiatedBy == InitiatedBy.USER_OR_PROGRAM) {
            persistability = Persistability.TRANSIENT;
        } else if (initiatedBy == InitiatedBy.USER) {
            persistability = Persistability.PROGRAM_PERSISTABLE;
        } else {
            persistability = Persistability.USER_PERSISTABLE;
        }
        setPersistability(persistability);

        // indicates have now been introspected.
        introspector = null;
        setIntrospected(true);
    }

    /**
     * Added to try to track down a race condition.
     */
    @Override
    public boolean isIntrospected() {
        return introspector == null;
    }

    // //////////////////////////////////////////////////////////////////////
    // Title and Icon
    // //////////////////////////////////////////////////////////////////////

    @Override
    public String getTitle(final ObjectAdapter object) {
        if (titleFacet == null) {
            titleFacet = getFacet(TitleFacet.class);
        }
        if (titleFacet != null) {
            final String titleString = titleFacet.title(object);
            if (titleString != null && !titleString.equals("")) {
                return titleString;
            }
        }
        return (this.isService() ? "" : "Untitled ") + getName();
    }

    @Override
    public String getIconName(final ObjectAdapter reference) {
        return iconFacet == null ? null : iconFacet.iconName(reference);
    }

    // //////////////////////////////////////////////////////////////////////
    // Whether a service or not
    // //////////////////////////////////////////////////////////////////////

    @Override
    public boolean isService() {
        return isService;
    }

    /**
     * TODO: should ensure that service has at least one user action; fix when specification knows of its hidden
     * methods.
     * 
     * <pre>
     * if (objectActions != null &amp;&amp; objectActions.length == 0) {
     *     throw new ObjectSpecificationException(&quot;Service object &quot; + getFullName() + &quot; should have at least one user action&quot;);
     * }
     * </pre>
     */
    @Override
    public void markAsService() {
        ensureServiceHasNoAssociations();
        isService = true;
    }

    private void ensureServiceHasNoAssociations() {
        final List<ObjectAssociation> associations = getAssociations();
        StringBuilder buf = new StringBuilder();
        for (ObjectAssociation association : associations) {
            final String name = association.getId();
            // services are allowed to have one association, called 'id'
            if (!isValidAssociationForService(name)) {
                appendAssociationName(buf, name);
            }
        }
        if (buf.length() > 0) {
            throw new ObjectSpecificationException("Service object " + getFullName()
                + " should have no fields, but has: " + buf);
        }
    }

    /**
     * Services are allowed to have one association, called 'id'.
     * 
     * <p>
     * This is used for {@link Perspective}s (user profiles).
     */
    private boolean isValidAssociationForService(final String associationId) {
        return "id".indexOf(associationId) != -1;
    }

    private void appendAssociationName(StringBuilder fieldNames, final String name) {
        fieldNames.append(fieldNames.length() > 0 ? ", " : "");
        fieldNames.append(name);
    }

    // //////////////////////////////////////////////////////////////////////
    // Actions
    // //////////////////////////////////////////////////////////////////////

    @Override
    public ObjectAction getObjectAction(final ObjectActionType type, final String id,
        final List<ObjectSpecification> parameters) {
        final List<ObjectAction> availableActions =
            ListUtils.combine(getObjectActionsAll(), getContributedActions(type));
        return getAction(availableActions, type, id, parameters);
    }

    @Override
    public ObjectAction getObjectAction(final ObjectActionType type, final String nameParmsIdentityString) {
        final List<ObjectAction> availableActions =
            ListUtils.combine(getObjectActionsAll(), getContributedActions(type));
        return getAction(availableActions, type, nameParmsIdentityString);
    }

    private ObjectAction getAction(final List<ObjectAction> availableActions, final ObjectActionType type,
        final String actionName, final List<ObjectSpecification> parameters) {
        outer: for (int i = 0; i < availableActions.size(); i++) {
            final ObjectAction action = availableActions.get(i);
            if (action.getActions().size() > 0) {
                // deal with action set
                final ObjectAction a = getAction(action.getActions(), type, actionName, parameters);
                if (a != null) {
                    return a;
                }
            } else {
                // regular action
                if (!action.getType().equals(type)) {
                    continue outer;
                }
                if (actionName != null && !actionName.equals(action.getId())) {
                    continue outer;
                }
                if (action.getParameters().size() != parameters.size()) {
                    continue outer;
                }
                for (int j = 0; j < parameters.size(); j++) {
                    if (!parameters.get(j).isOfType(action.getParameters().get(j).getSpecification())) {
                        continue outer;
                    }
                }
                return action;
            }
        }
        return null;
    }

    private ObjectAction getAction(final List<ObjectAction> availableActions, final ObjectActionType type,
        final String nameParmsIdentityString) {
        if (nameParmsIdentityString == null) {
            return null;
        }
        outer: for (int i = 0; i < availableActions.size(); i++) {
            final ObjectAction action = availableActions.get(i);
            if (action.getActions().size() > 0) {
                // deal with action set
                final ObjectAction a = getAction(action.getActions(), type, nameParmsIdentityString);
                if (a != null) {
                    return a;
                }
            } else {
                // regular action
                if (!sameActionTypeOrNotSpecified(type, action)) {
                    continue outer;
                }
                if (!nameParmsIdentityString.equals(action.getIdentifier().toNameParmsIdentityString())) {
                    continue outer;
                }
                return action;
            }
        }
        return null;
    }

    @Override
    public boolean isCollectionOrIsAggregated() {
        return isCollection() || isValueOrIsAggregated();
    }

    // //////////////////////////////////////////////////////////////////////
    // createObject
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Object createObject(CreationMode creationMode) {
        if (getIntrospectedClass().isArray()) {
            return Array.newInstance(getIntrospectedClass().getComponentType(), 0);
        }

        try {
            Object object = getObjectInstantiator().instantiate(getIntrospectedClass());

            if (creationMode == CreationMode.INITIALIZE) {
                final ObjectAdapter adapter = getAdapterMap().adapterFor(object);

                // initialize new object
                final List<ObjectAssociation> fields = adapter.getSpecification().getAssociations();
                for (int i = 0; i < fields.size(); i++) {
                    fields.get(i).toDefault(adapter);
                }
                getDependencyInjector().injectDependenciesInto(object);

                CallbackUtils.callCallback(adapter, CreatedCallbackFacet.class);
            }
            return object;
        } catch (final ObjectInstantiationException e) {
            throw new IsisException("Failed to create instance of type " + getFullName(), e);
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // getMember, catalog... (not API)
    // //////////////////////////////////////////////////////////////////////

    public ObjectMember getMember(final Method method) {
        if (membersByMethod == null) {
            final HashMap<Method, ObjectMember> membersByMethod = Maps.newHashMap();
            cataloguePropertiesAndCollections(membersByMethod);
            catalogueActions(membersByMethod);
            this.membersByMethod = membersByMethod;
        }
        return membersByMethod.get(method);
    }

    private void cataloguePropertiesAndCollections(final Map<Method, ObjectMember> membersByMethod) {
        Filter noop = AbstractFilter.noop(ObjectAssociation.class);
        final List<ObjectAssociation> fields = getAssociations(noop);
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final Facet[] facets = field.getFacets(ImperativeFacet.FILTER);
            for (int j = 0; j < facets.length; j++) {
                final ImperativeFacet facet = ImperativeFacetUtils.getImperativeFacet(facets[j]);
                for (Method imperativeFacetMethod : facet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, field);
                }
            }
        }
    }

    private void catalogueActions(final Map<Method, ObjectMember> membersByMethod) {
        final List<ObjectAction> userActions = getObjectActions(ObjectActionType.USER);
        for (int i = 0; i < userActions.size(); i++) {
            final ObjectAction userAction = userActions.get(i);
            final Facet[] facets = userAction.getFacets(ImperativeFacet.FILTER);
            for (int j = 0; j < facets.length; j++) {
                final ImperativeFacet facet = ImperativeFacetUtils.getImperativeFacet(facets[j]);
                for (Method imperativeFacetMethod : facet.getMethods()) {
                    membersByMethod.put(imperativeFacetMethod, userAction);
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // Debug, toString
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugString debug) {
        debug.blankLine();
        debug.appendln("Title", getFacet(TitleFacet.class));
        if (iconFacet != null) {
            debug.appendln("Icon", iconFacet);
        }
        debug.unindent();
    }

    @Override
    public String debugTitle() {
        return "NO Member Specification";
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this);
        str.append("class", getFullName());
        str.append("type", (isCollection() ? "Collection" : "Object"));
        str.append("persistable", persistability());
        str.append("superclass", superclass() == null ? "Object" : superclass().getFullName());
        return str.toString();
    }

    // //////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // //////////////////////////////////////////////////////////////////

    /**
     * Derived from {@link #getReflector()}
     */
    private ClassSubstitutor getClassSubstitutor() {
        return reflector.getClassSubstitutor();
    }

    protected AdapterMap getAdapterMap() {
        return adapterMap;
    }

    protected DependencyInjector getDependencyInjector() {
        return dependencyInjector;
    }

    protected QuerySubmitter getQuerySubmitter() {
        return querySubmitter;
    }

}
