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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.HiddenException;
import org.apache.isis.applib.services.wrapper.InteractionException;
import org.apache.isis.applib.services.wrapper.InvalidException;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.applib.services.wrapper.events.CollectionAccessEvent;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.events.ObjectTitleEvent;
import org.apache.isis.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.IsisJdoMetamodelPlugin;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.ImperativeFacet.Intent;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.interactions.ObjectTitleContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ContributeeMember;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionContributee;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;

import lombok.val;

public class DomainObjectInvocationHandler<T> extends DelegatingInvocationHandlerDefault<T> {

    private final AuthenticationSessionProvider authenticationSessionProvider;
    private final ObjectAdapterProvider objectAdapterProvider;

    private final ProxyContextHandler proxy;
    private final ExecutionMode executionMode;
    private final IsisSessionFactory isisSessionFactory;

    /**
     * The <tt>title()</tt> method; may be <tt>null</tt>.
     */
    protected Method titleMethod;

    /**
     * The <tt>__isis_save()</tt> method from {@link WrappingObject#__isis_save()}.
     */
    protected Method __isis_saveMethod;

    /**
     * The <tt>__isis_wrapped()</tt> method from {@link WrappingObject#__isis_wrapped()}.
     */
    protected Method __isis_wrappedMethod;

    /**
     * The <tt>__isis_executionMode()</tt> method from {@link WrappingObject#__isis_executionMode()}.
     */
    protected Method __isis_executionMode;

    protected final Set<String> jdoMethodsProvidedByEnhancement = _Sets.newHashSet();

    public DomainObjectInvocationHandler(
            final T delegate,
            final ExecutionMode mode,
            final ProxyContextHandler proxy,
            final IsisSessionFactory isisSessionFactory) {
        super(delegate, mode);

        this.proxy = proxy;
        this.executionMode = mode;
        this.isisSessionFactory = isisSessionFactory;

        final MetaModelContext context = MetaModelContext.current();
        
        this.authenticationSessionProvider = context.getAuthenticationSessionProvider(); 
        this.objectAdapterProvider = context.getObjectAdapterProvider();


        try {
            titleMethod = delegate.getClass().getMethod("title", new Class[]{});
        } catch (final NoSuchMethodException e) {
            // ignore
        }
        try {
            __isis_saveMethod = WrappingObject.class.getMethod("__isis_save", new Class[]{});
            __isis_wrappedMethod = WrappingObject.class.getMethod("__isis_wrapped", new Class[]{});
            __isis_executionMode = WrappingObject.class.getMethod("__isis_executionMode", new Class[]{});

            _NullSafe.stream(IsisJdoMetamodelPlugin.get().getMethodsProvidedByEnhancement())
            .map(Method::getName)
            .forEach(jdoMethodsProvidedByEnhancement::add);

            // legacy of ...
            //            dnPersistableMethods.addAll(
            //                    _Lists.newArrayList(
            //                            Iterables.transform(
            //                                    Arrays.asList(Persistable.class.getDeclaredMethods()),
            //                                    new Function<Method, String>() {
            //                                        @Override
            //                                        public String apply(final Method input) {
            //                                            return input.getName();
            //                                        }
            //                                    })));

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

            final ObjectAction objectAction = (ObjectAction) objectMember;

            ObjectAction actualObjectAction;
            ObjectAdapter actualTargetAdapter;

            final MixinFacet mixinFacet = targetAdapter.getSpecification().getFacet(MixinFacet.class);
            if(mixinFacet != null) {

                // rather than invoke on a (transient) mixin, instead try to
                // figure out the corresponding ObjectActionMixedIn
                actualTargetAdapter = mixinFacet.mixedIn(targetAdapter, MixinFacet.Policy.IGNORE_FAILURES);
                actualObjectAction = determineMixinAction(actualTargetAdapter, objectAction);

                if(actualTargetAdapter == null || actualObjectAction == null) {
                    // revert to original behaviour
                    actualTargetAdapter = targetAdapter;
                    actualObjectAction = objectAction;
                }
            } else {
                actualTargetAdapter = targetAdapter;
                actualObjectAction = objectAction;
            }

            return handleActionMethod(actualTargetAdapter, args, actualObjectAction, contributeeMember);
        }

        throw new UnsupportedOperationException(String.format("Unknown member type '%s'", objectMember));
    }

    private static ObjectAction determineMixinAction(
            final ObjectAdapter domainObjectAdapter,
            final ObjectAction objectAction) {
        if(domainObjectAdapter == null) {
            return null;
        }
        final ObjectSpecification specification = domainObjectAdapter.getSpecification();
        final Stream<ObjectAction> objectActions = specification.streamObjectActions(Contributed.INCLUDED);
        
        return objectActions
            .filter(action->action instanceof ObjectActionMixedIn)
            .map(action->(ObjectActionMixedIn) action)
            .filter(mixedInAction->mixedInAction.hasMixinAction(objectAction))
            .findFirst()
            .orElse(null);
        
        // throw new RuntimeException("Unable to find the mixed-in action corresponding to " + objectAction.getIdentifier().toFullIdentityString());
    }

    public InteractionInitiatedBy getInteractionInitiatedBy() {
        return getExecutionMode().shouldEnforceRules()? InteractionInitiatedBy.USER: InteractionInitiatedBy.FRAMEWORK;
    }

    // see if this is a contributed property/collection/action
    private ContributeeMember determineIfContributed(
            final Object[] args,
            final ObjectMember objectMember) {

        if (!(objectMember instanceof ObjectAction)) {
            return null;
        }

        final ObjectAction objectAction = (ObjectAction) objectMember;

        for (final Object arg : args) {
            if (arg == null) {
                continue;
            }
            final ObjectSpecificationDefault objectSpec = getJavaSpecification(arg.getClass());

            if (args.length == 1) {
                // is this a contributed property/collection?
                final Stream<ObjectAssociation> associations =
                        objectSpec.streamAssociations(Contributed.INCLUDED);
                
                
                final Optional<ContributeeMember> contributeeMember = associations
                .filter(association->association instanceof ContributeeMember)
                .map(association->(ContributeeMember) association)
                .filter(contributeeMember1->contributeeMember1.isContributedBy(objectAction))
                .findAny();
                
                if(contributeeMember.isPresent()) {
                    return contributeeMember.get();
                }
            }

            // is this a contributed action?
            {
                final Stream<ObjectAction> actions =
                        objectSpec.streamObjectActions(Contributed.INCLUDED);
                
                final Optional<ContributeeMember> contributeeMember = actions
                        .filter(action->action instanceof ContributeeMember)
                        .map(action->(ContributeeMember) action)
                        .filter(contributeeMember1->contributeeMember1.isContributedBy(objectAction))
                        .findAny();
                
                if(contributeeMember.isPresent()) {
                    return contributeeMember.get();
                }
            }
            
        }

        return null;
    }

    private boolean isJdoMethod(final Method method) {
        return methodStartsWith(method, "jdo") || jdoMethodsProvidedByEnhancement.contains(method.getName());
    }

    private static boolean isInjectMethod(final Method method) {
        return methodStartsWith(method, "inject");
    }

    private static boolean methodStartsWith(final Method method, final String prefix) {
        return method.getName().startsWith(prefix);
    }

    // /////////////////////////////////////////////////////////////////
    // title
    // /////////////////////////////////////////////////////////////////

    private Object handleTitleMethod(final ObjectAdapter targetAdapter)
            throws IllegalAccessException, InvocationTargetException {

        resolveIfRequired(targetAdapter);

        final ObjectSpecification targetNoSpec = targetAdapter.getSpecification();
        final ObjectTitleContext titleContext = targetNoSpec.createTitleInteractionContext(getAuthenticationSession(), InteractionInitiatedBy.FRAMEWORK, targetAdapter);
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
            if(getExecutionMode().shouldFailFast()) {
                final InteractionResult interactionResult =
                        targetNoSpec.isValidResult(targetAdapter, getInteractionInitiatedBy());
                notifyListenersAndVetoIfRequired(interactionResult);
            } else {
                try {
                    final InteractionResult interactionResult =
                            targetNoSpec.isValidResult(targetAdapter, getInteractionInitiatedBy());
                    notifyListenersAndVetoIfRequired(interactionResult);
                } catch(Exception ex) {
                    return null;
                }
            }

        }

        if (getExecutionMode().shouldExecute()) {
            if (targetAdapter.isTransient()) {
                val ps = IsisContext.getPersistenceSession().get();
                if(getExecutionMode().shouldFailFast()) {
                    ps.makePersistentInTransaction(targetAdapter);
                } else {
                    try {
                        ps.makePersistentInTransaction(targetAdapter);
                    } catch(Exception ignore) {
                        // ignore
                    }
                }
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
            final OneToOneAssociation property) {

        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        if(getExecutionMode().shouldEnforceRules()) {
            if(getExecutionMode().shouldFailFast()) {
                checkVisibility(targetAdapter, property);
            } else {
                try {
                    checkVisibility(targetAdapter, property);
                } catch(Exception ex) {
                    return null;

                }
            }

        }

        resolveIfRequired(targetAdapter);

        final InteractionInitiatedBy interactionInitiatedBy = getInteractionInitiatedBy();
        final ObjectAdapter currentReferencedAdapter = property.get(targetAdapter, interactionInitiatedBy);

        final Object currentReferencedObj = ObjectAdapter.Util.unwrapPojo(currentReferencedAdapter);

        final PropertyAccessEvent ev = new PropertyAccessEvent(getDelegate(), property.getIdentifier(), currentReferencedObj);
        notifyListeners(ev);
        return currentReferencedObj;
    }


    // /////////////////////////////////////////////////////////////////
    // property - modify
    // /////////////////////////////////////////////////////////////////

    private Object handleSetterMethodOnProperty(
            final ObjectAdapter targetAdapter, final Object[] args,
            final OneToOneAssociation property) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a setter should only have a single argument");
        }

        final Object argumentObj = underlying(args[0]);

        if(getExecutionMode().shouldEnforceRules()) {
            if(getExecutionMode().shouldFailFast()) {
                checkVisibility(targetAdapter, property);
                checkUsability(targetAdapter, property);
            } else {
                try {
                    checkVisibility(targetAdapter, property);
                    checkUsability(targetAdapter, property);
                } catch(Exception ex) {
                    return null;
                }
            }

        }

        final ObjectAdapter argumentAdapter = argumentObj != null ? adapterFor(argumentObj) : null;

        resolveIfRequired(targetAdapter);


        if(getExecutionMode().shouldEnforceRules()) {
            final InteractionResult interactionResult = property.isAssociationValid(targetAdapter, argumentAdapter, getInteractionInitiatedBy()).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        }

        if (getExecutionMode().shouldExecute()) {
            if(getExecutionMode().shouldFailFast()) {
                property.set(targetAdapter, argumentAdapter, getInteractionInitiatedBy());
            } else {
                try {
                    property.set(targetAdapter, argumentAdapter, getInteractionInitiatedBy());
                } catch(Exception ignore) {
                    // ignore
                }
            }

        }

        return null;
    }


    // /////////////////////////////////////////////////////////////////
    // collection - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnCollection(
            final ObjectAdapter targetAdapter,
            final Object[] args,
            final OneToManyAssociation collection,
            final Method method,
            final String memberName) {

        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking a 'get' should have no arguments");
        }

        if(getExecutionMode().shouldEnforceRules()) {
            if(getExecutionMode().shouldFailFast()) {
                checkVisibility(targetAdapter, collection);
            } else {
                try {
                    checkVisibility(targetAdapter, collection);
                } catch(Exception ex) {
                    return null;
                }
            }

        }

        resolveIfRequired(targetAdapter);

        final InteractionInitiatedBy interactionInitiatedBy = getInteractionInitiatedBy();
        final ObjectAdapter currentReferencedAdapter = collection.get(targetAdapter, interactionInitiatedBy);

        final Object currentReferencedObj = ObjectAdapter.Util.unwrapPojo(currentReferencedAdapter);

        final CollectionAccessEvent ev = new CollectionAccessEvent(getDelegate(), collection.getIdentifier());

        if (currentReferencedObj instanceof Collection) {
            final Collection<?> collectionViewObject = lookupWrappingObject(method, memberName,
                    (Collection<?>) currentReferencedObj, collection);
            notifyListeners(ev);
            return collectionViewObject;
        } else if (currentReferencedObj instanceof Map) {
            final Map<?, ?> mapViewObject = lookupWrappingObject(memberName, (Map<?, ?>) currentReferencedObj,
                    collection);
            notifyListeners(ev);
            return mapViewObject;
        }
        throw new IllegalArgumentException(String.format("Collection type '%s' not supported by framework", currentReferencedObj.getClass().getName()));
    }

    private Collection<?> lookupWrappingObject(
            final Method method,
            final String memberName,
            final Collection<?> collectionToLookup,
            final OneToManyAssociation otma) {
        return collectionToLookup instanceof WrappingObject
                ? collectionToLookup
                        : proxy.proxy(collectionToLookup, memberName, this, otma);
    }

    private Map<?, ?> lookupWrappingObject(
            final String memberName,
            final Map<?, ?> mapToLookup,
            final OneToManyAssociation otma) {
        return mapToLookup instanceof WrappingObject
                ? mapToLookup
                        : proxy.proxy(mapToLookup, memberName, this, otma);
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
            if(getExecutionMode().shouldFailFast()) {
                checkVisibility(targetAdapter, otma);
                checkUsability(targetAdapter, otma);
            } else {
                try {
                    checkVisibility(targetAdapter, otma);
                    checkUsability(targetAdapter, otma);
                } catch(Exception ex) {
                    return null;
                }
            }
        }

        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to add");
        }
        final ObjectAdapter argumentNO = adapterFor(argumentObj);

        if(getExecutionMode().shouldEnforceRules()) {
            final InteractionResult interactionResult = otma.isValidToAdd(targetAdapter, argumentNO,
                    getInteractionInitiatedBy()).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        }

        if (getExecutionMode().shouldExecute()) {
            if(getExecutionMode().shouldFailFast()) {
                otma.addElement(targetAdapter, argumentNO, getInteractionInitiatedBy());
            } else {
                try {
                    otma.addElement(targetAdapter, argumentNO, getInteractionInitiatedBy());
                } catch(Exception ignore) {
                    // ignore
                }
            }
        }

        return null;
    }


    // /////////////////////////////////////////////////////////////////
    // collection - remove from
    // /////////////////////////////////////////////////////////////////

    private Object handleCollectionRemoveFromMethod(
            final ObjectAdapter targetAdapter,
            final Object[] args,
            final OneToManyAssociation collection) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking a removeFrom should only have a single argument");
        }

        if(getExecutionMode().shouldEnforceRules()) {
            if(getExecutionMode().shouldFailFast()) {
                checkVisibility(targetAdapter, collection);
                checkUsability(targetAdapter, collection);
            } else {
                try {
                    checkVisibility(targetAdapter, collection);
                    checkUsability(targetAdapter, collection);
                } catch(Exception ex) {
                    return null;
                }
            }

        }


        resolveIfRequired(targetAdapter);

        final Object argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to remove");
        }
        final ObjectAdapter argumentAdapter = adapterFor(argumentObj);

        if(getExecutionMode().shouldEnforceRules()) {
            final InteractionResult interactionResult = collection.isValidToRemove(targetAdapter, argumentAdapter,
                    getInteractionInitiatedBy()).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        }

        if (getExecutionMode().shouldExecute()) {
            if(getExecutionMode().shouldFailFast()) {
                collection.removeElement(targetAdapter, argumentAdapter, getInteractionInitiatedBy());
            } else {
                try {
                    collection.removeElement(targetAdapter, argumentAdapter, getInteractionInitiatedBy());
                } catch(Exception ignore) {
                    // ignore
                }
            }
        }

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

            final List<Object> argCopy = _Lists.of(args);
            argCopy.remove(contributeeParamPosition);
            contributeeArgs = argCopy.toArray();
        } else {
            contributeeAdapter = null;
            contributeeArgs = null;
        }

        if(getExecutionMode().shouldEnforceRules()) {
            if(getExecutionMode().shouldFailFast()) {
                if(contributeeMember != null) {
                    checkVisibility(contributeeAdapter, contributeeMember);
                    checkUsability(contributeeAdapter, contributeeMember);
                } else {
                    checkVisibility(targetAdapter, objectAction);
                    checkUsability(targetAdapter, objectAction);
                }
            } else {
                try {
                    if(contributeeMember != null) {
                        checkVisibility(contributeeAdapter, contributeeMember);
                        checkUsability(contributeeAdapter, contributeeMember);
                    } else {
                        checkVisibility(targetAdapter, objectAction);
                        checkUsability(targetAdapter, objectAction);
                    }
                } catch(Exception ex) {
                    return null;
                }
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
            final InteractionInitiatedBy interactionInitiatedBy = getInteractionInitiatedBy();

            final ObjectAdapter mixedInAdapter = null; // if a mixin action, then it will automatically fill in.


            ObjectAdapter returnedAdapter;

            if(getExecutionMode().shouldFailFast()) {
                returnedAdapter = objectAction.execute(
                        targetAdapter, mixedInAdapter, argAdapters,
                        interactionInitiatedBy);
            } else {
                try {
                    returnedAdapter = objectAction.execute(
                            targetAdapter, mixedInAdapter, argAdapters,
                            interactionInitiatedBy);
                } catch(Exception ignore) {
                    // ignore
                    returnedAdapter = null;
                }

            }


            return ObjectAdapter.Util.unwrapPojo(returnedAdapter);
        }

        return null;
    }

    private void checkValidity(final ObjectAdapter targetAdapter, final ObjectAction objectAction, final ObjectAdapter[] argAdapters) {
        final InteractionResult interactionResult = objectAction.isProposedArgumentSetValid(targetAdapter, argAdapters,
                getInteractionInitiatedBy()).getInteractionResult();
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
        return obj != null ? getObjectAdapterProvider().adapterFor(obj) : null;
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
        final Consent visibleConsent = objectMember.isVisible(targetObjectAdapter, getInteractionInitiatedBy(), where);
        final InteractionResult interactionResult = visibleConsent.getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private void checkUsability(
            final ObjectAdapter targetObjectAdapter,
            final ObjectMember objectMember) {
        final InteractionResult interactionResult = objectMember.isUsable(targetObjectAdapter,
                getInteractionInitiatedBy(), where
                ).getInteractionResult();
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
        return method.equals(__isis_saveMethod);
    }

    protected boolean isWrappedMethod(final Method method) {
        return method.equals(__isis_wrappedMethod);
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
        final ObjectSpecification objectSpec = getSpecification(clazz);
        if (!(objectSpec instanceof ObjectSpecificationDefault)) {
            throw new UnsupportedOperationException("Only Java is supported (specification is '" + objectSpec.getClass().getCanonicalName() + "')");
        }
        return (ObjectSpecificationDefault) objectSpec;
    }

    private ObjectSpecification getSpecification(final Class<?> type) {
        return getSpecificationLoader().loadSpecification(type);
    }

    // /////////////////////////////////////////////////////////////////
    // Dependencies
    // /////////////////////////////////////////////////////////////////

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getAuthenticationSessionProvider().getAuthenticationSession();
    }

    protected ObjectAdapterProvider getObjectAdapterProvider() {
        return objectAdapterProvider;
    }
}
