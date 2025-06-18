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
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
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
import org.apache.causeway.core.runtime.wrap.WrapperInvocationHandler;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;

/**
 *
 * @param <T> type of delegate
 */
@Log4j2
final class DomainObjectInvocationHandler<T>
implements WrapperInvocationHandler {

    @Getter(onMethod_ = {@Override}) @Accessors(fluent=true) 
    private final WrapperInvocationHandler.ClassMetaData classMetaData;
    
    private final ProxyGenerator proxyGenerator;
    private final MetaModelContext mmc;

    /**
     * The <tt>title()</tt> method; may be <tt>null</tt>.
     */
    protected final Method titleMethod;

    /**
     * The <tt>__causeway_origin()</tt> method from {@link WrappingObject#__causeway_origin()}.
     */
    protected final Method __causeway_originMethod;
    
    /**
     * The <tt>__causeway_save()</tt> method from {@link WrappingObject#__causeway_save()}.
     */
    protected final Method __causeway_saveMethod;

    /**
     * The <tt>__causeway_executionModes()</tt> method from {@link WrappingObject#__causeway_executionModes()}.
     */
    protected final Method __causeway_executionModes;

    private final EntityFacet entityFacet;
    private final ManagedObject mixeeAdapter;

    public DomainObjectInvocationHandler(
            final T domainObject,
            final ManagedObject mixeeAdapter, // ignored if not handling a mixin
            final ManagedObject targetAdapter,
            final ProxyGenerator proxyGenerator) {
        
        this.mmc = targetAdapter.objSpec().getMetaModelContext();
        this.classMetaData = WrapperInvocationHandler.ClassMetaData.of(domainObject);
        this.proxyGenerator = proxyGenerator;

        var _titleMethod = (Method)null;
        try {
            _titleMethod = classMetaData().pojoClass().getMethod("title", _Constants.emptyClasses);
        } catch (final NoSuchMethodException e) {
            // ignore
        }
        this.titleMethod = _titleMethod;
        
        try {
            this.__causeway_originMethod = WrappingObject.class.getMethod(WrappingObject.ORIGIN_GETTER_NAME, _Constants.emptyClasses);
            this.__causeway_saveMethod = WrappingObject.class.getMethod(WrappingObject.SAVE_METHOD_NAME, _Constants.emptyClasses);
            this.__causeway_executionModes = WrappingObject.class.getMethod(WrappingObject.EXECUTION_MODES_METHOD_NAME, _Constants.emptyClasses);
        } catch (final NoSuchMethodException nsme) {
            throw new IllegalStateException(
                    "Could not locate reserved declared methods in the WrappingObject interfaces",
                    nsme);
        }

        this.entityFacet = targetAdapter.objSpec().entityFacet().orElse(null);
        this.mixeeAdapter = mixeeAdapter;
    }

    @Override
    public Object invoke(WrapperInvocation wrapperInvocation) throws Throwable {
    
        final Object target = wrapperInvocation.origin().pojo();
        final Method method = wrapperInvocation.method();
        final Object[] args = wrapperInvocation.args();
        var syncControl = wrapperInvocation.origin().syncControl();        
        
        if (classMetaData().isObjectMethod(method)
                || isEnhancedEntityMethod(method)) {
            return method.invoke(target, args);
        }

        final ManagedObject targetAdapter = mmc.getObjectManager().adapt(target);

        if(!targetAdapter.specialization().isMixin()) {
            MmAssertionUtils.assertIsBookmarkSupported(targetAdapter);
        }

        if (method.equals(titleMethod)) {
            return handleTitleMethod(wrapperInvocation, targetAdapter);
        }

        final ObjectSpecification targetSpec = targetAdapter.objSpec();
        var resolvedMethod = _GenericResolver.resolveMethod(method, targetSpec.getCorrespondingClass())
                .orElseThrow();

        if(!wrapperInvocation.origin().isFallback()) {
        
            if (method.equals(__causeway_originMethod)) {
                return wrapperInvocation.origin();
            }
            
            // save method, through the proxy
            if (method.equals(__causeway_saveMethod)) {
                return handleSaveMethod(wrapperInvocation, targetAdapter, targetSpec);
            }
    
            if (method.equals(__causeway_executionModes)) {
                return syncControl.getExecutionModes();
            }
        }

        var objectMember = targetSpec.getMemberElseFail(resolvedMethod);
        var memberId = objectMember.getId();

        var intent = ImperativeFacet.getIntent(objectMember, resolvedMethod);
        if(intent == Intent.CHECK_IF_HIDDEN || intent == Intent.CHECK_IF_DISABLED) {
            throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'", memberId));
        }

        if (intent == Intent.DEFAULTS || intent == Intent.CHOICES_OR_AUTOCOMPLETE) {
            return method.invoke(target, args);
        }

        if (objectMember.isOneToOneAssociation()) {

            if (intent == Intent.CHECK_IF_VALID || intent == Intent.MODIFY_PROPERTY_SUPPORTING) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method for '%s'; use only property accessor/mutator", memberId));
            }

            final OneToOneAssociation otoa = (OneToOneAssociation) objectMember;

            if (intent == Intent.ACCESSOR) {
                return handleGetterMethodOnProperty(wrapperInvocation, targetAdapter, args, otoa);
            }

            if (intent == Intent.MODIFY_PROPERTY || intent == Intent.INITIALIZATION) {
                return handleSetterMethodOnProperty(wrapperInvocation, targetAdapter, args, otoa);
            }
        }
        if (objectMember.isOneToManyAssociation()) {

            if (intent == Intent.CHECK_IF_VALID) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only collection accessor/mutator", memberId));
            }

            final OneToManyAssociation otma = (OneToManyAssociation) objectMember;
            if (intent == Intent.ACCESSOR) {
                return handleGetterMethodOnCollection(wrapperInvocation, targetAdapter, args, otma, memberId);
            }
        }

        if (objectMember instanceof ObjectAction) {

            if (intent == Intent.CHECK_IF_VALID) {
                throw new UnsupportedOperationException(String.format("Cannot invoke supporting method '%s'; use only the 'invoke' method", memberId));
            }

            var objectAction = (ObjectAction) objectMember;

            if(Facets.mixinIsPresent(targetSpec)) {
                if (mixeeAdapter == null) {
                    throw _Exceptions.illegalState(
                            "Missing the required mixeeAdapter for action '%s'",
                            objectAction.getId());
                }
                MmAssertionUtils.assertIsBookmarkSupported(mixeeAdapter);

                final ObjectMember mixinMember = determineMixinMember(mixeeAdapter, objectAction);

                if (mixinMember != null) {
                    if(mixinMember instanceof ObjectAction) {
                        return handleActionMethod(wrapperInvocation, mixeeAdapter, args, (ObjectAction)mixinMember);
                    }
                    if(mixinMember instanceof OneToOneAssociation) {
                        return handleGetterMethodOnProperty(wrapperInvocation, mixeeAdapter, new Object[0], (OneToOneAssociation)mixinMember);
                    }
                    if(mixinMember instanceof OneToManyAssociation) {
                        return handleGetterMethodOnCollection(wrapperInvocation, mixeeAdapter, new Object[0], (OneToManyAssociation)mixinMember, memberId);
                    }
                } else {
                    throw _Exceptions.illegalState(String.format(
                            "Could not locate mixin member for action '%s' on spec '%s'", objectAction.getId(), targetSpec));
                }
            }

            // this is just a regular non-mixin action.
            return handleActionMethod(wrapperInvocation, targetAdapter, args, objectAction);
        }

        throw new UnsupportedOperationException(String.format("Unknown member type '%s'", objectMember));
    }

    private static ObjectMember determineMixinMember(
            final ManagedObject domainObjectAdapter,
            final ObjectAction objectAction) {

        if(domainObjectAdapter == null) {
            return null;
        }
        var specification = domainObjectAdapter.objSpec();
        var objectActions = specification.streamAnyActions(MixedIn.INCLUDED);
        var objectAssociations = specification.streamAssociations(MixedIn.INCLUDED);

        final Stream<ObjectMember> objectMembers = Stream.concat(objectActions, objectAssociations);
        return objectMembers
                .filter(MixedInMember.class::isInstance)
                .map(MixedInMember.class::cast)
                .filter(mixedInMember->mixedInMember.hasMixinAction(objectAction))
                .findFirst()
                .orElse(null);

        // throw new RuntimeException("Unable to find the mixed-in action corresponding to " + objectAction.getIdentifier().toFullIdentityString());
    }

    public InteractionInitiatedBy getInteractionInitiatedBy(final WrapperInvocation wrapperInvocation) {
        return wrapperInvocation.shouldEnforceRules()
                ? InteractionInitiatedBy.USER
                : InteractionInitiatedBy.FRAMEWORK;
    }

    private boolean isEnhancedEntityMethod(final Method method) {
        return entityFacet!=null
                ? entityFacet.isProxyEnhancement(method)
                : false;
    }

    private Object handleTitleMethod(
            final WrapperInvocation wrapperInvocation, 
            final ManagedObject targetAdapter) {

        var targetNoSpec = targetAdapter.objSpec();
        var titleContext = targetNoSpec
                .createTitleInteractionContext(targetAdapter, InteractionInitiatedBy.FRAMEWORK);
        var titleEvent = titleContext.createInteractionEvent();
        mmc.getWrapperFactory().notifyListeners(titleEvent);
        return titleEvent.getTitle();
    }

    private Object handleSaveMethod(
            final WrapperInvocation wrapperInvocation, 
            final ManagedObject targetAdapter, 
            final ObjectSpecification targetNoSpec) {

        runValidationTask(wrapperInvocation, ()->{
            var interactionResult =
                    targetNoSpec.isValidResult(targetAdapter, getInteractionInitiatedBy(wrapperInvocation));
            notifyListenersAndVetoIfRequired(interactionResult);
        });

        var spec = targetAdapter.objSpec();
        if(spec.isEntity()) {
            return runExecutionTask(wrapperInvocation, ()->{
                MmEntityUtils.persistInCurrentTransaction(targetAdapter);
                return null;
            });
        }
        return null;

    }

    private Object handleGetterMethodOnProperty(
            final WrapperInvocation wrapperInvocation,
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToOneAssociation property) {

        zeroArgsElseThrow(args, "get");

        runValidationTask(wrapperInvocation, ()->{
            checkVisibility(wrapperInvocation, targetAdapter, property);
        });

        return runExecutionTask(wrapperInvocation, ()->{

            var interactionInitiatedBy = getInteractionInitiatedBy(wrapperInvocation);
            var currentReferencedAdapter = property.get(targetAdapter, interactionInitiatedBy);

            var currentReferencedObj = MmUnwrapUtils.single(currentReferencedAdapter);

            mmc.getWrapperFactory().notifyListeners(new PropertyAccessEvent(
                    targetAdapter.getPojo(), 
                    property.getFeatureIdentifier(), 
                    currentReferencedObj));
            return currentReferencedObj;

        });

    }

    private Object handleSetterMethodOnProperty(
            final WrapperInvocation wrapperInvocation,
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToOneAssociation property) {

        var singleArg = singleArgUnderlyingElseNull(args, "setter");

        runValidationTask(wrapperInvocation, ()->{
            checkVisibility(wrapperInvocation, targetAdapter, property);
            checkUsability(wrapperInvocation, targetAdapter, property);
        });

        var argumentAdapter = property.getObjectManager().adapt(singleArg);

        runValidationTask(wrapperInvocation, ()->{
            var interactionResult = property.isAssociationValid(
                    targetAdapter, argumentAdapter, getInteractionInitiatedBy(wrapperInvocation))
                    .getInteractionResult();
            notifyListenersAndVetoIfRequired(interactionResult);
        });

        return runExecutionTask(wrapperInvocation, ()->{
            property.set(targetAdapter, argumentAdapter, getInteractionInitiatedBy(wrapperInvocation));
            return null;
        });

    }

    private Object handleGetterMethodOnCollection(
            final WrapperInvocation wrapperInvocation,
            final ManagedObject targetAdapter,
            final Object[] args,
            final OneToManyAssociation collection,
            final String memberId) {

        zeroArgsElseThrow(args, "get");

        runValidationTask(wrapperInvocation, ()->{
            checkVisibility(wrapperInvocation, targetAdapter, collection);
        });

        return runExecutionTask(wrapperInvocation, ()->{

            var interactionInitiatedBy = getInteractionInitiatedBy(wrapperInvocation);
            var currentReferencedAdapter = collection.get(targetAdapter, interactionInitiatedBy);

            var currentReferencedObj = MmUnwrapUtils.single(currentReferencedAdapter);

            var collectionAccessEvent = new CollectionAccessEvent(currentReferencedObj, collection.getFeatureIdentifier());

            if (currentReferencedObj instanceof Collection) {
                var collectionViewObject = wrapCollection(
                        (Collection<?>) currentReferencedObj, 
                        collection);
                mmc.getWrapperFactory().notifyListeners(collectionAccessEvent);
                return collectionViewObject;
            } else if (currentReferencedObj instanceof Map) {
                var mapViewObject = wrapMap( 
                        (Map<?, ?>) currentReferencedObj,
                        collection);
                mmc.getWrapperFactory().notifyListeners(collectionAccessEvent);
                return mapViewObject;
            }

            var msg = String.format("Collection type '%s' not supported by framework", currentReferencedObj.getClass().getName());
            throw new IllegalArgumentException(msg);
        });

    }

    private Collection<?> wrapCollection(
            final Collection<?> collectionToLookup,
            final OneToManyAssociation otma) {
        if(proxyGenerator == null) {
            throw new IllegalStateException("Unable to create proxy for collection; "
                    + "proxyContextHandler not provided");
        }
        return proxyGenerator.collectionProxy(collectionToLookup, otma);
    }

    private Map<?, ?> wrapMap(
            final Map<?, ?> mapToLookup,
            final OneToManyAssociation otma) {
        if(proxyGenerator == null) {
            throw new IllegalStateException("Unable to create proxy for collection; "
                    + "proxyContextHandler not provided");
        }
        return proxyGenerator.mapProxy(mapToLookup, otma);
    }

    private Object handleActionMethod(
            final WrapperInvocation wrapperInvocation,
            final ManagedObject targetAdapter,
            final Object[] args,
            final ObjectAction objectAction) {

        var head = objectAction.interactionHead(targetAdapter);
        var objectManager = objectAction.getObjectManager();

        // adapt argument pojos to managed objects
        var argAdapters = objectAction.getParameterTypes().map(IndexedFunction.zeroBased((paramIndex, paramSpec)->{
            // guard against index out of bounds
            var argPojo = _Arrays.get(args, paramIndex).orElse(null);
            return argPojo!=null
                    ? objectManager.adapt(argPojo)
                    : ManagedObject.empty(paramSpec);
        }));

        runValidationTask(wrapperInvocation, ()->{
            checkVisibility(wrapperInvocation, targetAdapter, objectAction);
            checkUsability(wrapperInvocation, targetAdapter, objectAction);
            checkValidity(wrapperInvocation, head, objectAction, argAdapters);
        });

        return runExecutionTask(wrapperInvocation, ()->{
            var interactionInitiatedBy = getInteractionInitiatedBy(wrapperInvocation);

            var returnedAdapter = objectAction.execute(
                    head, argAdapters,
                    interactionInitiatedBy);
            return MmUnwrapUtils.single(returnedAdapter);

        });

    }

    private void checkValidity(
            final WrapperInvocation wrapperInvocation,
            final ActionInteractionHead head,
            final ObjectAction objectAction,
            final Can<ManagedObject> argAdapters) {

        var interactionResult = objectAction
                .isArgumentSetValid(head, argAdapters, getInteractionInitiatedBy(wrapperInvocation))
                .getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private Object underlying(final Object arg) {
        if (arg instanceof WrappingObject wrappingObject) {
            return wrappingObject.__causeway_origin().pojo();
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
            final WrapperInvocation wrapperInvocation,
            final ManagedObject targetObjectAdapter,
            final ObjectMember objectMember) {

        var visibleConsent = objectMember.isVisible(targetObjectAdapter, getInteractionInitiatedBy(wrapperInvocation), where);
        var interactionResult = visibleConsent.getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    private void checkUsability(
            final WrapperInvocation wrapperInvocation,
            final ManagedObject targetObjectAdapter,
            final ObjectMember objectMember) {

        var interactionResult = objectMember.isUsable(
                targetObjectAdapter,
                getInteractionInitiatedBy(wrapperInvocation),
                where)
                .getInteractionResult();
        notifyListenersAndVetoIfRequired(interactionResult);
    }

    // -- NOTIFY LISTENERS

    private void notifyListenersAndVetoIfRequired(final InteractionResult interactionResult) {
        var interactionEvent = interactionResult.getInteractionEvent();
        
        mmc.getWrapperFactory().notifyListeners(interactionEvent);
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

    private void runValidationTask(final WrapperInvocation wrapperInvocation, final Runnable task) {
        if(!wrapperInvocation.shouldEnforceRules()) {
            return;
        }
        try {
            task.run();
        } catch(Exception ex) {
            handleException(wrapperInvocation, ex);
        }
    }

    private <X> X runExecutionTask(final WrapperInvocation wrapperInvocation, final Supplier<X> task) {
        if(!wrapperInvocation.shouldExecute()) {
            return null;
        }
        try {
            return task.get();
        } catch(Exception ex) {
            return _Casts.uncheckedCast(handleException(wrapperInvocation, ex));
        }
    }

    @SneakyThrows
    private Object handleException(WrapperInvocation wrapperInvocation, final Exception ex) {
        var exceptionHandler = wrapperInvocation.origin().syncControl().getExceptionHandler()
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
        var argumentObj = underlying(args[0]);
        return argumentObj;
    }

    private void zeroArgsElseThrow(final Object[] args, final String name) {
        if (!_NullSafe.isEmpty(args)) {
            throw new IllegalArgumentException(String.format(
                    "Invoking '%s' should have no arguments", name));
        }
    }

}
