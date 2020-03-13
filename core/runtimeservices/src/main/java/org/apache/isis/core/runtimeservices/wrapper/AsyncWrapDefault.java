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
package org.apache.isis.core.runtimeservices.wrapper;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.wrapper.AsyncWrap;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;
import org.apache.isis.applib.services.MethodReferences.Call1;
import org.apache.isis.applib.services.MethodReferences.Call2;
import org.apache.isis.applib.services.MethodReferences.Call3;
import org.apache.isis.applib.services.MethodReferences.Call4;
import org.apache.isis.applib.services.MethodReferences.Call5;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.runtime.session.IsisSessionFactory;
import org.apache.isis.core.security.authentication.AuthenticationSessionTracker;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;
/**
 * 
 * @since 2.0
 *
 */
@Log4j2
class AsyncWrapDefault<T> implements AsyncWrap<T> {
    
    @NonNull
    private final WrapperFactory wrapperFactory;
    
    @NonNull
    private final IsisSessionFactory isisSessionFactory;
    
    @NonNull
    private final AuthenticationSessionTracker authenticationSessionTracker;
    
    @NonNull
    private final TransactionService transactionService;
    
    @NonNull
    private final BookmarkService bookmarkService;

    @NonNull
    private final Bookmark bookmark;

    private final Class<T> domainClass;

    @Getter(onMethod = @__(@Override))
    private ImmutableEnumSet<ExecutionMode> executionModes;

    private ImmutableEnumSet<ExecutionMode> executionModesSync;

    private ImmutableEnumSet<ExecutionMode> executionModesAsync;

    @NonNull
    @Getter(onMethod = @__({@Override})) 
    private ExecutorService executorService;
    
    @NonNull
    @Getter(onMethod = @__({@Override})) 
    private Consumer<Exception> exceptionHandler;

    @NonNull
    @Getter(onMethod = @__({@Override}))
    private WrapperFactory.RuleCheckingPolicy ruleCheckingPolicy;

    AsyncWrapDefault(
            final WrapperFactory wrapperFactory,
            final IsisSessionFactory isisSessionFactory,
            final AuthenticationSessionTracker authenticationSessionTracker,
            final TransactionService transactionService,
            final BookmarkService bookmarkService,
            final T domainObject,
            final ImmutableEnumSet<ExecutionMode> executionModes,
            final ExecutorService executorService,
            final Consumer<Exception> exceptionHandler,
            final WrapperFactory.RuleCheckingPolicy ruleCheckingPolicy) {

        this.wrapperFactory = wrapperFactory;
        this.isisSessionFactory = isisSessionFactory;
        this.authenticationSessionTracker = authenticationSessionTracker;
        this.transactionService = transactionService;
        this.bookmarkService = bookmarkService;
        this.bookmark = bookmarkService.bookmarkFor(domainObject);
        this.domainClass = (Class<T>) domainObject.getClass();
        this.executionModes = executionModes;

        withRuleCheckingPolicy(ruleCheckingPolicy);
        withExecutorService(executorService);
        withExceptionHandler(exceptionHandler);
    }

    private void deriveExecutionModes() {

        if(executionModes.contains(ExecutionMode.SKIP_RULE_VALIDATION)) {
            // ruleCheckingPolicy doesn't matter
            executionModesSync = WrapperFactory.ExecutionModes.NOOP;
            executionModesAsync = executionModes;
        } else {
            switch (ruleCheckingPolicy) {
                case SYNC:
                    // move rule validation to be performed synchronously.
                    executionModesSync = WrapperFactory.ExecutionModes.NO_EXECUTE;  // ie do check rules.
                    val tmp = executionModes.toEnumSet();
                    tmp.add(ExecutionMode.SKIP_RULE_VALIDATION);
                    this.executionModesAsync = ImmutableEnumSet.from(tmp);
                    break;
                case ASYNC:
                    executionModesSync = WrapperFactory.ExecutionModes.NOOP;
                    this.executionModesAsync = executionModes;
                    break;
            }
        }
    }


    /**
     * Fine-tune the executor.
     */
    public AsyncWrap<T> withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    /**
     * Fine-tune the exception handler.
     */
    @Override
    public AsyncWrap<T> withExceptionHandler(Consumer<Exception> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    /**
     * Adjust whether rule checking happens immediately or asynchronously.
     */
    @Override
    public AsyncWrap<T> withRuleCheckingPolicy(WrapperFactory.RuleCheckingPolicy ruleCheckingPolicy) {
        this.ruleCheckingPolicy = ruleCheckingPolicy;
        deriveExecutionModes();
        return this;
    }

    // -- METHOD REFERENCE MATCHERS (WITH RETURN VALUE)

    @Override
    public <R> Future<R> call(Call1<? extends R, ? super T> action) {

        if(shouldValidateSync()) {
            T lookup = bookmarkService.lookup(bookmark, this.domainClass);
            val proxy = wrapperFactory.wrap(lookup, executionModesSync);
            action.call(proxy);
        }
        
        if(shouldValidateAsync() || shouldExecuteAsync()) {
            return submit(()-> {

                final T lookup = bookmarkService.lookup(bookmark, this.domainClass);
                val proxy = wrapperFactory.wrap(lookup, executionModesAsync);
                return action.call(proxy);
            });
        }
        
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public <R, A1> Future<R> call(Call2<? extends R, ? super T, A1> action, A1 arg1) {

        if(shouldValidateSync()) {
            T lookup = bookmarkService.lookup(bookmark, this.domainClass);
            val proxy = wrapperFactory.wrap(lookup, executionModesSync);
            action.call(proxy, arg1);
        }

        if(shouldValidateAsync() || shouldExecuteAsync()) {
            return submit(()-> {

                final T lookup = bookmarkService.lookup(bookmark, this.domainClass);
                val proxy = wrapperFactory.wrap(lookup, executionModesAsync);
                return action.call(proxy, arg1);
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <R, A1, A2> Future<R> call(Call3<? extends R, ? super T, A1, A2> action, A1 arg1, A2 arg2) {

        if(shouldValidateSync()) {
            T lookup = bookmarkService.lookup(bookmark, this.domainClass);
            val proxy = wrapperFactory.wrap(lookup, executionModesSync);
            action.call(proxy, arg1, arg2);
        }

        if(shouldValidateAsync() || shouldExecuteAsync()) {
            return submit(()-> {

                final T lookup = bookmarkService.lookup(bookmark, this.domainClass);
                val proxy = wrapperFactory.wrap(lookup, executionModesAsync);
                return action.call(proxy, arg1, arg2);
            });
        }
        
        return CompletableFuture.completedFuture(null);
    }
    

    @Override
    public <R, A1, A2, A3> Future<R> call(Call4<? extends R, ? super T, A1, A2, A3> action, 
            A1 arg1, A2 arg2, A3 arg3) {
        
        if(shouldValidateSync()) {
            T lookup = bookmarkService.lookup(bookmark, this.domainClass);
            val proxy = wrapperFactory.wrap(lookup, executionModesSync);
            action.call(proxy, arg1, arg2, arg3);
        }

        if(shouldValidateAsync() || shouldExecuteAsync()) {
            return submit(()-> {

                final T lookup = bookmarkService.lookup(bookmark, this.domainClass);
                val proxy = wrapperFactory.wrap(lookup, executionModesAsync);
                return action.call(proxy, arg1, arg2, arg3);
            });
        }
        
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public <R, A1, A2, A3, A4> Future<R> call(Call5<? extends R, ? super T, A1, A2, A3, A4> action, 
            A1 arg1, A2 arg2, A3 arg3, A4 arg4) {

        if(shouldValidateSync()) {
            T lookup = bookmarkService.lookup(bookmark, this.domainClass);
            val proxy = wrapperFactory.wrap(lookup, executionModesSync);
            action.call(proxy, arg1, arg2, arg3, arg4);
        }

        if(shouldValidateAsync() || shouldExecuteAsync()) {
            return submit(()-> {

                final T lookup = bookmarkService.lookup(bookmark, this.domainClass);
                val proxy = wrapperFactory.wrap(lookup, executionModesAsync);
                return action.call(proxy, arg1, arg2, arg3, arg4);
            });
        }
        
        return CompletableFuture.completedFuture(null);
    }
    

    // -- SUBMISSION
    
    private <R> Future<R> submit(Supplier<R> actionInvocation) {
        
        val authenticationSession = authenticationSessionTracker.getAuthenticationSessionElseFail();
        
        Callable<R> asyncTask = ()->{

            try {
                return isisSessionFactory.callAuthenticated(authenticationSession,
                        ()->transactionService.executeWithinTransaction(actionInvocation));

            } catch (Exception e) {

//              val wrappedMethod = (Method) 
//              proxy.getClass().getMethod("__isis_wrappedMethod", _Constants.emptyClasses);

                Object lookup = bookmarkService.lookup(bookmark);
                val msg = 
                        String.format("Async execution of action '%s' on domain object '%s' failed.",
                                "[cannot resolve method name - not implemented]",//wrappedMethod.getName(),
                                bookmark);
                
                log.warn(msg, e);
                
                exceptionHandler.accept(_Exceptions.unrecoverable(msg, e));

                return null;
            }
        };
        
        return executorService.submit(asyncTask);
    }
    
    // -- HELPER
    
    private boolean shouldValidateSync() {
        return !this.executionModesSync.contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }

    private boolean shouldValidateAsync() {
        return !this.executionModesAsync.contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }

    private boolean shouldExecuteAsync() {
        return !executionModesAsync.contains(ExecutionMode.SKIP_EXECUTION);
    }

}
