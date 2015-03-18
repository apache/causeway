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
 * Whether an action can be invoked on a single object and/or on many objects in a collection.
 */
public enum InvokeOn {
    /**
     * The action can only be invoked on a single object.  This is the default.
     */
    OBJECT_ONLY,
    /**
     * The action can be invoked either on a single object or on a collection of objects (each in turn).
     *
     * <p>
     *     Corresponds to (the deprecated) <code>@Bulk(appliesTo=BULK_AND_REGULAR)</code> annotation.
     * </p>
     */
    OBJECT_AND_COLLECTION,
    /**
     * The action is intended to be invoked only on a collection of objects (each in turn).
     *
     * <p>
     *     Corresponds to (the deprecated) <code>@Bulk(appliesTo=BULK_ONLY)</code> annotation.
     * </p>
     */
    COLLECTION_ONLY;

    @Deprecated
    public static Bulk.AppliesTo from(final InvokeOn appliesToPolicy) {
        if(appliesToPolicy == null) return null;
        if(appliesToPolicy == OBJECT_AND_COLLECTION) return Bulk.AppliesTo.BULK_AND_REGULAR;
        if(appliesToPolicy == COLLECTION_ONLY) return Bulk.AppliesTo.BULK_ONLY;
        if(appliesToPolicy == OBJECT_ONLY) throw new IllegalArgumentException("No corresponding Bulk.AppliesTo enum for " + appliesToPolicy);
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized appliesTo: " + appliesToPolicy);
    }

    @Deprecated
    public static InvokeOn from(final Bulk.AppliesTo appliesTo) {
        if(appliesTo == null) return null;
        if(appliesTo == Bulk.AppliesTo.BULK_AND_REGULAR) return OBJECT_AND_COLLECTION;
        if(appliesTo == Bulk.AppliesTo.BULK_ONLY) return COLLECTION_ONLY;
        // shouldn't happen
        throw new IllegalArgumentException("Unrecognized appliesTo: " + appliesTo);
    }
}
