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

package org.apache.isis.wrapper.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
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
import org.apache.isis.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.isis.applib.services.wrapper.events.UsabilityEvent;
import org.apache.isis.applib.services.wrapper.events.ValidityEvent;
import org.apache.isis.applib.services.wrapper.events.VisibilityEvent;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.metamodel.IsisJdoMetamodelPlugin;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.consent.InteractionResult;
import org.apache.isis.metamodel.facets.ImperativeFacet;
import org.apache.isis.metamodel.facets.ImperativeFacet.Intent;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.metamodel.objectmanager.ObjectManager;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.specloader.specimpl.ContributeeMember;
import org.apache.isis.metamodel.specloader.specimpl.ObjectActionContributee;
import org.apache.isis.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.security.authentication.AuthenticationSession;

import lombok.val;

public class DomainObjectInvocationHandler<T> extends DelegatingInvocationHandlerDefault<T> {

    private final ProxyContextHandler proxy;
    private final EnumSet<ExecutionMode> executionMode;
    private final MetaModelContext mmContext;

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
            final MetaModelContext metaModelContext,
            final T delegate,
            final EnumSet<ExecutionMode> mode,
            final ProxyContextHandler proxy) {
        
        super(metaModelContext.getServiceRegistry(), delegate, mode);

        this.mmContext = metaModelContext;
        this.proxy = proxy;
        this.executionMode = mode;

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

        final ManagedObject targetAdapter = adapterForPojo(getDelegate());

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

            val objectAction = (ObjectAction) objectMember;

            ObjectAction actualObjectAction;
            ManagedObject actualTargetAdapter;

            val mixinFacet = targetAdapter.getSpecification().getFacet(MixinFacet.class);
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
            final ManagedObject domainObjectAdapter,
            final ObjectAction objectAction) {
        
        if(domainObjectAdapter == null) {
            return null;
        }
        val specification = domainObjectAdapter.getSpecification();
        val objectActions = specification.streamObjectActions(Contributed.INCLUDED);

        return objectActions
                .filter(action->action instanceof ObjectActionMixedIn)
                .map(action->(ObjectActionMixedIn) action)
                .filter(mixedInAction->mixedInAction.hasMixinAction(objectAction))
                .findFirst()
                .orElse(null);

        // throw new RuntimeException("Unable to find the mixed-in action corresponding to " + objectAction.getIdentifier().toFullIdentityString());
    }

    public InteractionInitiatedBy getInteractionInitiatedBy() {
        return shouldEnforceRules()
                ? InteractionInitiatedBy.USER
                        : InteractionInitiatedBy.FRAMEWORK;
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

    private Object handleTitleMethod(final ManagedObject targetAdapter)
            throws IllegalAccessException, InvocationTargetException {

        resolveIfRequired(targetAdapter);

        val targetNoSpec = targetAdapter.getSpecification();
        val titleContext = targetNoSpec.createTitleInteractionContext(
                getAuthenticationSession(), InteractionInitiatedBy.FRAMEWORK, targetAdapter);
        val titleEvent = titleContext.createInteractionEvent();
        notifyListeners(titleEvent);
        return titleEvent.getTitle();
    }

    // /////////////////////////////////////////////////////////////////
    // save
    // /////////////////////////////////////////////////////////////////

    private Object handleSaveMethod(
            final ManagedObject targetAdapter, final ObjectSpecification targetNoSpec) {

        runValidationTask(()->{
            val interactionResult =
                    targetNoSpec.isValidResult(targetAdapter, getInteractionInitiatedBy());
            notifyListenersAndVetoIfRequired(interactionResult);
        });
        
        
        val spec = targetAdapter.getSpecification();
        if(spec.isEntity()) {
            return runExecutionTask(()->{
                ManagedObject._makePersistentInTransaction(targetAdapter);
                return null;
            }); 
        }
        return null;
        
    }

    // /////////////////////////////////////////////////////////////////
    // property - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnProperty(
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToOneAssociation property) {

        zeroArgsElseThrow(args, "get");

        runValidationTask(()->{
            checkVisibility(targetAdapter, property);
        });

        resolveIfRequired(targetAdapter);
        
        return runExecutionTask(()->{
        
            val interactionInitiatedBy = getInteractionInitiatedBy();
            val currentReferencedAdapter = property.get(targetAdapter, interactionInitiatedBy);

            val currentReferencedObj = ManagedObject.unwrapPojo(currentReferencedAdapter);

            val propertyAccessEvent = new PropertyAccessEvent(
                    getDelegate(), property.getIdentifier(), currentReferencedObj);
            notifyListeners(propertyAccessEvent);
            return currentReferencedObj;
            
        });
        
    }


    // /////////////////////////////////////////////////////////////////
    // property - modify
    // /////////////////////////////////////////////////////////////////



    private Object handleSetterMethodOnProperty(
            final ManagedObject targetAdapter, 
            final Object[] args,
            final OneToOneAssociation property) {
        
        val singleArg = singleArgUnderlyingElseNull(args, "setter");
        
        runValidationTask(()->{
            checkVisibility(targetAdapter, property);
            checkUsability(targetAdapter, property);
        });
        
        val argumentAdapter = adapterForPojo(singleArg);
        
        resolveIfRequired(targetAdapter);

        runValidationTask(()->{
            val interactionResult = property.isAssociationValid(
                    targetAdapter, argumentAdapter, getInteractionInitiatedBy())
                    .getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        });
        
        return runExecutionTask(()->{
            property.set(targetAdapter, argumentAdapter, getInteractionInitiatedBy());
            return null;
        });
        
    }


    // /////////////////////////////////////////////////////////////////
    // collection - access
    // /////////////////////////////////////////////////////////////////

    private Object handleGetterMethodOnCollection(
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToManyAssociation collection,
            final Method method,
            final String memberName) {

        zeroArgsElseThrow(args, "get");

        runValidationTask(()->{
            checkVisibility(targetAdapter, collection);
        });

        resolveIfRequired(targetAdapter);
        
        return runExecutionTask(()->{
        
            val interactionInitiatedBy = getInteractionInitiatedBy();
            val currentReferencedAdapter = collection.get(targetAdapter, interactionInitiatedBy);

            val currentReferencedObj = ManagedObject.unwrapPojo(currentReferencedAdapter);

            val collectionAccessEvent = new CollectionAccessEvent(getDelegate(), collection.getIdentifier());

            if (currentReferencedObj instanceof Collection) {
                val collectionViewObject = lookupWrappingObject(method, memberName,
                        (Collection<?>) currentReferencedObj, collection);
                notifyListeners(collectionAccessEvent);
                return collectionViewObject;
            } else if (currentReferencedObj instanceof Map) {
                val mapViewObject = lookupWrappingObject(memberName, (Map<?, ?>) currentReferencedObj,
                        collection);
                notifyListeners(collectionAccessEvent);
                return mapViewObject;
            }
            
            val msg = String.format("Collection type '%s' not supported by framework", currentReferencedObj.getClass().getName()); 
            throw new IllegalArgumentException(msg);
            
        });
        
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
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToManyAssociation otma) {

        val singleArg = singleArgUnderlyingElseThrow(args, "addTo");

        runValidationTask(()->{
            checkVisibility(targetAdapter, otma);
            checkUsability(targetAdapter, otma);
        });
        
        resolveIfRequired(targetAdapter);
        val argumentAdapter = adapterForPojo(singleArg);
        
        runValidationTask(()->{
            val interactionResult = otma.isValidToAdd(targetAdapter, argumentAdapter,
                    getInteractionInitiatedBy()).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        });
        
        return runExecutionTask(()->{
            otma.addElement(targetAdapter, argumentAdapter, getInteractionInitiatedBy());
            return null;
        });
        
    }


    
    // /////////////////////////////////////////////////////////////////
    // collection - remove from
    // /////////////////////////////////////////////////////////////////



    private Object handleCollectionRemoveFromMethod(
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToManyAssociation collection) {
        
        val singleArg = singleArgUnderlyingElseThrow(args, "removeFrom");

        runValidationTask(()->{
            checkVisibility(targetAdapter, collection);
            checkUsability(targetAdapter, collection);
        });

        resolveIfRequired(targetAdapter);
        val argumentAdapter = adapterForPojo(singleArg);

        runValidationTask(()->{
            val interactionResult = collection.isValidToRemove(targetAdapter, argumentAdapter,
                    getInteractionInitiatedBy()).getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        });
        
        return runExecutionTask(()->{
            collection.removeElement(targetAdapter, argumentAdapter, getInteractionInitiatedBy());
            return null;
        });

        
    }

    // /////////////////////////////////////////////////////////////////
    // action
    // /////////////////////////////////////////////////////////////////

    private Object handleActionMethod(
            final ManagedObject targetAdapter, 
            final Object[] args,
            final ObjectAction objectAction,
            final ContributeeMember contributeeMember) {

        final ManagedObject contributeeAdapter;
        final Object[] contributeeArgs;
        if(contributeeMember != null) {
            val contributeeParamPosition = contributeeMember.getContributeeParamPosition();
            val contributee = args[contributeeParamPosition];
            
            contributeeAdapter = adapterForPojo(contributee);
            contributeeArgs = _Arrays.removeByIndex(args, contributeeParamPosition); 
        } else {
            contributeeAdapter = null;
            contributeeArgs = null;
        }
        
        val argAdapters = asObjectAdaptersUnderlying(args);

        runValidationTask(()->{
            if(contributeeMember != null) {
                checkVisibility(contributeeAdapter, contributeeMember);
                checkUsability(contributeeAdapter, contributeeMember);
                
                if(contributeeMember instanceof ObjectActionContributee) {
                    val objectActionContributee = (ObjectActionContributee) contributeeMember;
                    val contributeeArgAdapters = asObjectAdaptersUnderlying(contributeeArgs);
                    checkValidity(contributeeAdapter, objectActionContributee, contributeeArgAdapters);
                }
                // nothing to do for contributed properties or collections
                
            } else {
                checkVisibility(targetAdapter, objectAction);
                checkUsability(targetAdapter, objectAction);
                checkValidity(targetAdapter, objectAction, argAdapters);
            }
        });
        
        return runExecutionTask(()->{

            val interactionInitiatedBy = getInteractionInitiatedBy();
            val mixedInAdapter = (ManagedObject)null; // if a mixin action, then it will automatically fill in.
            val returnedAdapter = objectAction.execute(
                    targetAdapter, mixedInAdapter, argAdapters,
                    interactionInitiatedBy);
            return ManagedObject.unwrapPojo(returnedAdapter);
            
        });
        
    }

    private void checkValidity(
            final ManagedObject targetAdapter, 
            final ObjectAction objectAction, 
            final ManagedObject[] argAdapters) {
        
        val interactionResult = objectAction.isProposedArgumentSetValid(targetAdapter, argAdapters,
                getInteractionInitiatedBy()).getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private ManagedObject[] asObjectAdaptersUnderlying(final Object[] args) {

        val argAdapters = new ManagedObject[args.length];
        int i = 0;
        for (final Object arg : args) {
            argAdapters[i++] = adapterForPojo(underlying(arg));
        }

        return argAdapters;
    }

    private ManagedObject adapterForPojo(final Object pojo) {
        return pojo != null ? getObjectManager().adapt(pojo) : null;
    }

    private Object underlying(final Object arg) {
        if (arg instanceof WrappingObject) {
            val argViewObject = (WrappingObject) arg;
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
            final ManagedObject targetObjectAdapter,
            final ObjectMember objectMember) {
        
        val visibleConsent = objectMember.isVisible(targetObjectAdapter, getInteractionInitiatedBy(), where);
        val interactionResult = visibleConsent.getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private void checkUsability(
            final ManagedObject targetObjectAdapter,
            final ObjectMember objectMember) {
        
        val interactionResult = objectMember.isUsable(
                targetObjectAdapter,
                getInteractionInitiatedBy(), 
                where)
                .getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    // -- NOTIFY LISTENERS

    private void notifyListenersAndVetoIfRequired(final InteractionResult interactionResult) {
        val interactionEvent = interactionResult.getInteractionEvent();
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

    // -- SWITCHING

    private ObjectMember locateAndCheckMember(final Method method) {
        val objectSpecificationDefault = getJavaSpecificationOfOwningClass(method);
        val member = objectSpecificationDefault.getMember(method);

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

    // -- SPECIFICATION LOOKUP

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

    // -- HELPER
    
    private boolean shouldEnforceRules() {
        return !getExecutionMode().contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }
    
    private boolean shouldExecute() {
        return !getExecutionMode().contains(ExecutionMode.SKIP_EXECUTION);
    }
    
    private boolean shouldFailFast() {
        return !getExecutionMode().contains(ExecutionMode.SWALLOW_EXCEPTIONS);
    }
    
    private void runValidationTask(Runnable task) {
        if(!shouldEnforceRules()) {
            return;
        }
        if(shouldFailFast()) {
            task.run();
        } else {
            try {
                task.run();
            } catch(Exception ex) {
                // swallow
            }
        }
    }
    
    private <X> X runExecutionTask(Supplier<X> task) {
        if(!shouldExecute()) {
            return null;
        }
        if(shouldFailFast()) {
            return task.get();
        } else {
            try {
                return task.get();
            } catch(Exception ex) {
                // swallow
                return null;
            }
        }
    }

    private Object singleArgUnderlyingElseThrow(Object[] args, String name) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking '" + name + "' should only have a single argument");
        }
        val argumentObj = underlying(args[0]);
        if (argumentObj == null) {
            throw new IllegalArgumentException("Must provide a non-null object to '" + name +"'");
        }
        return argumentObj;
    }
    
    private Object singleArgUnderlyingElseNull(Object[] args, String name) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Invoking '" + name + "' should only have a single argument");
        }
        val argumentObj = underlying(args[0]);
        return argumentObj;
    }
    
    private void zeroArgsElseThrow(Object[] args, String name) {
        if (args.length != 0) {
            throw new IllegalArgumentException("Invoking '" + name + "' should have no arguments");
        }
    }
    
    // -- DEPENDENCIES

    protected SpecificationLoader getSpecificationLoader() {
        return mmContext.getSpecificationLoader();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return mmContext.getAuthenticationSessionProvider().getAuthenticationSession();
    }

    protected ObjectManager getObjectManager() {
        return mmContext.getObjectManager();
    }

    
}
