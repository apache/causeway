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


package org.apache.isis.core.progmodel.specloader.internal.introspector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.JavaClassUtils;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.exceptions.ReflectionException;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.facets.MethodScope;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.spec.identifier.Util;
import org.apache.isis.core.metamodel.specloader.ObjectReflectorAbstract;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor;
import org.apache.isis.core.metamodel.specloader.internal.facetprocessor.FacetProcessor;
import org.apache.isis.core.metamodel.specloader.internal.introspector.MethodFinderUtils;
import org.apache.isis.core.metamodel.specloader.internal.peer.JavaObjectActionParamPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.JavaObjectActionPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.JavaObjectAssociationPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectActionParamPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectActionPeer;
import org.apache.isis.core.metamodel.specloader.internal.peer.ObjectMemberPeer;
import org.apache.isis.core.metamodel.specloader.traverser.SpecificationTraverser;
import org.apache.isis.core.metamodel.util.NameUtils;
import org.apache.isis.core.progmodel.facets.object.facets.FacetsFacet;
import org.apache.isis.core.progmodel.facets.ordering.OrderSet;
import org.apache.isis.core.progmodel.facets.ordering.SimpleOrderSet;
import org.apache.isis.core.progmodel.facets.ordering.actionorder.ActionOrderFacet;
import org.apache.isis.core.progmodel.facets.ordering.fieldorder.FieldOrderFacet;
import org.apache.isis.core.progmodel.facets.ordering.memberorder.DeweyOrderSet;
import org.apache.isis.core.progmodel.spec.JavaSpecification;


public class JavaIntrospector {

    private final class JavaIntrospectorMethodRemover implements MethodRemover {

        @Override
        public void removeMethod(
                final MethodScope methodScope,
                final String methodName,
                final Class<?> returnType,
                final Class<?>[] parameterTypes) {
            MethodFinderUtils.removeMethod(
            		methods, methodScope, methodName, returnType, parameterTypes);
        }

        @Override
        public List<Method> removeMethods(
                final MethodScope methodScope,
                final String prefix,
                final Class<?> returnType,
                final boolean canBeVoid,
                final int paramCount) {
            return MethodFinderUtils.removeMethods(methods, methodScope, prefix, returnType, canBeVoid, paramCount);
        }

        @Override
        public void removeMethod(final Method method) {
            for (int i = 0; i < methods.size(); i++) {
                if (methods.get(i) == null) {
                    continue;
                }
                if (methods.get(i).equals(method)) {
                    methods.set(i, null);
                }
            }
        }

        @Override
        public void removeMethods(final List<Method> methodList) {
            final List<Method> methodList2 = methodList;
            for (int i = 0; i < methods.size(); i++) {
                if (methods.get(i) == null) {
                    continue;
                }
                for (final Method method : methodList2) {
                    if (methods.get(i).equals(method)) {
                        methods.set(i, null);
                        break;
                    }
                }
            }
        }
    }

    private static final Logger LOG = Logger.getLogger(JavaIntrospector.class);

    private static final Object[] NO_PARAMETERS = new Object[0];
    private static final Class<?>[] NO_PARAMETERS_TYPES = new Class[0];

    private static final String GET_PREFIX = "get";
    private static final String IS_PREFIX = "is";

    private final Class<?> type;
    private final List<Method> methods;

    private OrderSet orderedFields;
    private OrderSet orderedObjectActions;
    private OrderSet orderedClassActions;

    private final ObjectReflectorAbstract reflector;
    private final JavaSpecification javaSpecification;

    // ////////////////////////////////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////////////////////////////////

    public JavaIntrospector(final Class<?> type,
    		final JavaSpecification javaSpecification,
    		final ObjectReflectorAbstract reflector) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("creating JavaIntrospector for " + type);
    	}

        this.type = type;
        this.javaSpecification = javaSpecification;
        this.reflector = reflector;


        // previously we tested that the adapted class was public, as in
        // !JavaClassUtils.isPublic(cls). However, there doesn't seem to be any
        // good reason to have this restriction, while having it prevents us from
        // using third party libraries for value types that have non-public
        // interfaces or non-public concrete implementations. Therefore the test
        // has been removed.

        methods = Arrays.asList(type.getMethods());
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Introspection Control Parameters
    // ////////////////////////////////////////////////////////////////////////////

    private SpecificationTraverser getSpecificationTraverser() {
    	return reflector.getSpecificationTraverser();
    }

    private FacetProcessor getFacetProcessor() {
        return reflector.getFacetProcessor();
    }

    private ClassSubstitutor getClassSubstitutor() {
        return reflector.getClassSubstitutor();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Class and stuff immediately derived from class
    // ////////////////////////////////////////////////////////////////////////////

    public Class<?> getIntrospectedClass() {
        return type;
    }

    public String getFullName() {
        return type.getName();
    }

    public String[] getInterfaces() {
        return JavaClassUtils.getInterfaces(type);
    }

    public String getSuperclass() {
        return JavaClassUtils.getSuperclass(type);
    }

    public boolean isAbstract() {
        return JavaClassUtils.isAbstract(type);
    }

    public boolean isFinal() {
        return JavaClassUtils.isFinal(type);
    }

    private String getClassName() {
        return type.getName();
    }

    public String shortName() {
        final String name = getClassName();
        return name.substring(name.lastIndexOf('.') + 1);
    }


    // ////////////////////////////////////////////////////////////////////////////
    // introspect
    // ////////////////////////////////////////////////////////////////////////////

    public void introspectClass() {
        LOG.info("introspecting " + getClassName());
        if (LOG.isDebugEnabled()) {
        	LOG.debug("introspecting " + getClassName() + ": class-level details");
        }

        // process facets at object level
        // this will also remove some methods, such as the superclass methods.
        final JavaIntrospectorMethodRemover methodRemover = new JavaIntrospectorMethodRemover();
        getFacetProcessor().process(type, methodRemover, javaSpecification);

        // if this class has additional facets (as per @Facets), then process them.
        final FacetsFacet facetsFacet = javaSpecification.getFacet(FacetsFacet.class);
        if (facetsFacet != null) {
            final Class<? extends FacetFactory>[] facetFactories = facetsFacet.facetFactories();
            for (int i = 0; i < facetFactories.length; i++) {
                FacetFactory facetFactory = null;
                try {
                    facetFactory = facetFactories[i].newInstance();
                } catch (final InstantiationException e) {
                    throw new IsisException(e);
                } catch (final IllegalAccessException e) {
                    throw new IsisException(e);
                }
                getFacetProcessor().injectDependenciesInto(facetFactory);
                facetFactory.process(type, methodRemover, javaSpecification);
            }
        }
    }

    public void introspectPropertiesAndCollections() {
        LOG.debug("introspecting " + getClassName() + ": properties and collections");

        // find the properties and collections (fields) ...
        final List<ObjectMemberPeer> fieldPeers = findAndCreateFieldPeers();

        // ... and the ordering of the properties and collections
        String fieldOrder;
        final FieldOrderFacet fieldOrderFacet = javaSpecification.getFacet(FieldOrderFacet.class);
        if (fieldOrderFacet == null) {
            fieldOrder = null;
        } else {
            fieldOrder = fieldOrderFacet.value();
        }

        if (fieldOrder == null) {
            // TODO: the calling of fieldOrder() should be a facet
            fieldOrder = invokeSortOrderMethod("field");
        }
        orderedFields = createOrderSet(fieldOrder, fieldPeers);
    }

    public void introspectActions() {
        LOG.debug("introspecting " + getClassName() + ": actions");

        // find the actions ...
        MethodScope methodScope = MethodScope.OBJECT;
		final List<ObjectMemberPeer> actionPeers = findActionMethodsPeers(methodScope);


        // ... and the ordering of actions ...
        String actionOrder;
        final ActionOrderFacet actionOrderFacet = javaSpecification.getFacet(ActionOrderFacet.class);
        if (actionOrderFacet == null) {
            actionOrder = null;
        } else {
            actionOrder = actionOrderFacet.value();
        }
        if (actionOrder == null) {
            // TODO: the calling of actionOrder() should be a facet
            actionOrder = invokeSortOrderMethod("action");
        }
        orderedObjectActions = createOrderSet(actionOrder, actionPeers);

        // find the class actions ...
        MethodScope methodsScope2 = MethodScope.CLASS;
		final List<ObjectMemberPeer> findClassActionMethods = findActionMethodsPeers(methodsScope2);

        // ... and the ordering of class actions
        actionOrder = null;
        if (actionOrder == null) {
            // TODO: the calling of classActionOrder() should be a facet
            actionOrder = invokeSortOrderMethod("classAction");
        }
        orderedClassActions = createOrderSet(actionOrder, findClassActionMethods);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // find Properties and Collections (fields)
    // ////////////////////////////////////////////////////////////////////////////

    private List<ObjectMemberPeer> findAndCreateFieldPeers() {
        if(LOG.isDebugEnabled()) {
            LOG.debug("  looking for fields for " + type);
        }

        // Ensure all return types are known
        final Set<Method> propertyOrCollectionCandidates = getFacetProcessor().findPropertyOrCollectionCandidateAccessors(
                methods, new HashSet<Method>());

        List<Class<?>> typesToLoad = new ArrayList<Class<?>>();
		for (final Method method : propertyOrCollectionCandidates) {
			getSpecificationTraverser().traverseTypes(method, typesToLoad);
		}
		reflector.loadSpecifications(typesToLoad, type);

        // now create FieldPeers for value properties, for collections and for reference properties
        final List<ObjectMemberPeer> fieldPeers = new ArrayList<ObjectMemberPeer>();

        findAndRemoveCollectionAccessorsAndCreateCorrespondingFieldPeers(fieldPeers);
        findAndRemovePropertyAccessorsAndCreateCorrespondingFieldPeers(fieldPeers);

        return Collections.unmodifiableList(fieldPeers);
    }

    private void findAndRemoveCollectionAccessorsAndCreateCorrespondingFieldPeers(final List<ObjectMemberPeer> associationPeers) {
        final List<Method> collectionAccessors = new ArrayList<Method>();
        getFacetProcessor().findAndRemoveCollectionAccessors(new JavaIntrospectorMethodRemover(), collectionAccessors);
        createCollectionPeersFromAccessors(collectionAccessors, associationPeers);
    }

    /**
     * Since the value properties and collections have already been processed, this will pick up the remaining
     * reference properties.
     */
    private void findAndRemovePropertyAccessorsAndCreateCorrespondingFieldPeers(final List<ObjectMemberPeer> fields) {
        final List<Method> propertyAccessors = new ArrayList<Method>();
        getFacetProcessor().findAndRemovePropertyAccessors(new JavaIntrospectorMethodRemover(), propertyAccessors);

        findAndRemovePrefixedNonVoidMethods(MethodScope.OBJECT, GET_PREFIX, Object.class, 0, propertyAccessors);
        findAndRemovePrefixedNonVoidMethods(MethodScope.OBJECT, IS_PREFIX, Boolean.class, 0, propertyAccessors);

        createPropertyPeersFromAccessors(propertyAccessors, fields);
    }

    private void createCollectionPeersFromAccessors(final List<Method> collectionAccessors, final List<ObjectMemberPeer> associationPeerListToAppendto) {
        for (final Method getMethod : collectionAccessors) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("  identified one-many association method " + getMethod);
            }

            // create property and add facets
            final JavaObjectAssociationPeer collection = JavaObjectAssociationPeer.createCollectionPeer(type, getMethod, getSpecificationLoader());
            getFacetProcessor().process(type, getMethod, new JavaIntrospectorMethodRemover(), collection,
                    ObjectFeatureType.COLLECTION);

            // figure out what the type is
            Class<?> elementType = Object.class;
            final TypeOfFacet typeOfFacet = collection.getFacet(TypeOfFacet.class);
            if (typeOfFacet != null) {
                elementType = typeOfFacet.value();
            }
            collection.setType(elementType);

            // skip if class substitutor says so.
            if (getClassSubstitutor().getClass(elementType) == null) {
                continue;
            }

            associationPeerListToAppendto.add(collection);
        }
    }

    private void createPropertyPeersFromAccessors(final List<Method> propertyAccessors, final List<ObjectMemberPeer> associationPeerListToAppendto)
            throws ReflectionException {

        for (final Method getMethod : propertyAccessors) {
            LOG.debug("  identified 1-1 association method " + getMethod);

            final Class<?> returnType = getMethod.getReturnType();

            // skip if class strategy says so.
            if (getClassSubstitutor().getClass(returnType) == null) {
                continue;
            }

            // create a 1:1 association peer
            final JavaObjectAssociationPeer associationPeer = JavaObjectAssociationPeer.createPropertyPeer(type, getMethod, returnType, getSpecificationLoader());

            // process facets for the 1:1 association
            getFacetProcessor().process(type, getMethod, new JavaIntrospectorMethodRemover(), associationPeer,
                    ObjectFeatureType.PROPERTY);

            associationPeerListToAppendto.add(associationPeer);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////
    // find Actions
    // ////////////////////////////////////////////////////////////////////////////

	private List<ObjectMemberPeer> findActionMethodsPeers(
			MethodScope methodScope) {
		final List<ObjectMemberPeer> actionPeers1 = findActionMethodPeers(methodScope, RecognisedHelpersStrategy.SKIP);
        final List<ObjectMemberPeer> actionPeers2 = findActionMethodPeers(methodScope, RecognisedHelpersStrategy.DONT_SKIP);
		List<ObjectMemberPeer> peers = Lists.newArrayList(actionPeers1);
        peers.addAll(actionPeers2);
        return peers;
	}


	private enum RecognisedHelpersStrategy {
	    SKIP,
	    DONT_SKIP;
        public boolean skip() {
            return this == SKIP;
        }
	}
	
    private List<ObjectMemberPeer> findActionMethodPeers(final MethodScope methodScope, RecognisedHelpersStrategy skipRecognisedHelpers) {
    	if(LOG.isDebugEnabled()) {
    		LOG.debug("  looking for action methods");
    	}

        final List<ObjectMemberPeer> actionPeers = new ArrayList<ObjectMemberPeer>();

        for (int i = 0; i < methods.size(); i++) {
            Method method = methods.get(i);
            if (method == null) {
                continue;
            }
            ObjectActionPeer actionPeer = findActionMethodPeer(methodScope, skipRecognisedHelpers, method);
            if (actionPeer != null) {
            	methods.set(i, null);
            	actionPeers.add(actionPeer);
            }
        }

        return actionPeers;
    }

    private ObjectActionPeer findActionMethodPeer(final MethodScope methodScope, RecognisedHelpersStrategy skipRecognisedHelpers, final Method actionMethod) {

    	if (!representsAction(methodScope, skipRecognisedHelpers, actionMethod)) {
    		return null;
    	}

        // build action
        return createAction(actionMethod);
    }

	private ObjectActionPeer createAction(final Method actionMethod) {
	    
		if (!isAllParamTypesValid(actionMethod)) {
		    return null;
		}
        
        Class<?> returnType = actionMethod.getReturnType();
        final JavaObjectActionPeer action = new JavaObjectActionPeer(type, actionMethod, returnType, reflector);

        // process facets on the action & parameters
        getFacetProcessor()
                .process(type, actionMethod, new JavaIntrospectorMethodRemover(), action, ObjectFeatureType.ACTION);

        ObjectActionParamPeer[] actionParams = action.getParameters();
        for (int j = 0; j < actionParams.length; j++) {
            getFacetProcessor().processParams(actionMethod, j, actionParams[j]);
        }

        return action;
	}

    public boolean isAllParamTypesValid(final Method actionMethod) {
        return Util.isAllParamTypesValid(actionMethod, getSpecificationLoader());
    }

    private JavaObjectActionParamPeer[] getParamPeersFor(final Method actionMethod) {
        return Util.getParamPeers(actionMethod, getSpecificationLoader());
    }

    private boolean representsAction(final MethodScope methodScope, RecognisedHelpersStrategy skipRecognisedHelpers, final Method actionMethod) {

        if (!MethodFinderUtils.inScope(methodScope, actionMethod)) {
            return false;
        }

        List<Class<?>> typesToLoad = new ArrayList<Class<?>>();
        getSpecificationTraverser().traverseTypes(actionMethod, typesToLoad);

        boolean anyLoadedAsNull = reflector.loadSpecifications(typesToLoad);
        if (anyLoadedAsNull) {
        	return false;
        }

        if (!loadParamSpecs(actionMethod)) {
        	return false;
        }

        if (getFacetProcessor().recognizes(actionMethod)) {
            if (actionMethod.getName().startsWith("set")) {
            	return false;
            }
            if (skipRecognisedHelpers.skip()) {
                LOG.info("  skipping possible helper method " + actionMethod);
            	return false;
            }
        }

        if (LOG.isDebugEnabled()) {
        	LOG.debug("  identified action " + actionMethod);
        }

        return true;
    }

	private boolean loadParamSpecs(final Method actionMethod) {
		final Class<?>[] parameterTypes = actionMethod.getParameterTypes();
        return loadParamSpecs(parameterTypes);
	}

	private boolean loadParamSpecs(final Class<?>[] parameterTypes) {
        final int numParameters = parameterTypes.length;
        for (int j = 0; j < numParameters; j++) {
            ObjectSpecification paramSpec = getSpecificationLoader().loadSpecification(parameterTypes[j]);
            if (paramSpec == null) {
            	return false;
            }
        }
		return true;
	}

	private ObjectActionPeer[] convertToArray(final List<ObjectActionPeer> actions) {
        return actions.toArray(new ObjectActionPeer[] {});
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Helpers for finding and removing methods.
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Searches for specific method and returns it, also removing it from the {@link #methods array of
     * methods} if found.
     *
     * @see MethodFinderUtils#removeMethod(Method[], boolean, String, Class, Class[])
     */
    private Method findAndRemoveMethod(
            final MethodScope methodScope,
            final String name,
            final Class<?> returnType,
            final Class<?>[] paramTypes) {
        return MethodFinderUtils.removeMethod(methods, methodScope, name, returnType, paramTypes);
    }


    /**
     * As per {@link #findAndRemovePrefixedNonVoidMethods(boolean, String, Class, int)}, but appends to
     * provided {@link List} (collecting parameter pattern).
     */
    private void findAndRemovePrefixedNonVoidMethods(
            final MethodScope methodScope,
            final String prefix,
            final Class<?> returnType,
            final int paramCount,
            final List<Method> methodListToAppendTo) {
        final List<Method> matchingMethods = findAndRemovePrefixedMethods(methodScope, prefix, returnType, false, paramCount);
        methodListToAppendTo.addAll(matchingMethods);
    }

    /**
     * Searches for all methods matching the prefix and returns them, also removing it from the
     * {@link #methods array of methods} if found.
     * @param objectFactory
     *
     * @see MethodFinderUtils#removeMethods(Method[], boolean, String, Class, boolean, int, ClassSubstitutor)
     */
    private List<Method> findAndRemovePrefixedMethods(
            final MethodScope methodScope,
            final String prefix,
            final Class<?> returnType,
            final boolean canBeVoid,
            final int paramCount) {
        return MethodFinderUtils.removeMethods(methods, methodScope, prefix, returnType, canBeVoid, paramCount);
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Sort Member Peers
    // ////////////////////////////////////////////////////////////////////////////

    private OrderSet createOrderSet(final String order, final List<ObjectMemberPeer> members) {
        if (order != null) {
            OrderSet set;
            set = SimpleOrderSet.createOrderSet(order, members);
            return set;
        } else {
            final OrderSet set = DeweyOrderSet.createOrderSet(members);
            return set;
        }
    }

    private String invokeSortOrderMethod(final String type) {
        final Method method = findAndRemoveMethod(MethodScope.CLASS, type + "Order", String.class, NO_PARAMETERS_TYPES);
        if (method == null) {
            return null;
        } else if (!JavaClassUtils.isStatic(method)) {
            LOG.warn("method " + getClassName() + "." + type + "Order() must be declared as static");
            return null;
        } else {
            String s;
            s = (String) invokeMethod(method, NO_PARAMETERS);
            if (s.trim().length() == 0) {
                return null;
            }
            return s;
        }
    }

    private Object invokeMethod(final Method method, final Object[] parameters) {
        try {
            return method.invoke(null, parameters);
        } catch (final IllegalAccessException ignore) {
            LOG.warn("method " + getClassName() + "." + method.getName() + "() must be declared as public");
            return null;
        } catch (final InvocationTargetException e) {
            throw new ReflectionException(e);
        }
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Resultant Members
    // ////////////////////////////////////////////////////////////////////////////

    public OrderSet getFields() {
        return orderedFields;
    }

    public OrderSet getClassActions() {
        return orderedClassActions;
    }

    public OrderSet getObjectActions() {
        return orderedObjectActions;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // finalize
    // ////////////////////////////////////////////////////////////////////////////

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LOG.debug("finalizing reflector " + this);
    }


    // ////////////////////////////////////////////////////////////////////////////
    // (not used) - although it should be be Field and ActionOrder annotations
    // ////////////////////////////////////////////////////////////////////////////

    /*
     * REVIEW should we still support ordering of properties/actions via a string (this
     * could allow user specified ordered rather than coder based
     *
     * Also a preference issue - it is clearer to read the order from an order string from the @FieldOrder and @MemberOrder although it is  more
     * difficult to ensure validity.
     */
    /**
     * This was pulled out of JavaIntrospector, but doesn't seem to be used any more.
     *
     * <p>
     * Have therefore made private.
     */
    private static List<ObjectMemberPeer> orderArray(final List<ObjectMemberPeer> original, final String[] order) {
        if (order == null) {
            return original;

        } else {
            for (int i = 0; i < order.length; i++) {
                order[i] = NameUtils.simpleName(order[i]);
            }

            final List<ObjectMemberPeer> ordered = Lists.newArrayListWithCapacity(original.size());

            // work through each order element and find, if there is one, a
            // matching member.
            int orderedIndex = 0;
            ordering: for (int orderIndex = 0; orderIndex < order.length; orderIndex++) {
                for (int memberIndex = 0; memberIndex < original.size(); memberIndex++) {
                    final ObjectMemberPeer member = original.get(memberIndex);
                    if (member == null) {
                        continue;
                    }
                    if (member.getIdentifier().getMemberName().equalsIgnoreCase(order[orderIndex])) {
                        ordered.set(orderedIndex++, original.get(memberIndex));
                        original.set(memberIndex, null);

                        continue ordering;
                    }
                }

                if (!order[orderIndex].trim().equals("")) {
                    LOG.warn("invalid ordering element '" + order[orderIndex]);
                }
            }

            final List<ObjectMemberPeer> results = Lists.newArrayListWithCapacity(original.size());
            int index = append(0, results, ordered);
            index = append(index, results, original);

            return results;
        }
    }

    protected static int append(int index, final List<ObjectMemberPeer> to, List<ObjectMemberPeer> from) {
        for (int i = 0; i < from.size(); i++) {
            final ObjectMemberPeer member = from.get(i);
            if (member != null) {
                to.set(index++, member);
            }
        }
        return index;
    }



    // ////////////////////////////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // ////////////////////////////////////////////////////////////////////////////

    private SpecificationLoader getSpecificationLoader() {
        return reflector;
    }

    @Override
    public String toString() {
        ToString str = new ToString(this);
        str.append("class", getClassName());
        return str.toString();
    }


}
