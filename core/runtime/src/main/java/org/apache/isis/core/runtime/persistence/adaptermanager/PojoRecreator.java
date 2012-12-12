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
package org.apache.isis.core.runtime.persistence.adaptermanager;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;

public interface PojoRecreator {

    Object recreatePojo(final TypedOid oid);

    /**
     * Return an adapter, if possible, for a pojo that was instantiated by the
     * object store as a result of lazily loading, but which hasn't yet been seen
     * by the Isis framework.
     * 
     * <p>
     * For example, in the case of JDO object store, downcast to <tt>PersistenceCapable</tt>
     * and 'look inside' its state.
     */

    ObjectAdapter lazilyLoaded(Object pojo);
    
}
