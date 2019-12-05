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
package org.apache.isis.commons.handler;

import java.util.List;
import java.util.Optional;

import lombok.val;

/**
 * Building blocks for the <em>Chain of Responsibility</em> design pattern.
 * <p>
 * <em>Chain of Responsibility</em> allows passing requests along the chain of handlers, 
 * until one of them handles the request.
 *  
 * @since 2.0
 *
 * @param <X> request type
 * @param <R> response type
 */
public interface ChainOfResponsibility<X, R> {

    /**
     * The {@code request} is passed along the chain of handlers, until one of them handles the request.
     * @param request
     * @return response of the first handler that handled the request wrapped in an Optional, 
     * or an empty Optional, if no handler handled the request  
     */
    Optional<R> handle(X request);

    /**
     * A chain of responsibility is made up of handlers, that are asked in sequence, 
     * whether they handle a request. 
     * 
     * @since 2.0
     *
     * @param <X> request type
     * @param <R> response type
     */
    static interface Handler<X, R> {
        boolean isHandling(X request);
        R handle(X request);
    }

    /**
     * Creates a new ChainOfResponsibility of given {@code chainOfHandlers} 
     * @param <X>
     * @param <R>
     * @param chainOfHandlers
     * @return
     */
    static <X, R> ChainOfResponsibility<X, R> 
    of(
            final List<? extends ChainOfResponsibility.Handler<? super X, R>> chainOfHandlers) {
    
        return request -> {

            final Optional<R> responseIfAny = chainOfHandlers.stream()
                    .filter(h -> h.isHandling(request))
                    .findFirst()
                    .map(h -> h.handle(request));
            return responseIfAny;
        };
        
    }
    
}
