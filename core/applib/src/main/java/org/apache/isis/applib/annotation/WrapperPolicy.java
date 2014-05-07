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

import org.apache.isis.applib.services.wrapper.WrapperFactory;

/**
 * An attribute of {@link PostsPropertyChangedEvent}, {@link PostsCollectionAddedToEvent} and other related annotations;
 * is a hint to indicate that if the object member is interacted with through a {@link WrapperFactory}, then whether 
 * business rules (&quot;see it, use it, do it&quot;) should be enforced or not.
 * 
 * <p>
 * This provides a half-way house between strictly UI-interactions and fully programmatic interactions, so that an 
 * event can be fired programmatically even if the object is disabled in the UI.
 */
public enum WrapperPolicy {
    ENFORCE_RULES,
    SKIP_RULES
}