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

package org.apache.isis.objectstore.jdo.applib.service.support;

import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Service that provide a number of workarounds when using JDO/DataNucleus. 
 */
public interface IsisJdoSupport {

    /**
     * Force a reload (corresponding to the JDO <tt>PersistenceManager</tt>'s <tt>refresh()</tt> method)
     * of a domain objects.
     * 
     * <p>
     * In fact, this may just reset the lazy-load state of the domain object, but the effect is the same: 
     * to cause the object's state to be reloaded from the database.
     * 
     * <p>
     * The particular example that led to this method being added was a 1:m bidirectional relationship,
     * analogous to <tt>Customer <-> * Order</tt>.  Persisting the child <tt>Order</tt> object did not cause
     * the parent <tt>Customer</tt>'s collection of orders to be updated.  In fact, JDO does not make any
     * such guarantee to do so.  Options are therefore either to maintain the collection in code, or to
     * refresh the parent.
     */
    @Programmatic
    <T> T refresh(T domainObject);
    
    @Programmatic
    void ensureLoaded(Collection<?> collectionOfDomainObjects);
    
    @Programmatic
    PersistenceManager getJdoPersistenceManager();
    
}
