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
package org.apache.causeway.core.runtimeservices.wrapper.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.exceptions.recoverable.InteractionException;
import org.apache.causeway.applib.services.wrapper.DisabledException;
import org.apache.causeway.applib.services.wrapper.HiddenException;
import org.apache.causeway.applib.services.wrapper.InvalidException;
import org.apache.causeway.applib.services.wrapper.WrappingObject;
import org.apache.causeway.applib.services.wrapper.control.ExecutionMode;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.applib.services.wrapper.events.CollectionAccessEvent;
import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.applib.services.wrapper.events.PropertyAccessEvent;
import org.apache.causeway.applib.services.wrapper.events.UsabilityEvent;
import org.apache.causeway.applib.services.wrapper.events.ValidityEvent;
import org.apache.causeway.applib.services.wrapper.events.VisibilityEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet.Intent;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmAssertionUtils;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @param <T>
 */
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Log4j2
public class DomainObjectInvocationHandler<T>
extends DelegatingInvocationHandlerAbstract<T> {

    private final ProxyContextHandler proxyContextHandler;

    @EqualsAndHashCode.Include // this is the only state that is significant to distinguish one handler from another
    private final ObjectSpecification targetSpecification;

    /**
     * The <tt>title()</tt> method; may be <tt>null</tt>.
     */
    protected Method titleMethod;

    /**
     * The <tt>__causeway_save()</tt> method from {@link WrappingObject#__causeway_save()}.
     */
    protected Method __causeway_saveMethod;

    /**
     * The <tt>__causeway_wrapped()</tt> method from {@link WrappingObject#__causeway_wrapped()}.
     */
    protected Method __causeway_wrappedMethod;

    /**
     * The <tt>__causeway_executionModes()</tt> method from {@link WrappingObject#__causeway_executionModes()}.
     */
    protected Method __causeway_executionModes;


    public DomainObjectInvocationHandler(
            final MetaModelContext metaModelContext,
            final ProxyContextHandler proxyContextHandler,
            final ObjectSpecification targetSpecification
    ) {
        super(
                metaModelContext,
                (Class<T>) targetSpecification.getCorrespondingClass()
        );
        this.proxyContextHandler = proxyContextHandler;
        this.targetSpecification = targetSpecification;

        try {
            titleMethod = getTargetClass().getMethod("title", _Constants.emptyClasses);
        } catch (final NoSuchMethodException e) {
            // ignore
        }
        try {
            __causeway_saveMethod = WrappingObject.class.getMethod("__causeway_save", _Constants.emptyClasses);
            __causeway_wrappedMethod = WrappingObject.class.getMethod("__causeway_wrapped", _Constants.emptyClasses);
            __causeway_executionModes = WrappingObject.class.getMethod("__causeway_executionModes", _Constants.emptyClasses);
        } catch (final NoSuchMethodException nsme) {
            throw new IllegalStateException(
                    "Could not locate reserved declared methods in the WrappingObject interfaces",
                    nsme);
        }
    }

    /**
     *
     * @param proxyObject - holds the reference to {@link WrapperInvocationContext} which in turn references the target pojo (and mixee pojo if target is a mixin).
     * @param method - the method invoked on the proxy
     * @param args - the args to the method invoked on the proxy
     * @throws Throwable
     */
    @Override
    public Object invoke(final Object proxyObject, final Method method, final Object[] args) throws Throwable {

        final var wic = WrapperInvocationContext.get(proxyObject);
        if(wic != null) {
            return doInvoke(proxyObject, method, args);
        }

        throw new IllegalStateException("Unable to find the wrapper invocation context");
    }

    /**
     * The target, either a domain object or mixin instance (wrapping a mixee).
     * @return
     */
    @Override
    public T getTarget(Object proxyObject) {
        return (T) WrapperInvocationContext.get(proxyObject).targetPojo;
    }


    public ManagedObject getMixeeAdapter(Object proxyObject) {
        Object mixeePojo = WrapperInvocationContext.get(proxyObject).mixeePojo;
        return adaptAndGuardAgainstWrappingNotSupported(mixeePojo);
    }

    public SyncControl getSyncControl(Object proxyObject) {
        return WrapperInvocationContext.get(proxyObject).syncControl;
    }

    private Object doInvoke(Object proxyObject, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
        if (isObjectMethod(method)) {
            return delegate(proxyObject, method, args);
        }

        if(isEnhancedEntityMethod(method)) {
            return delegate(proxyObject, method, args);
        }

        final ManagedObject targetAdapter =  metaModelContext.getObjectManager().adapt(getTarget(proxyObject));

        if(!targetAdapter.getSpecialization().isMixin()) {
            MmAssertionUtils.assertIsBookmarkSupported(targetAdapter);
        }

        if (method.equals(titleMethod)) {
            return handleTitleMethod(targetAdapter);
        }

        val resolvedMethod = _GenericResolver.resolveMethod(method, targetSpecification.getCorrespondingClass())
                .orElseThrow();

        // save method, through the proxy
        if (method.equals(__causeway_saveMethod)) {
            return handleSaveMethod(proxyObject, targetAdapter, targetSpecification);
        }

        if (method.equals(__causeway_wrappedMethod)) {
            return getTarget(proxyObject);
        }

        if (method.equals(__causeway_executionModes)) {
            return getSyncControl(proxyObject).getExecutionModes();
        }

        val objectMember = targetSpecification.getMemberElseFail(resolvedMethod);
        val memberId = objectMember.getId();

        val intent = ImperativeFacet.getIntent(objectMember, resolvedMethod);
        if(intent == Intent.CHECK_IF_HIDDEN || intent == Intent.CHECK_IF_DISABLED) {
            throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'", memberId));
        }

        if (intent == Intent.DEFAULTS || intent == Intent.CHOICES_OR_AUTOCOMPLETE) {
            return method.invoke(getTarget(proxyObject), args);
        }

        if (objectMember.isOneToOneAssociation()) {

            if (intent == Intent.CHECK_IF_VALID || intent == Intent.MODIFY_PROPERTY_SUPPORTING) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method for '%s'; use only property accessor/mutator", memberId));
            }

            final OneToOneAssociation otoa = (OneToOneAssociation) objectMember;

            if (intent == Intent.ACCESSOR) {
                return handleGetterMethodOnProperty(proxyObject, targetAdapter, args, otoa);
            }

            if (intent == Intent.MODIFY_PROPERTY || intent == Intent.INITIALIZATION) {
                return handleSetterMethodOnProperty(proxyObject, targetAdapter, args, otoa);
            }
        }
        if (objectMember.isOneToManyAssociation()) {

            if (intent == Intent.CHECK_IF_VALID) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only collection accessor/mutator", memberId));
            }

            final OneToManyAssociation otma = (OneToManyAssociation) objectMember;
            if (intent == Intent.ACCESSOR) {
                return handleGetterMethodOnCollection(proxyObject, targetAdapter, args, otma, memberId);
            }
        }

        if (objectMember instanceof ObjectAction) {

            if (intent == Intent.CHECK_IF_VALID) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only the 'invoke' method", memberId));
            }

            val objectAction = (ObjectAction) objectMember;

            if(Facets.mixinIsPresent(targetSpecification)) {
                if (getMixeeAdapter(proxyObject) == null) {
                    throw _Exceptions.illegalState(
                            "Missing the required mixeeAdapter for action '%s'",
                            objectAction.getId());
                }
                MmAssertionUtils.assertIsBookmarkSupported(getMixeeAdapter(proxyObject));

                final ObjectMember mixinMember = determineMixinMember(getMixeeAdapter(proxyObject), objectAction);

                if (mixinMember != null) {
                    if(mixinMember instanceof ObjectAction) {
                        return handleActionMethod(proxyObject, getMixeeAdapter(proxyObject), args, (ObjectAction) mixinMember);
                    }
                    if(mixinMember instanceof OneToOneAssociation) {
                        return handleGetterMethodOnProperty(proxyObject, getMixeeAdapter(proxyObject), new Object[0], (OneToOneAssociation) mixinMember);
                    }
                    if(mixinMember instanceof OneToManyAssociation) {
                        return handleGetterMethodOnCollection(proxyObject, getMixeeAdapter(proxyObject), new Object[0], (OneToManyAssociation) mixinMember, memberId);
                    }
                } else {
                    throw _Exceptions.illegalState(String.format(
                            "Could not locate mixin member for action '%s' on spec '%s'", objectAction.getId(), targetSpecification));
                }
            }

            // this is just a regular non-mixin action.
            return handleActionMethod(proxyObject, targetAdapter, args, objectAction);
        }

        throw new UnsupportedOperationException(String.format("Unknown member type '%s'", objectMember));
    }

    private static ObjectMember determineMixinMember(
            final ManagedObject domainObjectAdapter,
            final ObjectAction objectAction) {

        if(domainObjectAdapter == null) {
            return null;
        }
        val specification = domainObjectAdapter.getSpecification();
        val objectActions = specification.streamAnyActions(MixedIn.INCLUDED);
        val objectAssociations = specification.streamAssociations(MixedIn.INCLUDED);

        final Stream<ObjectMember> objectMembers = Stream.concat(objectActions, objectAssociations);
        return objectMembers
                .filter(MixedInMember.class::isInstance)
                .map(MixedInMember.class::cast)
                .filter(mixedInMember->mixedInMember.hasMixinAction(objectAction))
                .findFirst()
                .orElse(null);

        // throw new RuntimeException("Unable to find the mixed-in action corresponding to " + objectAction.getIdentifier().toFullIdentityString());
    }

    public InteractionInitiatedBy getInteractionInitiatedBy(Object proxyObject) {
        return shouldEnforceRules(proxyObject)
                ? InteractionInitiatedBy.USER
                : InteractionInitiatedBy.FRAMEWORK;
    }

    private boolean isEnhancedEntityMethod(final Method method) {
        return targetSpecification.entityFacet()
                .map(x -> x.isProxyEnhancement(method))
                .orElse(false);
    }


    private Object handleTitleMethod(final ManagedObject targetAdapter) {

        val targetNoSpec = targetAdapter.getSpecification();
        val titleContext = targetNoSpec
                .createTitleInteractionContext(targetAdapter, InteractionInitiatedBy.FRAMEWORK);
        val titleEvent = titleContext.createInteractionEvent();
        notifyListeners(titleEvent);
        return titleEvent.getTitle();
    }


    private Object handleSaveMethod(
            Object proxyObject, final ManagedObject targetAdapter, final ObjectSpecification targetNoSpec) {

        runValidationTask(proxyObject, ()->{
            val interactionResult =
                    targetNoSpec.isValidResult(targetAdapter, getInteractionInitiatedBy(proxyObject));
            notifyListenersAndVetoIfRequired(interactionResult);
        });


        val spec = targetAdapter.getSpecification();
        if(spec.isEntity()) {
            return runExecutionTask(proxyObject, ()->{
                MmEntityUtils.persistInCurrentTransaction(targetAdapter);
                return null;
            });
        }
        return null;

    }


    private Object handleGetterMethodOnProperty(
            final Object proxyObject,
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToOneAssociation property) {

        zeroArgsElseThrow(args, "get");

        runValidationTask(proxyObject, ()->{
            checkVisibility(proxyObject, targetAdapter, property);
        });

        return runExecutionTask(proxyObject, ()->{

            val interactionInitiatedBy = getInteractionInitiatedBy(proxyObject);
            val currentReferencedAdapter = property.get(targetAdapter, interactionInitiatedBy);

            val currentReferencedObj = MmUnwrapUtils.single(currentReferencedAdapter);


            val propertyAccessEvent = new PropertyAccessEvent(
                    getTarget(proxyObject), property.getFeatureIdentifier(), currentReferencedObj);
            notifyListeners(propertyAccessEvent);
            return currentReferencedObj;

        });

    }



    private Object handleSetterMethodOnProperty(
            final Object proxyObject,
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToOneAssociation property) {

        val singleArg = singleArgUnderlyingElseNull(args, "setter");

        runValidationTask(proxyObject, ()->{
            checkVisibility(proxyObject, targetAdapter, property);
            checkUsability(proxyObject, targetAdapter, property);
        });

        val argumentAdapter = metaModelContext.getObjectManager().adapt(singleArg);

        runValidationTask(proxyObject, ()->{
            val interactionResult = property.isAssociationValid(
                    targetAdapter, argumentAdapter, getInteractionInitiatedBy(proxyObject))
                    .getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        });

        return runExecutionTask(proxyObject, ()->{
            property.set(targetAdapter, argumentAdapter, getInteractionInitiatedBy(proxyObject));
            return null;
        });

    }



    private Object handleGetterMethodOnCollection(
            final Object proxyObject,
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToManyAssociation collection,
            final String memberId) {

        zeroArgsElseThrow(args, "get");

        runValidationTask(proxyObject, ()->{
            checkVisibility(proxyObject, targetAdapter, collection);
        });

        return runExecutionTask(proxyObject, ()->{

            val interactionInitiatedBy = getInteractionInitiatedBy(proxyObject);
            val currentReferencedAdapter = collection.get(targetAdapter, interactionInitiatedBy);

            val currentReferencedObj = MmUnwrapUtils.single(currentReferencedAdapter);

            val collectionAccessEvent = new CollectionAccessEvent(getTarget(proxyObject), collection.getFeatureIdentifier());

            if (currentReferencedObj instanceof Collection) {
                val collectionViewObject = lookupWrappingObject(
                        proxyObject, (Collection<?>) currentReferencedObj, collection);
                notifyListeners(collectionAccessEvent);
                return collectionViewObject;
            } else if (currentReferencedObj instanceof Map) {
                val mapViewObject = lookupWrappingObject(proxyObject, (Map<?, ?>) currentReferencedObj,
                        collection);
                notifyListeners(collectionAccessEvent);
                return mapViewObject;
            }

            val msg = String.format("Collection type '%s' not supported by framework", currentReferencedObj.getClass().getName());
            throw new IllegalArgumentException(msg);

        });

    }

    private Collection<?> lookupWrappingObject(
            final Object proxyObject,
            final Collection<?> collectionToLookup,
            final OneToManyAssociation otma) {
        if (collectionToLookup instanceof WrappingObject) {
            return collectionToLookup;
        }
        if(proxyContextHandler == null) {
            throw new IllegalStateException("Unable to create proxy for collection; "
                    + "proxyContextHandler not provided");
        }
        return proxyContextHandler.proxy(proxyObject, collectionToLookup, this, otma);
    }

    private Map<?, ?> lookupWrappingObject(
            final Object proxyObject,
            final Map<?, ?> mapToLookup,
            final OneToManyAssociation otma) {
        if (mapToLookup instanceof WrappingObject) {
            return mapToLookup;
        }
        if(proxyContextHandler == null) {
            throw new IllegalStateException("Unable to create proxy for collection; "
                    + "proxyContextHandler not provided");
        }
        return proxyContextHandler.proxy(proxyObject, mapToLookup, this, otma);
    }



    private Object handleActionMethod(
            final Object proxyObject,
            final ManagedObject targetAdapter,
            final Object[] args,
            final ObjectAction objectAction) {

        val head = objectAction.interactionHead(targetAdapter);
        val objectManager = metaModelContext.getObjectManager();

        // adapt argument pojos to managed objects
        val argAdapters = objectAction.getParameterTypes().map(IndexedFunction.zeroBased((paramIndex, paramSpec)->{
            // guard against index out of bounds
            val argPojo = _Arrays.get(args, paramIndex).orElse(null);
            return argPojo!=null
                    ? objectManager.adapt(argPojo)
                    : ManagedObject.empty(paramSpec);
        }));

        runValidationTask(proxyObject, ()->{
            checkVisibility(proxyObject, targetAdapter, objectAction);
            checkUsability(proxyObject, targetAdapter, objectAction);
            checkValidity(proxyObject, head, objectAction, argAdapters);
        });

        return runExecutionTask(proxyObject, ()->{
            val interactionInitiatedBy = getInteractionInitiatedBy(proxyObject);

            val returnedAdapter = objectAction.execute(
                    head, argAdapters,
                    interactionInitiatedBy);
            return MmUnwrapUtils.single(returnedAdapter);

        });

    }

    private void checkValidity(
            final Object proxyObject,
            final ActionInteractionHead head,
            final ObjectAction objectAction,
            final Can<ManagedObject> argAdapters) {

        val interactionResult = objectAction
                .isArgumentSetValid(head, argAdapters, getInteractionInitiatedBy(proxyObject))
                .getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private Object underlying(final Object arg) {
        if (arg instanceof WrappingObject) {
            val argViewObject = (WrappingObject) arg;
            return argViewObject.__causeway_wrapped();
        } else {
            return arg;
        }
    }


    /**
     * REVIEW: ideally should provide some way to allow to caller to indicate the 'where' context.  Having
     * a hard-coded value like this is an approximation.
     */
    private final Where where = Where.ANYWHERE;

    private void checkVisibility(
            final Object proxyObject,
            final ManagedObject targetObjectAdapter,
            final ObjectMember objectMember) {

        val visibleConsent = objectMember.isVisible(targetObjectAdapter, getInteractionInitiatedBy(proxyObject), where);
        val interactionResult = visibleConsent.getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private void checkUsability(
            final Object proxyObject,
            final ManagedObject targetObjectAdapter,
            final ObjectMember objectMember) {

        val interactionResult = objectMember.isUsable(
                targetObjectAdapter,
                getInteractionInitiatedBy(proxyObject),
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

    // -- HELPER

    private boolean shouldEnforceRules(Object proxyObject) {
        return !getSyncControl(proxyObject).getExecutionModes().contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }

    private boolean shouldExecute(Object proxyObject) {
        return !getSyncControl(proxyObject).getExecutionModes().contains(ExecutionMode.SKIP_EXECUTION);
    }

    private void runValidationTask(Object proxyObject, final Runnable task) {
        if(!shouldEnforceRules(proxyObject)) {
            return;
        }
        try {
            task.run();
        } catch(Exception ex) {
            handleException(proxyObject, ex);
        }
    }

    private <X> X runExecutionTask(Object proxyObject, final Supplier<X> task) {
        if(!shouldExecute(proxyObject)) {
            return null;
        }
        try {
            return task.get();
        } catch(Exception ex) {
            return _Casts.uncheckedCast(handleException(proxyObject, ex));
        }
    }

    @SneakyThrows
    private Object handleException(Object proxyObject, final Exception ex) {
        val exceptionHandler = getSyncControl(proxyObject).getExceptionHandler()
                .orElse(null);

        if(exceptionHandler==null) {
            log.warn("No ExceptionHandler was setup to handle this Exception", ex);
        }

        return exceptionHandler!=null
                ? exceptionHandler.handle(ex)
                : null;
    }

    private Object singleArgUnderlyingElseNull(final Object[] args, final String name) {
        if (args.length != 1) {
            throw new IllegalArgumentException(String.format(
                    "Invoking '%s' should only have a single argument", name));
        }
        val argumentObj = underlying(args[0]);
        return argumentObj;
    }

    private void zeroArgsElseThrow(final Object[] args, final String name) {
        if (!_NullSafe.isEmpty(args)) {
            throw new IllegalArgumentException(String.format(
                    "Invoking '%s' should have no arguments", name));
        }
    }

}
