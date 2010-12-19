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


package org.apache.isis.core.progmodel.spec;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugInfo;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.commons.filters.AbstractFilter;
import org.apache.isis.core.commons.filters.Filter;
import org.apache.isis.core.commons.lang.ArrayUtils;
import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.exceptions.ReflectionException;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorSet;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.core.metamodel.java5.ImperativeFacet;
import org.apache.isis.core.metamodel.java5.ImperativeFacetUtils;
import org.apache.isis.core.metamodel.runtimecontext.ObjectInstantiationException;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.spec.IntrospectableSpecificationAbstract;
import org.apache.isis.core.metamodel.runtimecontext.spec.feature.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.internal.ObjectActionImpl;
import org.apache.isis.core.metamodel.specloader.internal.OneToManyAssociationImpl;
import org.apache.isis.core.metamodel.specloader.internal.OneToOneAssociationImpl;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeerImpl;
import org.apache.isis.core.metamodel.util.CallbackUtils;
import org.apache.isis.core.metamodel.util.NameUtils;
import org.apache.isis.core.metamodel.util.SpecUtils;
import org.apache.isis.core.progmodel.facets.naming.named.NamedFacetInferred;
import org.apache.isis.core.progmodel.facets.object.dirty.ClearDirtyObjectFacet;
import org.apache.isis.core.progmodel.facets.object.dirty.IsDirtyObjectFacet;
import org.apache.isis.core.progmodel.facets.object.dirty.MarkDirtyObjectFacet;
import org.apache.isis.core.progmodel.facets.object.ident.icon.IconFacet;
import org.apache.isis.core.progmodel.facets.object.ident.plural.PluralFacet;
import org.apache.isis.core.progmodel.facets.object.ident.plural.PluralFacetInferred;
import org.apache.isis.core.progmodel.facets.object.ident.title.TitleFacet;
import org.apache.isis.core.progmodel.facets.object.notpersistable.InitiatedBy;
import org.apache.isis.core.progmodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.progmodel.facets.ordering.OrderSet;
import org.apache.isis.core.progmodel.specloader.internal.introspector.JavaIntrospector;


public class JavaSpecification extends IntrospectableSpecificationAbstract implements DebugInfo, FacetHolder {

    private final static Logger LOG = Logger.getLogger(JavaSpecification.class);

    private static class SubclassList {
        private final List<ObjectSpecification> classes = Lists.newArrayList();

        public void addSubclass(final ObjectSpecification subclass) {
            classes.add(subclass);
        }

        public boolean hasSubclasses() {
            return !classes.isEmpty();
        }

        public ObjectSpecification[] toArray() {
            return classes.toArray(new ObjectSpecification[0]);
        }

        /**
         * @return
         */
        public List<ObjectSpecification> toList() {
            return Collections.unmodifiableList(classes);
        }
    }

    private final SubclassList subclasses;
    private final ObjectReflectorAbstract reflector;

    private JavaIntrospector introspector;

    private Persistability persistable;

    private String pluralName;
    private String shortName;
    private String singularName;
    private String description;

    private List<ObjectSpecification> interfaces;

    private IconFacet iconMethod;
    private MarkDirtyObjectFacet markDirtyObjectFacet;
    private ClearDirtyObjectFacet clearDirtyObjectFacet;
    private IsDirtyObjectFacet isDirtyObjectFacet;

    /**
     * Lazily built by {@link #getMember(Method)}.
     */
    private Hashtable<Method, ObjectMember> membersByMethod = null;
    private Class<?> cls;

    private boolean whetherAbstract;
    private boolean whetherFinal;
    private boolean service;


    // //////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////

    public JavaSpecification(
    		final Class<?> cls,
    		final ObjectReflectorAbstract reflector,
    		final RuntimeContext runtimeContext) {
    	super(runtimeContext);
        this.introspector = new JavaIntrospector(cls, this, reflector);
        this.subclasses = new SubclassList();
        this.identifier = Identifier.classIdentifier(cls);
        this.reflector = reflector;
    }


    // //////////////////////////////////////////////////////////////////////
    // Class and stuff immediately derivable from class
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void addSubclass(final ObjectSpecification subclass) {
        subclasses.addSubclass(subclass);
    }

    @Override
    public boolean hasSubclasses() {
        return subclasses.hasSubclasses();
    }

    @Override
    public List<ObjectSpecification> interfaces() {
        return interfaces;
    }

    @Override
    public List<ObjectSpecification> subclasses() {
        return subclasses.toList();
    }

    // //////////////////////////////////////////////////////////////////////
    // Hierarchical
    // //////////////////////////////////////////////////////////////////////

    /**
     * Determines if this class represents the same class, or a subclass, of the specified class.
     */
    @Override
    public boolean isOfType(final ObjectSpecification specification) {
        if (specification == this) {
            return true;
        } else {
            if (interfaces != null) {
                for (int i = 0, len = interfaces.size(); i < len; i++) {
                    if (interfaces.get(i).isOfType(specification)) {
                        return true;
                    }
                }
            }
            if (superClassSpecification != null) {
                return superClassSpecification.isOfType(specification);
            }
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////////
    // introspect
    // //////////////////////////////////////////////////////////////////////

    @Override
    public synchronized void introspect(final FacetDecoratorSet decorator) {
        if (introspector == null) {
            throw new ReflectionException("Introspection already taken place, cannot introspect again");
        }

        cls = introspector.getIntrospectedClass();

        introspector.introspectClass();

        fullName = introspector.getFullName();
        shortName = introspector.shortName();
        NamedFacet namedFacet = getFacet(NamedFacet.class);
        if (namedFacet == null) {
            namedFacet = new NamedFacetInferred(NameUtils.naturalName(shortName), this);
            addFacet(namedFacet);
        }

        PluralFacet pluralFacet = getFacet(PluralFacet.class);
        if (pluralFacet == null) {
            pluralFacet = new PluralFacetInferred(NameUtils.pluralName(namedFacet.value()), this);
            addFacet(pluralFacet);
        }

        whetherAbstract = introspector.isAbstract();
        whetherFinal = introspector.isFinal();

        final String superclassName = introspector.getSuperclass();
        final String[] interfaceNames = introspector.getInterfaces();

        final SpecificationLoader loader = getReflector();
        if (superclassName != null) {
            superClassSpecification = loader.loadSpecification(superclassName);
            if (superClassSpecification != null) {
            	if (LOG.isDebugEnabled()) {
            		LOG.debug("  Superclass " + superclassName);
            	}
                superClassSpecification.addSubclass(this);
            }
        }

        final boolean skipIntrospection = JavaClassUtils.isJavaClass(cls) || isValueClass(cls);
        if (skipIntrospection) {
        	if (LOG.isDebugEnabled()) {
        		LOG.debug("skipping introspection of properties, actions and interfaces for " + cls.getName() + " (java.xxx class)");
        	}
            fields = Collections.emptyList();
            objectActions = Collections.emptyList();
            interfaces = Collections.emptyList();

        } else {

            List<ObjectSpecification> interfaceSpecList = Lists.newArrayList();
            for (int i = 0; i < interfaceNames.length; i++) {

                Class<?> substitutedInterfaceClass = getSubstitutedClass(interfaceNames[i], getClassSubstitutor());
                if (substitutedInterfaceClass != null) {
                    ObjectSpecification interfacespec = loader.loadSpecification(substitutedInterfaceClass.getName());
                    interfaceSpecList.add(interfacespec);
                    interfacespec.addSubclass(this);
                }
            }
            interfaces = interfaceSpecList;

            introspector.introspectPropertiesAndCollections();

            final OrderSet orderedFields = introspector.getFields();
            if (orderedFields != null) {
                fields = orderFields(orderedFields);
            }

            introspector.introspectActions();

            OrderSet orderedActions = introspector.getClassActions();

            orderedActions = introspector.getObjectActions();
            objectActions = orderActions(orderedActions);
        }

        decorateAllFacets(decorator);

        clearDirtyObjectFacet = getFacet(ClearDirtyObjectFacet.class);
        markDirtyObjectFacet = getFacet(MarkDirtyObjectFacet.class);
        isDirtyObjectFacet = getFacet(IsDirtyObjectFacet.class);
        namedFacet = getFacet(NamedFacet.class);
        singularName = namedFacet.value();

        pluralFacet = getFacet(PluralFacet.class);
        pluralName = pluralFacet.value();

        final DescribedAsFacet describedAsFacet = getFacet(DescribedAsFacet.class);
        description = describedAsFacet.value();

        iconMethod = getFacet(IconFacet.class);

        final NotPersistableFacet notPersistableFacet = getFacet(NotPersistableFacet.class);
        final InitiatedBy initiatedBy = notPersistableFacet.value();
        if (initiatedBy == InitiatedBy.USER_OR_PROGRAM) {
            persistable = Persistability.TRANSIENT;
        } else if (initiatedBy == InitiatedBy.USER) {
            persistable = Persistability.PROGRAM_PERSISTABLE;
        } else {
            persistable = Persistability.USER_PERSISTABLE;
        }

        // indicates have now been introspected.
        introspector = null;
        setIntrospected(true);
    }

    private boolean isValueClass(Class<?> type) {
        return  type.getName().startsWith("org.apache.isis.applib.value.");
    }


    /**
     * Looks up the class and runs it through the {@link #getClassSubstitutor()} obtained
     * from in the constructor.
     */
    private Class<?> getSubstitutedClass(
    		final String fullyQualifiedClassName,
    		final ClassSubstitutor classSubstitor) {
        Class<?> interfaceClass;
        try {
            interfaceClass = Class.forName(fullyQualifiedClassName);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return classSubstitor.getClass(interfaceClass);
    }


    /**
     * Added to try to track down a race condition.
     */
    @Override
	public boolean isIntrospected() {
		return introspector == null;
	}



    // //////////////////////////////////////////////////////////////////////
    // Facets
    // //////////////////////////////////////////////////////////////////////

    private void decorateAllFacets(final FacetDecoratorSet decoratorSet) {
        decoratorSet.decorateAllFacets(this);
        for (int i = 0; i < fields.size(); i++) {
            ObjectAssociation objectAssociation = fields.get(i);
			decoratorSet.decorateAllFacets(objectAssociation);
        }
        for (int i = 0; i < objectActions.size(); i++) {
            ObjectAction objectAction = objectActions.get(i);
			decoratorSet.decorateAllFacets(objectAction);
            final List<ObjectActionParameter> parameters = objectActions.get(i).getParameters();
            for (int j = 0; j < parameters.size(); j++) {
                decoratorSet.decorateAllFacets(parameters.get(j));
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // getMember, catalog...
    // //////////////////////////////////////////////////////////////////////

    public ObjectMember getMember(final Method method) {
        if (membersByMethod == null) {
            membersByMethod = new Hashtable<Method, ObjectMember>();
            cataloguePropertiesAndCollections(membersByMethod);
            catalogueActions(membersByMethod);
        }
        return membersByMethod.get(method);
    }

    private void cataloguePropertiesAndCollections(final Hashtable<Method, ObjectMember> membersByMethod) {
        Filter noop = AbstractFilter.noop(ObjectAssociation.class);
        final List<ObjectAssociation> fields = getAssociations(noop);
        for (int i = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            final Facet[] facets = field.getFacets(ImperativeFacet.FILTER);
            for (int j = 0; j < facets.length; j++) {
                final ImperativeFacet facet = ImperativeFacetUtils.getImperativeFacet(facets[j]);
                for(Method imperativeFacetMethod: facet.getMethods()) {
                	membersByMethod.put(imperativeFacetMethod, field);
                }
            }
        }
    }

    private void catalogueActions(final Hashtable<Method, ObjectMember> membersByMethod) {
        final List<ObjectAction> userActions = getObjectActions(ObjectActionType.USER);
        for (int i = 0; i < userActions.size(); i++) {
            final ObjectAction userAction = userActions.get(i);
            final Facet[] facets = userAction.getFacets(ImperativeFacet.FILTER);
            for (int j = 0; j < facets.length; j++) {
                final ImperativeFacet facet = ImperativeFacetUtils.getImperativeFacet(facets[j]);
                for(Method imperativeFacetMethod: facet.getMethods()) {
                	membersByMethod.put(imperativeFacetMethod, userAction);
                }
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////
    // getStaticallyAvailableFields, getDynamically..Fields, getField
    // //////////////////////////////////////////////////////////////////////

    @Override
    public ObjectAssociation getAssociation(final String id) {
        // TODO put fields into hash
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getId().equals(id)) {
                return fields.get(i);
            }
        }
        throw new ObjectSpecificationException("No field called '" + id + "' in '" + getSingularName() + "'");
    }

    // //////////////////////////////////////////////////////////////////////
    // getObjectAction, getAction, getActions, getClassActions
    // //////////////////////////////////////////////////////////////////////

    @Override
    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String id,
            final List<ObjectSpecification> parameters) {
        final List<ObjectAction> availableActions = ArrayUtils.combine(objectActions, getServiceActions(type));
        return getAction(availableActions, type, id, parameters);
    }

    @Override
    public ObjectAction getObjectAction(final ObjectActionType type, final String nameParmsIdentityString) {
        final List<ObjectAction> availableActions = ArrayUtils.combine(objectActions, getServiceActions(type));
        return getAction2(availableActions, type, nameParmsIdentityString);
    }

    private ObjectAction getAction(
            final List<ObjectAction> availableActions,
            final ObjectActionType type,
            final String actionName,
            final List<ObjectSpecification> parameters) {
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

    private ObjectAction getAction2(
            final List<ObjectAction> availableActions,
            final ObjectActionType type,
            final String nameParmsIdentityString) {
        if (nameParmsIdentityString == null) {
            return null;
        }
        outer: for (int i = 0; i < availableActions.size(); i++) {
            final ObjectAction action = availableActions.get(i);
            if (action.getActions().size() > 0) {
                // deal with action set
                final ObjectAction a = getAction2(action.getActions(), type, nameParmsIdentityString);
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
    protected List<ObjectAction> getActions(final List<ObjectAction> availableActions, final ObjectActionType type) {
        final List<ObjectAction> actions = new ArrayList<ObjectAction>();
        for (final ObjectAction action : availableActions) {
            final ObjectActionType actionType = action.getType();
            if (actionType == ObjectActionType.SET) {
                final ObjectActionSet actionSet = (ObjectActionSet) action;
                final List<ObjectAction> subActions = actionSet.getActions();
                for (final ObjectAction subAction : subActions) {
                    if (sameActionTypeOrNotSpecified(type, subAction)) {
                        actions.add(subAction);
                        break;
                    }
                }
            } else {
                if (sameActionTypeOrNotSpecified(type, action)) {
                    actions.add(action);
                }
            }
        }

        return actions;
    }

    private boolean sameActionTypeOrNotSpecified(final ObjectActionType type, final ObjectAction action) {
        return type == null || action.getType().equals(type);
    }

    // //////////////////////////////////////////////////////////////////////
    // orderFields, orderActions
    // //////////////////////////////////////////////////////////////////////

    private List<ObjectAssociation> orderFields(final OrderSet order) {
        final List<ObjectAssociation> fields = Lists.newArrayList();
        final Enumeration<ObjectAssociation> elements = CastUtils.enumerationOver(order.elements(),
                ObjectAssociation.class);
        while (elements.hasMoreElements()) {
            final Object element = elements.nextElement();
            if (element instanceof ObjectMemberPeerImpl) {
                ObjectMemberPeerImpl javaObjectMemberPeer = (ObjectMemberPeerImpl) element;
                if (javaObjectMemberPeer.getFeatureType().isPropertyOrCollection()) {
                    final ObjectAssociation objectAssociation = createObjectAssociation(javaObjectMemberPeer);
                    fields.add(objectAssociation);
                }
            } else if (element instanceof OrderSet) {
                // Not supported at present
            } else {
                throw new UnknownTypeException(element);
            }
        }

        return fields;
    }

    private List<ObjectAction> orderActions(final OrderSet order) {
        final List<ObjectAction> actions = Lists.newArrayList();
        final Enumeration<ObjectAction> elements = CastUtils.enumerationOver(order.elements(), ObjectAction.class);
        while (elements.hasMoreElements()) {
            final Object element = elements.nextElement();
            if (element instanceof ObjectMemberPeerImpl) {
                final ObjectMemberPeerImpl memberPeer = (ObjectMemberPeerImpl) element;
                if(memberPeer.getFeatureType().isAction()) {
                    final String actionId = memberPeer.getIdentifier().getMemberName();
                    final ObjectAction objectAction = new ObjectActionImpl(actionId, memberPeer, getRuntimeContext());
                    actions.add(objectAction);
                }
            } else if (element instanceof OrderSet) {
                final OrderSet set = ((OrderSet) element);
                actions.add(new ObjectActionSet("", set.getGroupFullName(), orderActions(set), getRuntimeContext()));
            } else {
                throw new UnknownTypeException(element);
            }
        }

        return actions;
    }

    private ObjectAssociation createObjectAssociation(final ObjectMemberPeer peer) {
        if (peer.getFeatureType().isCollection()) {
            return new OneToManyAssociationImpl(peer, getRuntimeContext());

        } else {
            return new OneToOneAssociationImpl(peer, getRuntimeContext());
        }
    }

    @Override
    public boolean isCollectionOrIsAggregated() {
        return isCollection() || isValueOrIsAggregated();
    }

    @Override
    public boolean isAbstract() {
        return whetherAbstract;
    }

    @Override
    public boolean isFinal() {
        return whetherFinal;
    }

    @Override
    public boolean isService() {
        return service;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    @Override
    public String getSingularName() {
        return singularName;
    }

    @Override
    public String getPluralName() {
        return pluralName;
    }

    @Override
    public String getDescription() {
        return description == null ? "" : description;
    }

    private TitleFacet titleFacet;

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
        return (this.isService() ? "" : "Untitled ") + getSingularName();
    }

    @Override
    public String getIconName(final ObjectAdapter reference) {
        if (iconMethod == null) {
            return null;
        } else {
            return iconMethod.iconName(reference);
        }
    }

    @Override
    public Persistability persistability() {
        return persistable;
    }

    // //////////////////////////////////////////////////////////////////////
    // createObject
    // //////////////////////////////////////////////////////////////////////

    @Override
    public Object createObject(CreationMode creationMode) {
        if (cls.isArray()) {
            return Array.newInstance(cls.getComponentType(), 0);
        }

        try {
            Object object = getRuntimeContext().instantiate(cls);

            if (creationMode == CreationMode.INITIALIZE) {
                final ObjectAdapter adapter = getRuntimeContext().adapterFor(object);

                // initialize new object
                final List<ObjectAssociation> fields = adapter.getSpecification().getAssociations();
        		for (int i = 0; i < fields.size(); i++) {
        		    fields.get(i).toDefault(adapter);
        		}
        		getRuntimeContext().injectDependenciesInto(object);

        		CallbackUtils.callCallback(adapter, CreatedCallbackFacet.class);
            }
			return object;
        } catch (final ObjectInstantiationException e) {
            throw new IsisException("Failed to create instance of type " + cls.getName(), e);
        }
    }

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
    // markAsService
    // //////////////////////////////////////////////////////////////////////

    /**
     * TODO: should ensure that service has at least one user action; fix when specification knows of its
     * hidden methods.
     *
     * <pre>
     * if (objectActions != null &amp;&amp; objectActions.length == 0) {
     *     throw new ObjectSpecificationException(&quot;Service object &quot; + getFullName() + &quot; should have at least one user action&quot;);
     * }
     * </pre>
     */
    @Override
    public void markAsService() {
        final List<ObjectAssociation> fields = getAssociations();
        if (fields != null && fields.size() > 0) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < fields.size(); i++) {
                final String name = fields.get(i).getId();
                if ("id".indexOf(name) == -1) {
                    appendFieldName(buf, name);
                }
            }
            if (buf.length() > 0) {
                throw new ObjectSpecificationException("Service object " + getFullName()
                        + " should have no fields, but has: " + buf);
            }
        }

        service = true;
    }


    private void appendFieldName(StringBuilder fieldNames, final String name) {
        fieldNames.append(fieldNames.length() > 0 ? ", " : "");
        fieldNames.append(name);
    }



    // //////////////////////////////////////////////////////////////////////
    // Debug, toString
    // //////////////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugString debug) {
        debug.blankLine();
        debug.appendln("Title", getFacet(TitleFacet.class));
        if (iconMethod != null) {
            debug.appendln("Icon", iconMethod);
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
        str.append("class", fullName);
        str.append("type", SpecUtils.typeNameFor(this));
        str.append("persistable", persistable);
        str.append("superclass", superClassSpecification == null ? "Object" : superClassSpecification.getFullName());
        return str.toString();
    }



    // //////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // //////////////////////////////////////////////////////////////////

    private ObjectReflectorAbstract getReflector() {
        return reflector;
    }

    /**
     * Derived from {@link #getReflector()}
     */
    private ClassSubstitutor getClassSubstitutor() {
        return reflector.getClassSubstitutor();
    }





}
