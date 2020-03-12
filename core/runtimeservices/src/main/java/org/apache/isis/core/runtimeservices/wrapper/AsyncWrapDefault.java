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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;
import lombok.val;
import lombok.extern.log4j.Log4j2;
/**
 * 
 * @since 2.0
 *
 */
@AllArgsConstructor
@Log4j2
class AsyncWrapDefault<T> implements AsyncWrap<T> {
    
    @With(AccessLevel.PACKAGE) @NonNull
    private WrapperFactory wrapper;
    
    @With(AccessLevel.PACKAGE) @NonNull
    private IsisSessionFactory isisSessionFactory;
    
    @With(AccessLevel.PACKAGE) @NonNull
    private AuthenticationSessionTracker authenticationSessionTracker;
    
    @With(AccessLevel.PACKAGE) @NonNull
    private TransactionService transactionService;
    
    @With(AccessLevel.PACKAGE) @NonNull
    private T domainObject;
    
    /*getter is API*/
    @Getter(onMethod = @__(@Override)) @With(AccessLevel.PACKAGE) @NonNull 
    private ImmutableEnumSet<ExecutionMode> executionMode;
    
    /*getter and wither are API*/
    @NonNull
    @Getter(onMethod = @__({@Override})) 
    @With(value = AccessLevel.PUBLIC, onMethod = @__(@Override)) 
    private ExecutorService executor;  
    
    /*getter and wither are API*/
    @NonNull
    @Getter(onMethod = @__({@Override})) 
    @With(value = AccessLevel.PUBLIC, onMethod = @__(@Override)) 
    private Consumer<Exception> exceptionHandler;

        
    // -- METHOD REFERENCE MATCHERS (WITH RETURN VALUE)
    
    @Override
    public <R> Future<R> call(Call1<? extends R, ? super T> action) {
        
        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.NO_EXECUTE);
            action.call(proxy_validateOnly);
        }
        
        if(shouldExecute()) {
            // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.SKIP_RULES);
            return submit(()->action.call(proxy_executeOnly));
        }
        
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public <R, A1> Future<R> call(Call2<? extends R, ? super T, A1> action, A1 arg1) {
        
        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.NO_EXECUTE);
            action.call(proxy_validateOnly, arg1);
        }
        
        if(shouldExecute()) {
            // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.SKIP_RULES);
            return submit(()->action.call(proxy_executeOnly, arg1));
        }
        
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public <R, A1, A2> Future<R> call(Call3<? extends R, ? super T, A1, A2> action, A1 arg1, A2 arg2) {

        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.NO_EXECUTE);
            action.call(proxy_validateOnly, arg1, arg2);
        }
        
        if(shouldExecute()) {
            // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.SKIP_RULES);
            return submit(()->action.call(proxy_executeOnly, arg1, arg2));
        }
        
        return CompletableFuture.completedFuture(null);
    }
    

    @Override
    public <R, A1, A2, A3> Future<R> call(Call4<? extends R, ? super T, A1, A2, A3> action, 
            A1 arg1, A2 arg2, A3 arg3) {
        
        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.NO_EXECUTE);
            action.call(proxy_validateOnly, arg1, arg2, arg3);
        }
        
        if(shouldExecute()) {
         // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.SKIP_RULES);
            return submit(()->action.call(proxy_executeOnly, arg1, arg2, arg3));
        }
        
        return CompletableFuture.completedFuture(null);
    }


    @Override
    public <R, A1, A2, A3, A4> Future<R> call(Call5<? extends R, ? super T, A1, A2, A3, A4> action, 
            A1 arg1, A2 arg2, A3 arg3, A4 arg4) {

        if(shouldValidate()) {
            // do validation synchronous (with the calling thread)
            val proxy_validateOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.NO_EXECUTE);
            action.call(proxy_validateOnly, arg1, arg2, arg3, arg4);
        }
        
        if(shouldExecute()) {
            // to also trigger domain events, we need a proxy, but validation (if required)
            // was already done above
            val proxy_executeOnly = wrapper.wrap(domainObject, WrapperFactory.ExecutionModes.SKIP_RULES);
            return submit(()->action.call(proxy_executeOnly, arg1, arg2, arg3, arg4));
        }
        
        return CompletableFuture.completedFuture(null);
    }
    

    // -- SUBMISSION
    
    private <R> Future<R> submit(Supplier<R> actionInvocation) {
        
        val authenticationSession = authenticationSessionTracker.getAuthenticationSessionElseFail();
        
        Callable<R> asyncTask = ()->{

            try {
                //transactionLatch.await(); // wait for transaction of the calling thread to complete

                return isisSessionFactory.callAuthenticated(authenticationSession,
                        ()->transactionService.executeWithinTransaction(actionInvocation));

            } catch (Exception e) {

//              val wrappedMethod = (Method) 
//              proxy.getClass().getMethod("__isis_wrappedMethod", _Constants.emptyClasses);
      
                val msg = 
                        String.format("Async execution of action '%s' on type '%s' failed.",
                                "[cannot resolve method name - not implemented]",//wrappedMethod.getName(),
                                domainObject.getClass());
                
                log.warn(msg, e);
                
                exceptionHandler.accept(_Exceptions.unrecoverable(msg, e));

                return null;
            }
        };
        
        return executor.submit(asyncTask);
    }
    
    // -- HELPER
    
    private boolean shouldValidate() {
        return !executionMode.contains(ExecutionMode.SKIP_RULE_VALIDATION);
    }
    
    private boolean shouldExecute() {
        return !executionMode.contains(ExecutionMode.SKIP_EXECUTION);
    }



}
