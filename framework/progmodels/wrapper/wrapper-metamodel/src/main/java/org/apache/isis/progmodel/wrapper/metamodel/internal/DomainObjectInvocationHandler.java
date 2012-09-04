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

package org.apache.isis.progmodel.wrapper.metamodel.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.CollectionAccessEvent;
import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.applib.events.ObjectTitleEvent;
import org.apache.isis.applib.events.PropertyAccessEvent;
import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.util.AdapterUtils;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.actions.choices.ActionChoicesFacet;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyInitializationFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.progmodel.facets.actions.validate.method.ActionValidationFacetViaMethod;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateAddToFacetViaMethod;
import org.apache.isis.core.progmodel.facets.collections.validate.CollectionValidateRemoveFromFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.disable.method.DisableForContextFacetViaMethod;
import org.apache.isis.core.progmodel.facets.members.hide.method.HideForContextFacetViaMethod;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyClearFacetViaClearMethod;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertySetterFacetViaModifyMethod;
import org.apache.isis.core.progmodel.facets.properties.validate.PropertyValidateFacetViaMethod;
import org.apache.isis.progmodel.wrapper.applib.DisabledException;
import org.apache.isis.progmodel.wrapper.applib.HiddenException;
import org.apache.isis.progmodel.wrapper.applib.InteractionException;
import org.apache.isis.progmodel.wrapper.applib.InvalidException;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory.ExecutionMode;
import org.apache.isis.progmodel.wrapper.applib.WrapperObject;

public class DomainObjectInvocationHandler<T> extends DelegatingInvocationHandlerDefault<T> {

    private final Map<Method, Collection<?>> collectionViewObjectsByMethod = new HashMap<Method, Collection<?>>();
    private final Map<Method, Map<?, ?>> mapViewObjectsByMethod = new HashMap<Method, Map<?, ?>>();

    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final SpecificationLoader specificationLookup;
    private final AdapterManager adapterManager;
    private final ObjectPersistor objectPersistor;

    /**
     * The <tt>title()</tt> method; may be <tt>null</tt>.
     */
    protected Method titleMethod;

    /**
     * The <tt>save()</tt> method from {@link WrapperObject#save()}.
     */
    protected Method saveMethod;

    /**
     * The <tt>underlying()</tt> method from {@link WrapperObject#wrapped()}.
     */
    protected Method wrappedMethod;

    public DomainObjectInvocationHandler(final T delegate, final WrapperFactory embeddedViewer, final ExecutionMode mode, final AuthenticationSessionProvider authenticationSessionProvider, final SpecificationLoader specificationLookup, final AdapterManager adapterManager,
            final ObjectPersistor objectPersistor) {
        super(delegate, embeddedViewer, mode);

        this.authenticationSessionProvider = authenticationSessionProvider;
        this.specificationLookup = specificationLookup;
        this.adapterManager = adapterManager;
        this.objectPersistor = objectPersistor;

        try {
            titleMethod = delegate.getClass().getMethod("title", new Class[] {});
            saveMethod = WrapperObject.class.getMethod("save", new Class[] {});
            wrappedMethod = WrapperObject.class.getMethod("wrapped", new Class[] {});
        } catch (final NoSuchMethodException e) {
        }
    }

    @Override
    public Object invoke(final Object proxyObject, final Method method, final Object[] args) throws Throwable {

        if (isObjectMethod(method)) {
            return delegate(method, args);
        }

        final ObjectAdapter targetAdapter = getAdapterManager().getAdapterFor(getDelegate());

        if (isTitleMethod(method)) {
            return handleTitleMethod(method, args, targetAdapter);
        }

        final ObjectSpecification targetNoSpec = targetAdapter.getSpecification();

        // save method, through the proxy
        if (isSaveMethod(method)) {
            return handleSaveMethod(getAuthenticationSession(), targetAdapter, targetNoSpec);
        }

        if (isUnderlyingMethod(method)) {
            return getDelegate();
        }

        final ObjectMember objectMember = locateAndCheckMember(method);
        final List<Facet> imperativeFacets = getImperativeFacets(objectMember, method);

        final String memberName = objectMember.getName();

        if (instanceOf(imperativeFacets, DisableForContextFacetViaMethod.class, HideForContextFacetViaMethod.class)) {
            throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'", memberName));
        }

        final String methodName = method.getName();

        if (instanceOf(imperativeFacets, ActionDefaultsFacet.class, PropertyDefaultFacet.class, ActionChoicesFacet.class, ActionParameterChoicesFacet.class, PropertyChoicesFacet.class)) {
            return method.invoke(getDelegate(), args);
        }

        // for all members, check visibility and usability
        checkVisibility(getAuthenticationSession(), targetAdapter, objectMember);

        if (objectMember.isOneToOneAssociation()) {

            if (instanceOf(imperativeFacets, PropertyValidateFacetViaMethod.class, PropertySetterFacetViaModifyMethod.class, PropertyClearFacetViaClearMethod.class)) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only property accessor/mutator", memberName));
            }

            final OneToOneAssociation otoa = (OneToOneAssociation) objectMember;
            if (instanceOf(imperativeFacets, PropertyOrCollectionAccessorFacet.class)) {
                return handleGetterMethodOnProperty(args, targetAdapter, otoa, methodName);
            }
            if (instanceOf(imperativeFacets, PropertySetterFacet.class, PropertyInitializationFacet.class)) {
                checkUsability(getAuthenticationSession(), targetAdapter, objectMember);
                return handleSetterMethodOnProperty(args, getAuthenticationSession(), targetAdapter, otoa, methodName);
            }
        }
        if (objectMember.isOneToManyAssociation()) {

            if (instanceOf(imperativeFacets, CollectionValidateAddToFacetViaMethod.class, CollectionValidateRemoveFromFacetViaMethod.class)) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only collection accessor/mutator", memberName));
            }

            final OneToManyAssociation otma = (OneToManyAssociation) objectMember;
            if (instanceOf(imperativeFacets, PropertyOrCollectionAccessorFacet.class)) {
                return handleGetterMethodOnCollection(method, args, targetAdapter, otma, memberName);
            }
            if (instanceOf(imperativeFacets, CollectionAddToFacet.class)) {
                checkUsability(getAuthenticationSession(), targetAdapter, objectMember);
                return handleCollectionAddToMethod(args, targetAdapter, otma, methodName);
            }
            if (instanceOf(imperativeFacets, CollectionRemoveFromFacet.class)) {
                checkUsability(getAuthenticationSession(), targetAdapter, objectMember);
                return handleCollectionRemoveFromMethod(args, targetAdapter, otma, methodName);
            }
        }

        // filter out
        if (instanceOf(imperativeFacets, PropertyOrCollectionAccessorFacet.class)) {
            throw new UnsupportedOperationException(String.format("Can only invoke accessor on properties or collections; '%s' represents %s", methodName, decode(objectMember)));
        }
        if (instanceOf(imperativeFacets, PropertySetterFacet.class)) {
            throw new UnsupportedOperationException(String.format("Can only invoke mutator on properties; '%s' represents %s", methodName, decode(objectMember)));
        }
        if (instanceOf(imperativeFacets, CollectionAddToFacet.class)) {
            throw new UnsupportedOperationException(String.format("Can only invoke 'adder' on collections; '%s' represents %s", methodName, decode(objectMember)));
        }
        if (instanceOf(imperativeFacets, CollectionRemoveFromFacet.class)) {
            throw new UnsupportedOperationException(String.format("Can only invoke 'remover' on collections; '%s' represents %s", methodName, decode(objectMember)));
        }

        if (objectMember instanceof ObjectAction) {

            if (instanceOf(imperativeFacets, ActionValidationFacetViaMethod.class)) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only the 'invoke' method", memberName));
            }

            checkUsability(getAuthenticationSession(), targetAdapter, objectMember);

            final ObjectAction noa = (ObjectAction) objectMember;
            return handleActionMethod(args, getAuthenticationSession(), targetAdapter, noa, memberName);
        }

        throw new UnsupportedOperationException(String.format("Unknown member type '%s'", objectMember));
    }

    public List<Facet> getImperativeFacets(final ObjectMember objectMember, final Method method) {
        final List<Facet> imperativeFacets = objectMember.getFacets(new Filter<Facet>() {
            @Override
            public boolean accept(final Facet facet) {
                final ImperativeFacet imperativeFacet = asImperativeFacet(facet);
                if (imperativeFacet == null) {
                    return false;
                }
                return imperativeFacet.getMethods().contains(method);
            }

            private ImperativeFacet asImperativeFacet(final Facet facet) {
                if (facet == null) {
                    return null;
                }
                if (facet instanceof ImperativeFacet) {
                    return (ImperativeFacet) facet;
                }
                return asImperativeFacet(facet.getUnderlyingFacet());
            }
        });

        // there will be at least one
        if (imperativeFacets.isEmpty()) {
            throw new IllegalStateException("should be at least one imperative facet");
        }
        return imperativeFacets;
    }

    private static boolean instanceOf(final List<?> objects, final Class<?>... superTypes) {
        for (final Class<?> superType : superTypes) {
            for (final Object obj : objects) {
                if (superType.isAssignableFrom(obj.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    // /////////////////////////////////////////////////////////////////
    // title
    // /////////////////////////////////////////////////////////////////

    private Object handleTitleMethod(final Method method, final Object[] args, final ObjectAdapter targetAdapter) throws IllegalAccessException, InvocationTargetException {

        resolveIfRequired(targetAdapter);

        final ObjectSpecification targetNoSpec = targetAdapter.getSpecification();
        final ObjectTitleContext titleContext = targetNoSpec.createTitleInteractionContext(getAuthenticationSession(), InteractionInvocationMethod.BY_USER, targetAdapter);
        final ObjectTitleEvent titleEvent = titleContext.createInteractionEvent();
        notifyListeners(titleEvent);
        return titleEvent.getTitle();
    }

    // /////////////////////////////////////////////////////////////////
    // save
    // /////////////////////////////////////////////////////////////////

    private Object handleSaveMethod(final AuthenticationSession session, final ObjectAdapter targetAdapter, final ObjectSpecification targetNoSpec) {

        final InteractionResult interactionResult = targetNoSpec.isValidResult(targetAdapter);
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            if (targetAdapter.isTransient()) {
                getObjectPersistor().makePersistent(targetAdapter);
            }
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // property - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnProperty(final Object[] args, final ObjectAdapter targetAdapter, final OneToOneAssociation otoa, final String methodName) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        resolveIfRequired(targetAdapter);

        final ObjectAdapter currentReferencedAdapter = otoa.get(targetAdapter);
        final Object currentReferencedObj = AdapterUtils.unwrap(currentReferencedAdapter);

        final PropertyAccessEvent ev = new PropertyAccessEvent(getDelegate(), otoa.getIdentifier(), currentReferencedObj);
        notifyListeners(ev);
        return currentReferencedObj;
    }

    // /////////////////////////////////////////////////////////////////
    // property - modify
    // /////////////////////////////////////////////////////////////////

    private Object handleSetterMethodOnProperty(final Object[] args, final AuthenticationSession session, final ObjectAdapter targetAdapter, final OneToOneAssociation otoa, final String methodName) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a setter should only have a single argument");
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        final ObjectAdapter argumentAdapter = argumentObj != null ? getAdapterManager().adapterFor(argumentObj) : null;

        final InteractionResult interactionResult = otoa.isAssociationValid(targetAdapter, argumentAdapter).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            otoa.set(targetAdapter, argumentAdapter);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // collection - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnCollection(final Method method, final Object[] args, final ObjectAdapter targetAdapter, final OneToManyAssociation otma, final String memberName) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        resolveIfRequired(targetAdapter);

        final ObjectAdapter currentReferencedAdapter = otma.get(targetAdapter);
        final Object currentReferencedObj = AdapterUtils.unwrap(currentReferencedAdapter);

        final CollectionAccessEvent ev = new CollectionAccessEvent(getDelegate(), otma.getIdentifier());

        if (currentReferencedObj instanceof Collection) {
            final Collection<?> collectionViewObject = lookupViewObject(method, memberName, (Collection<?>) currentReferencedObj, otma);
            notifyListeners(ev);
            return collectionViewObject;
        } else if (currentReferencedObj instanceof Map) {
            final Map<?, ?> mapViewObject = lookupViewObject(method, memberName, (Map<?, ?>) currentReferencedObj, otma);
            notifyListeners(ev);
            return mapViewObject;
        }
        throw new IllegalArgumentException(String.format("Collection type '%s' not supported by framework", currentReferencedObj.getClass().getName()));
    }

    /**
     * Looks up (or creates) a proxy for this object.
     */
    private Collection<?> lookupViewObject(final Method method, final String memberName, final Collection<?> collectionToLookup, final OneToManyAssociation otma) {
        Collection<?> collectionViewObject = collectionViewObjectsByMethod.get(method);
        if (collectionViewObject == null) {
            if (collectionToLookup instanceof WrapperObject) {
                collectionViewObject = collectionToLookup;
            } else {
                collectionViewObject = Proxy.proxy(collectionToLookup, memberName, this, otma);
            }
            collectionViewObjectsByMethod.put(method, collectionViewObject);
        }
        return collectionViewObject;
    }

    private Map<?, ?> lookupViewObject(final Method method, final String memberName, final Map<?, ?> mapToLookup, final OneToManyAssociation otma) {
        Map<?, ?> mapViewObject = mapViewObjectsByMethod.get(method);
        if (mapViewObject == null) {
            if (mapToLookup instanceof WrapperObject) {
                mapViewObject = mapToLookup;
            } else {
                mapViewObject = Proxy.proxy(mapToLookup, memberName, this, otma);
            }
            mapViewObjectsByMethod.put(method, mapViewObject);
        }
        return mapViewObject;
    }

    // /////////////////////////////////////////////////////////////////
    // collection - add to
    // /////////////////////////////////////////////////////////////////

    private Object handleCollectionAddToMethod(final Object[] args, final ObjectAdapter targetAdapter, final OneToManyAssociation otma, final String methodName) {

        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a addTo should only have a single argument");
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to add");
        }
        final ObjectAdapter argumentNO = getAdapterManager().adapterFor(argumentObj);

        final InteractionResult interactionResult = otma.isValidToAdd(targetAdapter, argumentNO).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            otma.addElement(targetAdapter, argumentNO);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // collection - remove from
    // /////////////////////////////////////////////////////////////////

    private Object handleCollectionRemoveFromMethod(final Object[] args, final ObjectAdapter targetAdapter, final OneToManyAssociation otma, final String methodName) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a removeFrom should only have a single argument");
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to remove");
        }
        final ObjectAdapter argumentAdapter = getAdapterManager().adapterFor(argumentObj);

        final InteractionResult interactionResult = otma.isValidToRemove(targetAdapter, argumentAdapter).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            otma.removeElement(targetAdapter, argumentAdapter);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // action
    // /////////////////////////////////////////////////////////////////

    private Object handleActionMethod(final Object[] args, final AuthenticationSession session, final ObjectAdapter targetAdapter, final ObjectAction noa, final String memberName) {

        final Object[] underlyingArgs = new Object[args.length];
        int i = 0;
        for (final Object arg : args) {
            underlyingArgs[i++] = underlying(arg);
        }

        final ObjectAdapter[] argAdapters = new ObjectAdapter[underlyingArgs.length];
        int j = 0;
        for (final Object underlyingArg : underlyingArgs) {
            argAdapters[j++] = underlyingArg != null ? getAdapterManager().adapterFor(underlyingArg) : null;
        }

        final InteractionResult interactionResult = noa.isProposedArgumentSetValid(targetAdapter, argAdapters).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            final ObjectAdapter actionReturnNO = noa.execute(targetAdapter, argAdapters);
            return AdapterUtils.unwrap(actionReturnNO);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    private Object underlying(final Object arg) {
        if (arg instanceof WrapperObject) {
            final WrapperObject argViewObject = (WrapperObject) arg;
            return argViewObject.wrapped();
        } else {
            return arg;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // visibility and usability checks (common to all members)
    // /////////////////////////////////////////////////////////////////

    /**
     * REVIEW: ideally should provide some way to allow to caller to indicate the 'where' context.  Having
     * a hard-coded value like this is an approximation. 
     */
    private final Where where = Where.ANYWHERE;

    private void checkVisibility(final AuthenticationSession session, final ObjectAdapter targetObjectAdapter, final ObjectMember objectMember) {
        final Consent visibleConsent = objectMember.isVisible(getAuthenticationSession(), targetObjectAdapter, where);
        final InteractionResult interactionResult = visibleConsent.getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private void checkUsability(final AuthenticationSession session, final ObjectAdapter targetObjectAdapter, final ObjectMember objectMember) {
        final InteractionResult interactionResult = objectMember.isUsable(getAuthenticationSession(), targetObjectAdapter, where).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    // /////////////////////////////////////////////////////////////////
    // notify listeners
    // /////////////////////////////////////////////////////////////////

    private void notifyListenersAndVetoIfRequired(final InteractionResult interactionResult) {
        final InteractionEvent interactionEvent = interactionResult.getInteractionEvent();
        notifyListeners(interactionEvent);
        if (interactionEvent.isVeto()) {
            throw toException(interactionEvent);
        }
    }

    private String decode(final ObjectMember objectMember) {
        if (objectMember instanceof OneToOneAssociation) {
            return "a property";
        }
        if (objectMember instanceof OneToManyAssociation) {
            return "a collection";
        }
        if (objectMember instanceof ObjectAction) {
            return "an action";
        }
        return "an UNKNOWN member type";
    }

    /**
     * Wraps a {@link InteractionEvent#isVeto() vetoing}
     * {@link InteractionEvent} in a corresponding {@link InteractionException},
     * and returns it.
     */
    private InteractionException toException(final InteractionEvent interactionEvent) {
        if (!interactionEvent.isVeto()) {
            throw new IllegalArgumentException("Provided interactionEvent must be a veto");
        }
        if (interactionEvent instanceof ValidityEvent) {
            final ValidityEvent validityEvent = (ValidityEvent) interactionEvent;
            return new InvalidException(validityEvent);
        }
        if (interactionEvent instanceof VisibilityEvent) {
            final VisibilityEvent visibilityEvent = (VisibilityEvent) interactionEvent;
            return new HiddenException(visibilityEvent);
        }
        if (interactionEvent instanceof UsabilityEvent) {
            final UsabilityEvent usabilityEvent = (UsabilityEvent) interactionEvent;
            return new DisabledException(usabilityEvent);
        }
        throw new IllegalArgumentException("Provided interactionEvent must be a VisibilityEvent, UsabilityEvent or a ValidityEvent");
    }

    // /////////////////////////////////////////////////////////////////
    // switching
    // /////////////////////////////////////////////////////////////////

    private ObjectMember locateAndCheckMember(final Method method) {
        final ObjectSpecificationDefault objectSpecificationDefault = getJavaSpecificationOfOwningClass(method);
        final ObjectMember member = objectSpecificationDefault.getMember(method);
        if (member == null) {
            final String methodName = method.getName();
            throw new UnsupportedOperationException("Method '" + methodName + "' being invoked does not correspond to any of the object's fields or actions.");
        }
        return member;
    }

    protected boolean isTitleMethod(final Method method) {
        return method.equals(titleMethod);
    }

    protected boolean isSaveMethod(final Method method) {
        return method.equals(saveMethod);
    }

    protected boolean isUnderlyingMethod(final Method method) {
        return method.equals(wrappedMethod);
    }

    // /////////////////////////////////////////////////////////////////
    // Specification lookup
    // /////////////////////////////////////////////////////////////////

    private ObjectSpecificationDefault getJavaSpecificationOfOwningClass(final Method method) {
        return getJavaSpecification(method.getDeclaringClass());
    }

    private ObjectSpecificationDefault getJavaSpecification(final Class<?> clazz) {
        final ObjectSpecification nos = getSpecification(clazz);
        if (!(nos instanceof ObjectSpecificationDefault)) {
            throw new UnsupportedOperationException("Only Java is supported (specification is '" + nos.getClass().getCanonicalName() + "')");
        }
        return (ObjectSpecificationDefault) nos;
    }

    private ObjectSpecification getSpecification(final Class<?> type) {
        final ObjectSpecification nos = getSpecificationLookup().loadSpecification(type);
        return nos;
    }

    // /////////////////////////////////////////////////////////////////
    // Dependencies
    // /////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLookup() {
        return specificationLookup;
    }

    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getAuthenticationSessionProvider().getAuthenticationSession();
    }

    protected AdapterManager getAdapterManager() {
        return adapterManager;
    }

    protected ObjectPersistor getObjectPersistor() {
        return objectPersistor;
    }

}
