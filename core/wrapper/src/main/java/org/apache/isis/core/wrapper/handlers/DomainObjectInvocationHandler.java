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

package org.apache.isis.core.wrapper.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.events.CollectionAccessEvent;
import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.applib.events.ObjectTitleEvent;
import org.apache.isis.applib.events.PropertyAccessEvent;
import org.apache.isis.applib.events.UsabilityEvent;
import org.apache.isis.applib.events.ValidityEvent;
import org.apache.isis.applib.events.VisibilityEvent;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.HiddenException;
import org.apache.isis.applib.services.wrapper.InteractionException;
import org.apache.isis.applib.services.wrapper.InvalidException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.applib.services.wrapper.WrapperObject;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectPersistor;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.ImperativeFacet.Intent;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ContributeeMember;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionContributee;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;

public class DomainObjectInvocationHandler<T> extends DelegatingInvocationHandlerDefault<T> {

    private final Map<Method, Collection<?>> collectionViewObjectsByMethod = new HashMap<Method, Collection<?>>();
    private final Map<Method, Map<?, ?>> mapViewObjectsByMethod = new HashMap<Method, Map<?, ?>>();

    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final SpecificationLoader specificationLookup;
    private final AdapterManager adapterManager;
    private final ObjectPersistor objectPersistor;

    private final ProxyContextHandler proxy;
    private final ExecutionMode executionMode;

    /**
     * The <tt>title()</tt> method; may be <tt>null</tt>.
     */
    protected Method titleMethod;

    /**
     * The <tt>__isis_save()</tt> method from {@link WrapperObject#__isis_save()}.
     */
    protected Method __isis_saveMethod;

    /**
     * The <tt>save()</tt> method from {@link WrapperObject#save()}.
     */
    @Deprecated
    protected Method saveMethod;

    /**
     * The <tt>__isis_wrapped()</tt> method from {@link WrapperObject#__isis_wrapped()}.
     */
    protected Method __isis_wrappedMethod;

    /**
     * The <tt>wrapped()</tt> method from {@link WrapperObject#wrapped()}.
     */
    @Deprecated
    protected Method wrappedMethod;

    /**
     * The <tt>__isis_executionMode()</tt> method from {@link WrapperObject#__isis_executionMode()}.
     */
    protected Method __isis_executionMode;

    public DomainObjectInvocationHandler(
            final T delegate,
            final WrapperFactory wrapperFactory,
            final ExecutionMode mode,
            final AuthenticationSessionProvider authenticationSessionProvider,
            final SpecificationLoader specificationLookup,
            final AdapterManager adapterManager,
            final ObjectPersistor objectPersistor,
            final ProxyContextHandler proxy) {
        super(delegate, wrapperFactory, mode);

        this.proxy = proxy;
        this.authenticationSessionProvider = authenticationSessionProvider;
        this.specificationLookup = specificationLookup;
        this.adapterManager = adapterManager;
        this.objectPersistor = objectPersistor;
        this.executionMode = mode;

        try {
            titleMethod = delegate.getClass().getMethod("title", new Class[]{});
        } catch (final NoSuchMethodException e) {
            // ignore
        }
        try {
            __isis_saveMethod = WrapperObject.class.getMethod("__isis_save", new Class[]{});
            __isis_wrappedMethod = WrapperObject.class.getMethod("__isis_wrapped", new Class[]{});
            __isis_executionMode = WrapperObject.class.getMethod("__isis_executionMode", new Class[]{});
            saveMethod = WrapperObject.class.getMethod("save", new Class[] {});
            wrappedMethod = WrapperObject.class.getMethod("wrapped", new Class[] {});
        } catch (final NoSuchMethodException nsme) {
            throw new IllegalStateException(
                    "Could not locate reserved declared methods in the WrappingObject and WrappedObject interfaces",
                    nsme);
        }
    }

    @Override
    public Object invoke(final Object proxyObject, final Method method, final Object[] args) throws Throwable {

        if (isObjectMethod(method)) {
            return delegate(method, args);
        }

        // workaround for JDO-enhanced..
        if(isJdoMethod(method)) {
            return delegate(method, args);
        }

        if(isInjectMethod(method)) {
            return delegate(method, args);
        }

        final ObjectAdapter targetAdapter = adapterFor(getDelegate());

        if (isTitleMethod(method)) {
            return handleTitleMethod(targetAdapter);
        }


        final ObjectSpecification targetNoSpec = targetAdapter.getSpecification();

        // save method, through the proxy
        if (isSaveMethod(method)) {
            return handleSaveMethod(targetAdapter, targetNoSpec);
        }

        if (isWrappedMethod(method)) {
            return getDelegate();
        }

        if (isExecutionModeMethod(method)) {
            return executionMode;
        }

        final ObjectMember objectMember = locateAndCheckMember(method);
        final ContributeeMember contributeeMember = determineIfContributed(args, objectMember);

        final String memberName = objectMember.getName();

        final Intent intent = ImperativeFacet.Util.getIntent(objectMember, method);
        if(intent == Intent.CHECK_IF_HIDDEN || intent == Intent.CHECK_IF_DISABLED) {
            throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'", memberName));
        }

        final String methodName = method.getName();

        if (intent == Intent.DEFAULTS || intent == Intent.CHOICES_OR_AUTOCOMPLETE) {
            return method.invoke(getDelegate(), args);
        }

        if (objectMember.isOneToOneAssociation()) {

            if (intent == Intent.CHECK_IF_VALID || intent == Intent.MODIFY_PROPERTY_SUPPORTING) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method for '%s'; use only property accessor/mutator", memberName));
            }

            final OneToOneAssociation otoa = (OneToOneAssociation) objectMember;
            
            if (intent == Intent.ACCESSOR) {
                return handleGetterMethodOnProperty(targetAdapter, args, otoa);
            }
            
            if (intent == Intent.MODIFY_PROPERTY || intent == Intent.INITIALIZATION) {
                return handleSetterMethodOnProperty(targetAdapter, args, otoa);
            }
        }
        if (objectMember.isOneToManyAssociation()) {

            if (intent == Intent.CHECK_IF_VALID) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only collection accessor/mutator", memberName));
            }

            final OneToManyAssociation otma = (OneToManyAssociation) objectMember;
            if (intent == Intent.ACCESSOR) {
                return handleGetterMethodOnCollection(targetAdapter, args, otma, method, memberName);
            }
            if (intent == Intent.MODIFY_COLLECTION_ADD) {
                return handleCollectionAddToMethod(targetAdapter, args, otma);
            }
            if (intent == Intent.MODIFY_COLLECTION_REMOVE) {
                return handleCollectionRemoveFromMethod(targetAdapter, args, otma);
            }
        }

        if (objectMember instanceof ObjectAction) {

            if (intent == Intent.CHECK_IF_VALID) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only the 'invoke' method", memberName));
            }

            final ObjectAction noa = (ObjectAction) objectMember;
            return handleActionMethod(targetAdapter, args, noa, contributeeMember);
        }

        throw new UnsupportedOperationException(String.format("Unknown member type '%s'", objectMember));
    }

    // see if this is a contributed property/collection/action
    private ContributeeMember determineIfContributed(
            final Object[] args,
            final ObjectMember objectMember) {

        if (!(objectMember instanceof ObjectAction)) {
            return null;
        }

        final ObjectAction objectAction = (ObjectAction) objectMember;

        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            if(arg == null) {
                continue;
            }
            final ObjectSpecificationDefault objectSpec = getJavaSpecification(arg.getClass());

            if(args.length == 1) {
                // is this a contributed property/collection?
                final List<ObjectAssociation> associations =
                        objectSpec.getAssociations(Contributed.INCLUDED);
                for (final ObjectAssociation association : associations) {
                    if(association instanceof ContributeeMember) {
                        final ContributeeMember contributeeMember = (ContributeeMember) association;
                        if(contributeeMember.isContributedBy(objectAction)) {
                            return contributeeMember;
                        }
                    }
                }
            }

            // is this a contributed action?
            final List<ObjectAction> actions =
                    objectSpec.getObjectActions(Contributed.INCLUDED);
            for (final ObjectAction action : actions) {
                if(action instanceof ContributeeMember) {
                    final ContributeeMember contributeeMember = (ContributeeMember) action;
                    if(contributeeMember.isContributedBy(objectAction)) {
                        return contributeeMember;
                    }
                }
            }
        }

        return null;
    }

    private boolean isJdoMethod(final Method method) {
        return methodStartsWith(method, "jdo");
    }

    private boolean isInjectMethod(final Method method) {
        return methodStartsWith(method, "inject");
    }

    private boolean methodStartsWith(final Method method, final String prefix) {
        return method.getName().startsWith(prefix);
    }

    // /////////////////////////////////////////////////////////////////
    // title
    // /////////////////////////////////////////////////////////////////

    private Object handleTitleMethod(final ObjectAdapter targetAdapter)
            throws IllegalAccessException, InvocationTargetException {

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

    private Object handleSaveMethod(
            final ObjectAdapter targetAdapter, final ObjectSpecification targetNoSpec) {

        if(getExecutionMode().shouldEnforceRules()) {
            final InteractionResult interactionResult = targetNoSpec.isValidResult(targetAdapter);
            notifyListenersAndVetoIfRequired(interactionResult);
        }

        if (getExecutionMode().shouldExecute()) {
            if (targetAdapter.isTransient()) {
                getObjectPersistor().makePersistent(targetAdapter);
            }
        }
        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // property - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnProperty(
            final ObjectAdapter targetAdapter,
            final Object[] args,
            final OneToOneAssociation otoa) {

        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        if(getExecutionMode().shouldEnforceRules()) {
            checkVisibility(targetAdapter, otoa);
        }

        resolveIfRequired(targetAdapter);

        final ObjectAdapter currentReferencedAdapter = otoa.get(targetAdapter);
        final Object currentReferencedObj = ObjectAdapter.Util.unwrap(currentReferencedAdapter);

        final PropertyAccessEvent ev = new PropertyAccessEvent(getDelegate(), otoa.getIdentifier(), currentReferencedObj);
        notifyListeners(ev);
        return currentReferencedObj;
    }

    // /////////////////////////////////////////////////////////////////
    // property - modify
    // /////////////////////////////////////////////////////////////////

    private Object handleSetterMethodOnProperty(
            final ObjectAdapter targetAdapter, final Object[] args,
            final OneToOneAssociation otoa) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a setter should only have a single argument");
        }

        final Object argumentObj = underlying(args[0]);

        if(getExecutionMode().shouldEnforceRules()) {
            checkVisibility(targetAdapter, otoa);
            checkUsability(targetAdapter, otoa);
        }

        final ObjectAdapter argumentAdapter = argumentObj != null ? adapterFor(argumentObj) : null;

        resolveIfRequired(targetAdapter);


        if(getExecutionMode().shouldEnforceRules()) {
            final InteractionResult interactionResult = otoa.isAssociationValid(targetAdapter, argumentAdapter).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        }

        if (getExecutionMode().shouldExecute()) {
            otoa.set(targetAdapter, argumentAdapter);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }


    // /////////////////////////////////////////////////////////////////
    // collection - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnCollection(
            final ObjectAdapter targetAdapter,
            final Object[] args,
            final OneToManyAssociation otma,
            final Method method,
            final String memberName) {

        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        if(getExecutionMode().shouldEnforceRules()) {
            checkVisibility(targetAdapter, otma);
        }

        resolveIfRequired(targetAdapter);

        final ObjectAdapter currentReferencedAdapter = otma.get(targetAdapter);
        final Object currentReferencedObj = ObjectAdapter.Util.unwrap(currentReferencedAdapter);

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
                collectionViewObject = proxy.proxy(collectionToLookup, memberName, this, otma);
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
                mapViewObject = proxy.proxy(mapToLookup, memberName, this, otma);
            }
            mapViewObjectsByMethod.put(method, mapViewObject);
        }
        return mapViewObject;
    }

    // /////////////////////////////////////////////////////////////////
    // collection - add to
    // /////////////////////////////////////////////////////////////////

    private Object handleCollectionAddToMethod(
            final ObjectAdapter targetAdapter,
            final Object[] args,
            final OneToManyAssociation otma) {

        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a addTo should only have a single argument");
        }

        if(getExecutionMode().shouldEnforceRules()) {
            checkVisibility(targetAdapter, otma);
            checkUsability(targetAdapter, otma);
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to add");
        }
        final ObjectAdapter argumentNO = adapterFor(argumentObj);

        if(getExecutionMode().shouldEnforceRules()) {
            final InteractionResult interactionResult = otma.isValidToAdd(targetAdapter, argumentNO).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        }

        if (getExecutionMode().shouldExecute()) {
            otma.addElement(targetAdapter, argumentNO);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }


    // /////////////////////////////////////////////////////////////////
    // collection - remove from
    // /////////////////////////////////////////////////////////////////

    private Object handleCollectionRemoveFromMethod(
            final ObjectAdapter targetAdapter,
            final Object[] args,
            final OneToManyAssociation otma) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a removeFrom should only have a single argument");
        }

        if(getExecutionMode().shouldEnforceRules()) {
            checkVisibility(targetAdapter, otma);
            checkUsability(targetAdapter, otma);
        }


        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to remove");
        }
        final ObjectAdapter argumentAdapter = adapterFor(argumentObj);

        if(getExecutionMode().shouldEnforceRules()) {
            final InteractionResult interactionResult = otma.isValidToRemove(targetAdapter, argumentAdapter).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        }

        if (getExecutionMode().shouldExecute()) {
            otma.removeElement(targetAdapter, argumentAdapter);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    // /////////////////////////////////////////////////////////////////
    // action
    // /////////////////////////////////////////////////////////////////

    private Object handleActionMethod(
            final ObjectAdapter targetAdapter, final Object[] args,
            final ObjectAction objectAction,
            final ContributeeMember contributeeMember) {

        final ObjectAdapter contributeeAdapter;
        final Object[] contributeeArgs;
        if(contributeeMember != null) {
            final int contributeeParamPosition = contributeeMember.getContributeeParamPosition();
            final Object contributee = args[contributeeParamPosition];
            contributeeAdapter = adapterFor(contributee);

            final List<Object> argCopy = Lists.newArrayList(args);
            argCopy.remove(contributeeParamPosition);
            contributeeArgs = argCopy.toArray();
        } else {
            contributeeAdapter = null;
            contributeeArgs = null;
        }

        if(getExecutionMode().shouldEnforceRules()) {
            if(contributeeMember != null) {
                checkVisibility(contributeeAdapter, contributeeMember);
                checkUsability(contributeeAdapter, contributeeMember);
            } else {
                checkVisibility(targetAdapter, objectAction);
                checkUsability(targetAdapter, objectAction);
            }
        }

        final ObjectAdapter[] argAdapters = asObjectAdaptersUnderlying(args);

        if(getExecutionMode().shouldEnforceRules()) {
            if(contributeeMember != null) {
                if(contributeeMember instanceof ObjectActionContributee) {
                    final ObjectActionContributee objectActionContributee = (ObjectActionContributee) contributeeMember;
                    final ObjectAdapter[] contributeeArgAdapters = asObjectAdaptersUnderlying(contributeeArgs);

                    checkValidity(contributeeAdapter, objectActionContributee, contributeeArgAdapters);
                }
                // nothing to do for contributed properties or collections
            } else {
                checkValidity(targetAdapter, objectAction, argAdapters);
            }
        }

        if (getExecutionMode().shouldExecute()) {
            final ObjectAdapter actionReturnNO = objectAction.execute(targetAdapter, argAdapters);
            return ObjectAdapter.Util.unwrap(actionReturnNO);
        }

        objectChangedIfRequired(targetAdapter);

        return null;
    }

    private void checkValidity(final ObjectAdapter targetAdapter, final ObjectAction objectAction, final ObjectAdapter[] argAdapters) {
        final InteractionResult interactionResult = objectAction.isProposedArgumentSetValid(targetAdapter, argAdapters).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private ObjectAdapter[] asObjectAdaptersUnderlying(final Object[] args) {

        final ObjectAdapter[] argAdapters = new ObjectAdapter[args.length];
        int i = 0;
        for (final Object arg : args) {
            argAdapters[i++] = adapterFor(underlying(arg));
        }

        return argAdapters;
    }

    private ObjectAdapter adapterFor(final Object obj) {
        return obj != null ? getAdapterManager().adapterFor(obj) : null;
    }

    private Object underlying(final Object arg) {
        if (arg instanceof WrappingObject) {
            final WrappingObject argViewObject = (WrappingObject) arg;
            return argViewObject.__isis_wrapped();
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

    private void checkVisibility(
            final ObjectAdapter targetObjectAdapter,
            final ObjectMember objectMember) {
        final Consent visibleConsent = objectMember.isVisible(getAuthenticationSession(), targetObjectAdapter, where);
        final InteractionResult interactionResult = visibleConsent.getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private void checkUsability(
            final ObjectAdapter targetObjectAdapter,
            final ObjectMember objectMember) {
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
        return method.equals(saveMethod) || method.equals(__isis_saveMethod);
    }

    protected boolean isWrappedMethod(final Method method) {
        return method.equals(wrappedMethod) || method.equals(__isis_wrappedMethod);
    }

    protected boolean isExecutionModeMethod(final Method method) {
        return method.equals(__isis_executionMode);
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
