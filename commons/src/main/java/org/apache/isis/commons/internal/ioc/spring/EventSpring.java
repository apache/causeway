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
package org.apache.isis.commons.internal.ioc.spring;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;

import org.springframework.context.ApplicationEventPublisher;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
class EventSpring<T> implements Event<T> {

    private final ApplicationEventPublisher publisher;

    @Override
    public void fire(T event) {

        if(log.isDebugEnabled()) {
            log.debug("{} fire({} ... {})",
                    Thread.currentThread().getName(),
                    event.getClass().getSimpleName(), 
                    event.toString());
        }

        publisher.publishEvent(event);
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync(U event) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <U extends T> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
        throw _Exceptions.notImplemented();    }

    @Override
    public Event<T> select(Annotation... qualifiers) {
        throw _Exceptions.notImplemented();    }

    @Override
    public <U extends T> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
        throw _Exceptions.notImplemented();    }

    @Override
    public <U extends T> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        throw _Exceptions.notImplemented();    }

}
