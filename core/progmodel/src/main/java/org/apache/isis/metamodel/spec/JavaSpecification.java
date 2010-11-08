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


package org.apache.isis.metamodel.spec;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

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
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.exceptions.ReflectionException;
import org.apache.isis.metamodel.facetdecorator.FacetDecoratorSet;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.naming.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacet;
import org.apache.isis.metamodel.facets.naming.named.NamedFacetInferred;
import org.apache.isis.metamodel.facets.object.callbacks.CreatedCallbackFacet;
import org.apache.isis.metamodel.facets.object.dirty.ClearDirtyObjectFacet;
import org.apache.isis.metamodel.facets.object.dirty.IsDirtyObjectFacet;
import org.apache.isis.metamodel.facets.object.dirty.MarkDirtyObjectFacet;
import org.apache.isis.metamodel.facets.object.ident.icon.IconFacet;
import org.apache.isis.metamodel.facets.object.ident.plural.PluralFacet;
import org.apache.isis.metamodel.facets.object.ident.plural.PluralFacetInferred;
import org.apache.isis.metamodel.facets.object.ident.title.TitleFacet;
import org.apache.isis.metamodel.facets.object.notpersistable.InitiatedBy;
import org.apache.isis.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.metamodel.facets.ordering.OrderSet;
import org.apache.isis.metamodel.java5.ImperativeFacet;
import org.apache.isis.metamodel.java5.ImperativeFacetUtils;
import org.apache.isis.metamodel.runtimecontext.ObjectInstantiationException;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.spec.IntrospectableSpecificationAbstract;
import org.apache.isis.metamodel.runtimecontext.spec.feature.ObjectActionSet;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.metamodel.specloader.internal.ObjectActionImpl;
import org.apache.isis.metamodel.specloader.internal.OneToManyAssociationImpl;
import org.apache.isis.metamodel.specloader.internal.OneToOneAssociationImpl;
import org.apache.isis.metamodel.specloader.internal.introspector.JavaIntrospector;
import org.apache.isis.metamodel.specloader.internal.peer.JavaObjectActionPeer;
import org.apache.isis.metamodel.specloader.internal.peer.JavaObjectAssociationPeer;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectAssociationPeer;
import org.apache.isis.metamodel.util.CallbackUtils;
import org.apache.isis.metamodel.util.NameUtils;
import org.apache.isis.metamodel.util.SpecUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;


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
    }

    private final SubclassList subclasses;
    private final ObjectReflectorAbstract reflector;

    private JavaIntrospector introspector;

    private Persistability persistable;

    private String pluralName;
    private String shortName;
    private String singularName;
    private String description;

    private ObjectSpecification[] interfaces;

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
    public ObjectSpecification[] interfaces() {
        return interfaces;
    }

    @Override
    public ObjectSpecification[] subclasses() {
        return subclasses.toArray();
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
                for (int i = 0, len = interfaces.length; i < len; i++) {
                    if (interfaces[i].isOfType(specification)) {
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

    public synchronized void introspect(final FacetDecoratorSet decorator) {
        if (introspector == null) {
            throw new ReflectionException("Introspection already taken place, cannot introspect again");
        }

        cls = introspector.getIntrospectedClass();

        introspector.introspectClass();

        fullName = introspector.getFullName();
        shortName = introspector.shortName();
        NamedFacet namedFacet = (NamedFacet) getFacet(NamedFacet.class);
        if (namedFacet == null) {
            namedFacet = new NamedFacetInferred(NameUtils.naturalName(shortName), this);
            addFacet(namedFacet);
        }

        PluralFacet pluralFacet = (PluralFacet) getFacet(PluralFacet.class);
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
            fields = new ObjectAssociation[0];
            objectActions = new ObjectAction[0];
            interfaces = new ObjectSpecification[0];

        } else {

            List<ObjectSpecification> interfaceSpecList = new ArrayList<ObjectSpecification>();
            for (int i = 0; i < interfaceNames.length; i++) {

                Class<?> substitutedInterfaceClass = getSubstitutedClass(interfaceNames[i], getClassSubstitutor());
                if (substitutedInterfaceClass != null) {
                    ObjectSpecification interfacespec = loader.loadSpecification(substitutedInterfaceClass.getName());
                    interfaceSpecList.add(interfacespec);
                    interfacespec.addSubclass(this);
                }
            }
            interfaces = interfaceSpecList.toArray(new ObjectSpecification[]{});

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

        clearDirtyObjectFacet = (ClearDirtyObjectFacet) getFacet(ClearDirtyObjectFacet.class);
        markDirtyObjectFacet = (MarkDirtyObjectFacet) getFacet(MarkDirtyObjectFacet.class);
        isDirtyObjectFacet = (IsDirtyObjectFacet) getFacet(IsDirtyObjectFacet.class);
        namedFacet = (NamedFacet) getFacet(NamedFacet.class);
        singularName = namedFacet.value();

        pluralFacet = (PluralFacet) getFacet(PluralFacet.class);
        pluralName = pluralFacet.value();

        final DescribedAsFacet describedAsFacet = (DescribedAsFacet) getFacet(DescribedAsFacet.class);
        description = describedAsFacet.value();

        iconMethod = (IconFacet) getFacet(IconFacet.class);

        final NotPersistableFacet notPersistableFacet = (NotPersistableFacet) getFacet(NotPersistableFacet.class);
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
        for (int i = 0; i < fields.length; i++) {
            ObjectAssociation objectAssociation = fields[i];
			decoratorSet.decorateAllFacets(objectAssociation);
        }
        for (int i = 0; i < objectActions.length; i++) {
            ObjectAction objectAction = objectActions[i];
			decoratorSet.decorateAllFacets(objectAction);
            final ObjectActionParameter[] parameters = objectActions[i].getParameters();
            for (int j = 0; j < parameters.length; j++) {
                decoratorSet.decorateAllFacets(parameters[j]);
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
        final ObjectAssociation[] fields = getAssociations(noop);
        for (int i = 0; i < fields.length; i++) {
            final ObjectAssociation field = fields[i];
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
        final ObjectAction[] userActions = getObjectActions(ObjectActionType.USER);
        for (int i = 0; i < userActions.length; i++) {
            final ObjectAction userAction = userActions[i];
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

    public ObjectAssociation getAssociation(final String id) {
        // TODO put fields into hash
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getId().equals(id)) {
                return fields[i];
            }
        }
        throw new ObjectSpecificationException("No field called '" + id + "' in '" + getSingularName() + "'");
    }

    // //////////////////////////////////////////////////////////////////////
    // getObjectAction, getAction, getActions, getClassActions
    // //////////////////////////////////////////////////////////////////////

    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String id,
            final ObjectSpecification[] parameters) {
        final ObjectAction[] availableActions = ArrayUtils.combine(objectActions, getServiceActions(type));
        return getAction(availableActions, type, id, parameters);
    }

    public ObjectAction getObjectAction(final ObjectActionType type, final String nameParmsIdentityString) {
        final ObjectAction[] availableActions = ArrayUtils.combine(objectActions, getServiceActions(type));
        return getAction2(availableActions, type, nameParmsIdentityString);
    }

    private ObjectAction getAction(
            final ObjectAction[] availableActions,
            final ObjectActionType type,
            final String actionName,
            final ObjectSpecification[] parameters) {
        outer: for (int i = 0; i < availableActions.length; i++) {
            final ObjectAction action = availableActions[i];
            if (action.getActions().length > 0) {
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
                if (action.getParameters().length != parameters.length) {
                    continue outer;
                }
                for (int j = 0; j < parameters.length; j++) {
                    if (!parameters[j].isOfType(action.getParameters()[j].getSpecification())) {
                        continue outer;
                    }
                }
                return action;
            }
        }
        return null;
    }

    private ObjectAction getAction2(
            final ObjectAction[] availableActions,
            final ObjectActionType type,
            final String nameParmsIdentityString) {
        if (nameParmsIdentityString == null) {
            return null;
        }
        outer: for (int i = 0; i < availableActions.length; i++) {
            final ObjectAction action = availableActions[i];
            if (action.getActions().length > 0) {
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
    protected ObjectAction[] getActions(final ObjectAction[] availableActions, final ObjectActionType type) {
        final List<ObjectAction> actions = new ArrayList<ObjectAction>();
        for (final ObjectAction action : availableActions) {
            final ObjectActionType actionType = action.getType();
            if (actionType == ObjectActionType.SET) {
                final ObjectActionSet actionSet = (ObjectActionSet) action;
                final ObjectAction[] subActions = actionSet.getActions();
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

        return actions.toArray(new ObjectAction[0]);
    }

    private boolean sameActionTypeOrNotSpecified(final ObjectActionType type, final ObjectAction action) {
        return type == null || action.getType().equals(type);
    }

    // //////////////////////////////////////////////////////////////////////
    // orderFields, orderActions
    // //////////////////////////////////////////////////////////////////////

    private ObjectAssociation[] orderFields(final OrderSet order) {
        final ObjectAssociation[] fields = new ObjectAssociation[order.size()];
        final Enumeration<ObjectAssociation> elements = CastUtils.enumerationOver(order.elements(),
                ObjectAssociation.class);
        int actionCnt = 0;
        while (elements.hasMoreElements()) {
            final Object element = elements.nextElement();
            if (element instanceof JavaObjectAssociationPeer) {
                final JavaObjectAssociationPeer javaObjectAssociationPeer = (JavaObjectAssociationPeer) element;
                final ObjectAssociation objectAssociation = createObjectField(javaObjectAssociationPeer);
                fields[actionCnt++] = objectAssociation;
            } else if (element instanceof OrderSet) {
                // Not supported at present
            } else {
                throw new UnknownTypeException(element);
            }
        }

        if (actionCnt < fields.length) {
            final ObjectAssociation[] actualActions = new ObjectAssociation[actionCnt];
            System.arraycopy(fields, 0, actualActions, 0, actionCnt);
            return actualActions;
        }
        return fields;
    }

    private ObjectAction[] orderActions(final OrderSet order) {
        final ObjectAction[] actions = new ObjectAction[order.size()];
        final Enumeration<ObjectAction> elements = CastUtils.enumerationOver(order.elements(), ObjectAction.class);
        int actionCnt = 0;
        while (elements.hasMoreElements()) {
            final Object element = elements.nextElement();
            if (element instanceof JavaObjectActionPeer) {
                final JavaObjectActionPeer javaObjectActionPeer = (JavaObjectActionPeer) element;
                final String actionId = javaObjectActionPeer.getIdentifier().getMemberName();
                final ObjectAction objectAction = new ObjectActionImpl(actionId, javaObjectActionPeer, getRuntimeContext());
                actions[actionCnt++] = objectAction;
            } else if (element instanceof OrderSet) {
                final OrderSet set = ((OrderSet) element);
                actions[actionCnt++] = new ObjectActionSet("", set.getGroupFullName(), orderActions(set), getRuntimeContext());
            } else {
                throw new UnknownTypeException(element);
            }
        }

        if (actionCnt < actions.length) {
            final ObjectAction[] actualActions = new ObjectAction[actionCnt];
            System.arraycopy(actions, 0, actualActions, 0, actionCnt);
            return actualActions;
        }
        return actions;
    }

    private ObjectAssociation createObjectField(final ObjectAssociationPeer peer) {
        ObjectAssociation field;
        if (peer.isOneToOne()) {
            field = new OneToOneAssociationImpl(peer, getRuntimeContext());

        } else if (peer.isOneToMany()) {
            field = new OneToManyAssociationImpl(peer, getRuntimeContext());

        } else {
            throw new IsisException();
        }
        return field;
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

    public String getShortName() {
        return shortName;
    }

    public String getSingularName() {
        return singularName;
    }

    public String getPluralName() {
        return pluralName;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    private TitleFacet titleFacet;

    public String getTitle(final ObjectAdapter object) {
        if (titleFacet == null) {
            titleFacet = (TitleFacet) getFacet(TitleFacet.class);
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
                final ObjectAssociation[] fields = adapter.getSpecification().getAssociations();
        		for (int i = 0; i < fields.length; i++) {
        		    fields[i].toDefault(adapter);
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
    public void markAsService() {
        final ObjectAssociation[] fields = getAssociations();
        if (fields != null && fields.length > 0) {
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < fields.length; i++) {
                final String name = fields[i].getId();
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

    public void debugData(final DebugString debug) {
        debug.blankLine();
        debug.appendln("Title", getFacet(TitleFacet.class));
        if (iconMethod != null) {
            debug.appendln("Icon", iconMethod);
        }
        debug.unindent();
    }

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
