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


package org.apache.isis.core.metamodel.adapter.oid;

import org.apache.isis.core.commons.encoding.Encodable;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public interface Oid extends Encodable {
    
    /**
     * Copies the content of the specified oid into this oid.
     * 
     * <p>
     * After this call the {@link #hashCode()} return by both the specified object and this 
     * object will be the same, and both objects will be {@link #equals(Object) equal}.
     */
    void copyFrom(Oid oid);

    /**
     * Returns the pending oid if there is one.
     * 
     * @see #hasPrevious()
     * @see #clearPrevious()
     */
    Oid getPrevious();

    /**
     * Returns true if this oid contains a {@link #getPrevious() previous} value, specifically that the {@link Oid} was changed from
     * transient to persistent.
     * 
     * @see #getPrevious()
     * @see #clearPrevious()
     */
    boolean hasPrevious();
    
    
    /**
     * Indicate that the {@link #getPrevious() previous value} has been used to remap the {@link ObjectAdapter adapter} 
     * and should not been cleared.
     * 
     * @see #getPrevious()
     * @see #hasPrevious()
     */
    void clearPrevious();

    /**
     * Flags whether this OID is temporary, and is for a transient object..
     */
    boolean isTransient();

    /**
     * Marks the Oid as persistent.
     */
    void makePersistent();
}
