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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;

/**
 * Applies only to properties; any changes should be propagated as events to subscribers.  
 * Only posted after a successful validation.
 * 
 * <p>For example:
 * <pre>
 * public static class StartDateChangedEvent extends PropertyChangedEvent {}
 * 
 * &#64;PostsPropertyChangedEvent(StartDateChangedEvent.class)
 * public LocalDate getStartDate() { ...}
 * </pre>
 * 
 * <p>
 * Only domain services should be registered as subscribers; only domain services are guaranteed to be instantiated and
 * resident in memory.  The typical implementation of a domain service subscriber is to identify the impacted entities,
 * load them using a repository, and then to delegate to the event to them.
 * 
 * @deprecated - use instead {@link InteractWithProperty}.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PostsPropertyChangedEvent {

    /**
     * The subclass of {@link PropertyChangedEvent} to be instantiated and posted.
     * 
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     */
    Class<? extends PropertyChangedEvent<?,?>> value() default PropertyChangedEvent.Default.class;

}
