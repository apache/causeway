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
package org.apache.isis.applib.services.wrapper.control;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.commons.collections.ImmutableEnumSet;

/**
 * Whether interactions with the wrapper are actually passed onto the
 * underlying domain object.
 *
 * @see WrapperFactory#wrap(Object, org.apache.isis.applib.services.wrapper.control.SyncControl)
 * 
 * @since 2.0 {@index}
 */
public enum ExecutionMode {
    /**
     * Skip all business rules.
     */
    SKIP_RULE_VALIDATION,
    /**
     * Skip actual execution.
     *
     * <p>
     * This is not supported for {@link WrapperFactory#asyncWrap(Object, AsyncControl)}; 
     * instead just invoke {@link WrapperFactory#wrap(Object, SyncControl)}.
     */
    SKIP_EXECUTION,
}
