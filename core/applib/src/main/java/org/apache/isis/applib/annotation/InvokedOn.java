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

/**
 * How an action was invoked.
 */
public enum InvokedOn {
    OBJECT,
    COLLECTION;

    public boolean isObject() { return this == OBJECT; }
    public boolean isCollection() { return this == COLLECTION; }

    /**
     * @deprecated - because the {@link Bulk} annotation is deprecated.
     */
    @Deprecated
    public static InvokedOn from(final Bulk.InteractionContext.InvokedAs invokedAs) {
        if (invokedAs == null) return null;
        if (invokedAs == Bulk.InteractionContext.InvokedAs.REGULAR) return OBJECT;
        if (invokedAs == Bulk.InteractionContext.InvokedAs.BULK) return COLLECTION;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized bulk interactionContext invokedAs: " + invokedAs);
    }

    /**
     * @deprecated - because the {@link Bulk} annotation is deprecated.
     */
    @Deprecated
    public static Bulk.InteractionContext.InvokedAs from(final InvokedOn invokedOn) {
        if (invokedOn == null) return null;
        if (invokedOn == OBJECT) return Bulk.InteractionContext.InvokedAs.REGULAR;
        if (invokedOn == COLLECTION) return Bulk.InteractionContext.InvokedAs.BULK;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized bulk interactionContext invokedAs: " + invokedOn);
    }


}
