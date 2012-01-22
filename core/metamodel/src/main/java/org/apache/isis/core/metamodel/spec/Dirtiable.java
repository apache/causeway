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

package org.apache.isis.core.metamodel.spec;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;

public interface Dirtiable {

    /**
     * Clear the dirty flag so that a call to <tt>isDirty()</tt>, and before
     * <tt>markDirty()</tt> is called, will return false;
     * 
     * @see #isDirty(ObjectAdapter)
     * @see #markDirty(ObjectAdapter)
     */
    void clearDirty(ObjectAdapter object);

    /**
     * Checks if the specified object has been changed, and hence needs
     * persisting.
     * 
     * @see #markDirty(ObjectAdapter)
     * @see #clearDirty(ObjectAdapter)
     */
    boolean isDirty(ObjectAdapter object);

    /**
     * Mark the specified object as having been changed, and hence needing
     * persisting.
     * 
     * @see #isDirty(ObjectAdapter)
     * @see #clearDirty(ObjectAdapter)
     */
    void markDirty(ObjectAdapter object);

}
