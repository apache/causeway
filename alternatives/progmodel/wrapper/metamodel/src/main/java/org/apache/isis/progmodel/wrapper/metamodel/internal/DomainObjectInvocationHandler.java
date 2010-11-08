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
import java.util.Map;

import org.apache.isis.applib.events.CollectionAccessEvent;
import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.applib.events.ObjectTitleEvent;
import org.apache.isis.applib.events.PropertyAccessEvent;
import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.util.IsisUtils;
import org.apache.isis.metamodel.spec.JavaSpecification;
import org.apache.isis.progmodel.wrapper.applib.DisabledException;
import org.apache.isis.progmodel.wrapper.applib.HiddenException;
import org.apache.isis.progmodel.wrapper.applib.InteractionException;
import org.apache.isis.progmodel.wrapper.applib.InvalidException;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory;
import org.apache.isis.progmodel.wrapper.applib.WrapperFactory.ExecutionMode;
import org.apache.isis.progmodel.wrapper.applib.WrapperObject;
import org.apache.isis.progmodel.wrapper.metamodel.internal.util.Constants;
import org.apache.isis.progmodel.wrapper.metamodel.internal.util.MethodPrefixFinder;

public class DomainObjectInvocationHandler<T> extends DelegatingInvocationHandlerDefault<T> {

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

    private final Map<Method, Collection<?>> collectionViewObjectsByMethod = new HashMap<Method, Collection<?>>();
    private final Map<Method, Map<?, ?>> mapViewObjectsByMethod = new HashMap<Method, Map<?, ?>>();

    public DomainObjectInvocationHandler(final T delegate, final WrapperFactory embeddedViewer,
        final ExecutionMode mode, final RuntimeContext runtimeContext) {
        super(delegate, embeddedViewer, mode, runtimeContext);
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

        final ObjectAdapter targetAdapter = getRuntimeContext().getAdapterFor(getDelegate());

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
        final String memberName = objectMember.getName();

        final String methodName = method.getName();
        final String prefix = checkPrefix(methodName);

        if (isDefaultMethod(prefix) || isChoicesMethod(prefix)) {
            return method.invoke(getDelegate(), args);
        }

        final boolean isGetterMethod = isGetterMethod(prefix);
        final boolean isSetterMethod = isSetterMethod(prefix);
        final boolean isAddToMethod = isAddToMethod(prefix);
        final boolean isRemoveFromMethod = isRemoveFromMethod(prefix);

        // for all members, check visibility and usability
        checkVisibility(getAuthenticationSession(), targetAdapter, objectMember);

        if (objectMember.isOneToOneAssociation()) {
            final OneToOneAssociation otoa = (OneToOneAssociation) objectMember;
            if (isGetterMethod) {
                return handleGetterMethodOnProperty(args, targetAdapter, otoa, methodName);
            }
            if (isSetterMethod) {
                checkUsability(getAuthenticationSession(), targetAdapter, objectMember);
                return handleSetterMethodOnProperty(args, getAuthenticationSession(), targetAdapter, otoa, methodName);
            }
        }
        if (objectMember.isOneToManyAssociation()) {
            final OneToManyAssociation otma = (OneToManyAssociation) objectMember;
            if (isGetterMethod) {
                return handleGetterMethodOnCollection(method, args, targetAdapter, otma, memberName);
            }
            if (isAddToMethod) {
                checkUsability(getAuthenticationSession(), targetAdapter, objectMember);
                return handleCollectionAddToMethod(args, targetAdapter, otma, methodName);
            }
            if (isRemoveFromMethod) {
                checkUsability(getAuthenticationSession(), targetAdapter, objectMember);
                return handleCollectionRemoveFromMethod(args, targetAdapter, otma, methodName);
            }
        }

        // filter out
        if (isGetterMethod) {
            throw new UnsupportedOperationException(String.format(
                "Can only invoke 'get' on properties or collections; '%s' represents %s", methodName,
                decode(objectMember)));
        }
        if (isSetterMethod) {
            throw new UnsupportedOperationException(String.format(
                "Can only invoke 'set' on properties; '%s' represents %s", methodName, decode(objectMember)));
        }
        if (isAddToMethod) {
            throw new UnsupportedOperationException(String.format(
                "Can only invoke 'addTo' on collections; '%s' represents %s", methodName, decode(objectMember)));
        }
        if (isRemoveFromMethod) {
            throw new UnsupportedOperationException(String.format(
                "Can only invoke 'removeFrom' on collections; '%s' represents %s", methodName, decode(objectMember)));
        }

        if (objectMember instanceof ObjectAction) {
            checkUsability(getAuthenticationSession(), targetAdapter, objectMember);

            final ObjectAction noa = (ObjectAction) objectMember;
            return handleActionMethod(args, getAuthenticationSession(), targetAdapter, noa, memberName);
        }

        throw new UnsupportedOperationException(String.format("Unknown member type '%s'", objectMember));
    }

    // /////////////////////////////////////////////////////////////////
    // title
    // /////////////////////////////////////////////////////////////////

    private Object handleTitleMethod(final Method method, final Object[] args, final ObjectAdapter targetAdapter)
        throws IllegalAccessException, InvocationTargetException {

        resolveIfRequired(targetAdapter);

        final ObjectSpecification targetNoSpec = targetAdapter.getSpecification();
        final ObjectTitleContext titleContext =
            targetNoSpec.createTitleInteractionContext(getAuthenticationSession(), InteractionInvocationMethod.BY_USER,
                targetAdapter);
        final ObjectTitleEvent titleEvent = titleContext.createInteractionEvent();
        notifyListeners(titleEvent);
        return titleEvent.getTitle();
    }

    // /////////////////////////////////////////////////////////////////
    // save
    // /////////////////////////////////////////////////////////////////

    private Object handleSaveMethod(final AuthenticationSession session, final ObjectAdapter targetAdapter,
        final ObjectSpecification targetNoSpec) {

        final InteractionResult interactionResult = targetNoSpec.isValidResult(targetAdapter);
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            if (targetAdapter.isTransient()) {
                getRuntimeContext().makePersistent(targetAdapter);
            }
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // property - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnProperty(final Object[] args, final ObjectAdapter targetAdapter,
        final OneToOneAssociation otoa, final String methodName) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        resolveIfRequired(targetAdapter);

        final ObjectAdapter currentReferencedAdapter = otoa.get(targetAdapter);
        final Object currentReferencedObj = IsisUtils.unwrap(currentReferencedAdapter);

        final PropertyAccessEvent ev =
            new PropertyAccessEvent(getDelegate(), otoa.getIdentifier(), currentReferencedObj);
        notifyListeners(ev);
        return currentReferencedObj;
    }

    // /////////////////////////////////////////////////////////////////
    // property - modify
    // /////////////////////////////////////////////////////////////////

    private Object handleSetterMethodOnProperty(final Object[] args, final AuthenticationSession session,
        final ObjectAdapter targetAdapter, final OneToOneAssociation otoa, final String methodName) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a setter should only have a single argument");
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        final ObjectAdapter argumentNO = argumentObj != null ? getRuntimeContext().adapterFor(argumentObj) : null;

        final InteractionResult interactionResult =
            otoa.isAssociationValid(targetAdapter, argumentNO).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            if (argumentNO != null) {
                otoa.setAssociation(targetAdapter, argumentNO); // need to wrap arg
            } else {
                otoa.clearAssociation(targetAdapter);
            }
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // collection - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnCollection(final Method method, final Object[] args,
        final ObjectAdapter targetAdapter, final OneToManyAssociation otma, final String memberName) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        resolveIfRequired(targetAdapter);

        final ObjectAdapter currentReferencedAdapter = otma.get(targetAdapter);
        final Object currentReferencedObj = IsisUtils.unwrap(currentReferencedAdapter);

        final CollectionAccessEvent ev = new CollectionAccessEvent(getDelegate(), otma.getIdentifier());

        if (currentReferencedObj instanceof Collection) {
            final Collection<?> collectionViewObject =
                lookupViewObject(method, memberName, (Collection<?>) currentReferencedObj, otma);
            notifyListeners(ev);
            return collectionViewObject;
        } else if (currentReferencedObj instanceof Map) {
            final Map<?, ?> mapViewObject =
                lookupViewObject(method, memberName, (Map<?, ?>) currentReferencedObj, otma);
            notifyListeners(ev);
            return mapViewObject;
        }
        throw new IllegalArgumentException(String.format("Collection type '%s' not supported by framework",
            currentReferencedObj.getClass().getName()));
    }

    /**
     * Looks up (or creates) a proxy for this object.
     */
    private Collection<?> lookupViewObject(final Method method, final String memberName,
        final Collection<?> collectionToLookup, final OneToManyAssociation otma) {
        Collection<?> collectionViewObject = collectionViewObjectsByMethod.get(method);
        if (collectionViewObject == null) {
            if (collectionToLookup instanceof WrapperObject) {
                collectionViewObject = collectionToLookup;
            } else {
                collectionViewObject = Proxy.proxy(collectionToLookup, memberName, this, getRuntimeContext(), otma);
            }
            collectionViewObjectsByMethod.put(method, collectionViewObject);
        }
        return collectionViewObject;
    }

    private Map<?, ?> lookupViewObject(final Method method, final String memberName, final Map<?, ?> mapToLookup,
        final OneToManyAssociation otma) {
        Map<?, ?> mapViewObject = mapViewObjectsByMethod.get(method);
        if (mapViewObject == null) {
            if (mapToLookup instanceof WrapperObject) {
                mapViewObject = mapToLookup;
            } else {
                mapViewObject = Proxy.proxy(mapToLookup, memberName, this, getRuntimeContext(), otma);
            }
            mapViewObjectsByMethod.put(method, mapViewObject);
        }
        return mapViewObject;
    }

    // /////////////////////////////////////////////////////////////////
    // collection - add to
    // /////////////////////////////////////////////////////////////////

    private Object handleCollectionAddToMethod(final Object[] args, final ObjectAdapter targetAdapter,
        final OneToManyAssociation otma, final String methodName) {

        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a addTo should only have a single argument");
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to add");
        }
        final ObjectAdapter argumentNO = getRuntimeContext().adapterFor(argumentObj);

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

    private Object handleCollectionRemoveFromMethod(final Object[] args, final ObjectAdapter targetAdapter,
        final OneToManyAssociation otma, final String methodName) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a removeFrom should only have a single argument");
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to remove");
        }
        final ObjectAdapter argumentAdapter = getRuntimeContext().adapterFor(argumentObj);

        final InteractionResult interactionResult =
            otma.isValidToRemove(targetAdapter, argumentAdapter).getInteractionResult();
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

    private Object handleActionMethod(final Object[] args, final AuthenticationSession session,
        final ObjectAdapter targetAdapter, final ObjectAction noa, final String memberName) {

        final Object[] underlyingArgs = new Object[args.length];
        int i = 0;
        for (final Object arg : args) {
            underlyingArgs[i++] = underlying(arg);
        }

        final ObjectAdapter[] argAdapters = new ObjectAdapter[underlyingArgs.length];
        int j = 0;
        for (final Object underlyingArg : underlyingArgs) {
            argAdapters[j++] = underlyingArg != null ? getRuntimeContext().adapterFor(underlyingArg) : null;
        }

        final InteractionResult interactionResult =
            noa.isProposedArgumentSetValid(targetAdapter, argAdapters).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);

        if (getExecutionMode() == ExecutionMode.EXECUTE) {
            final ObjectAdapter actionReturnNO = noa.execute(targetAdapter, argAdapters);
            return IsisUtils.unwrap(actionReturnNO);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    private Object underlying(final Object arg) {
        if (arg instanceof WrapperObject<?>) {
            final WrapperObject<?> argViewObject = (WrapperObject<?>) arg;
            return argViewObject.wrapped();
        } else {
            return arg;
        }
    }

    // /////////////////////////////////////////////////////////////////
    // visibility and usability checks (common to all members)
    // /////////////////////////////////////////////////////////////////

    private void checkVisibility(final AuthenticationSession session, final ObjectAdapter targetObjectAdapter,
        final ObjectMember objectMember) {
        final InteractionResult interactionResult =
            objectMember.isVisible(getAuthenticationSession(), targetObjectAdapter).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private void checkUsability(final AuthenticationSession session, final ObjectAdapter targetObjectAdapter,
        final ObjectMember objectMember) {
        final InteractionResult interactionResult =
            objectMember.isUsable(getAuthenticationSession(), targetObjectAdapter).getInteractionResult();
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
     * Wraps a {@link InteractionEvent#isVeto() vetoing} {@link InteractionEvent} in a corresponding
     * {@link InteractionException}, and returns it.
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
        throw new IllegalArgumentException(
            "Provided interactionEvent must be a VisibilityEvent, UsabilityEvent or a ValidityEvent");
    }

    // /////////////////////////////////////////////////////////////////
    // switching
    // /////////////////////////////////////////////////////////////////

    private ObjectMember locateAndCheckMember(final Method method) {
        final JavaSpecification javaSpecification = getJavaSpecificationOfOwningClass(method);
        final ObjectMember member = javaSpecification.getMember(method);
        if (member == null) {
            final String methodName = method.getName();
            throw new UnsupportedOperationException("Method '" + methodName
                + "' being invoked does not correspond to any of the object's fields or actions.");
        }
        return member;
    }

    private String checkPrefix(final String methodName) {
        final String prefix = new MethodPrefixFinder().findPrefix(methodName);
        if (StringUtils.in(prefix, Constants.INVALID_PREFIXES)) {
            throw new UnsupportedOperationException(String.format(
                "Cannot invoke methods with prefix '%s'; use only get/set/addTo/removeFrom/action", prefix));
        }
        return prefix;
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

    private boolean isGetterMethod(final String prefix) {
        return prefix.equals(Constants.PREFIX_GET);
    }

    private boolean isSetterMethod(final String prefix) {
        return prefix.equals(Constants.PREFIX_SET);
    }

    private boolean isAddToMethod(final String prefix) {
        return prefix.equals(Constants.PREFIX_ADD_TO);
    }

    private boolean isRemoveFromMethod(final String prefix) {
        return prefix.equals(Constants.PREFIX_REMOVE_FROM);
    }

    private boolean isChoicesMethod(final String prefix) {
        return prefix.equals(Constants.PREFIX_CHOICES);
    }

    private boolean isDefaultMethod(final String prefix) {
        return prefix.equals(Constants.PREFIX_DEFAULT);
    }

    // /////////////////////////////////////////////////////////////////
    // Specification lookup
    // /////////////////////////////////////////////////////////////////

    private JavaSpecification getJavaSpecificationOfOwningClass(final Method method) {
        return getJavaSpecification(method.getDeclaringClass());
    }

    private JavaSpecification getJavaSpecification(final Class<?> clazz) {
        final ObjectSpecification nos = getSpecification(clazz);
        if (!(nos instanceof JavaSpecification)) {
            throw new UnsupportedOperationException("Only Java is supported (specification is '"
                + nos.getClass().getCanonicalName() + "')");
        }
        return (JavaSpecification) nos;
    }

    private ObjectSpecification getSpecification(final Class<?> type) {
        final ObjectSpecification nos = getSpecificationLoader().loadSpecification(type);
        return nos;
    }

}
