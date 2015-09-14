/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.runtimecontext;

import java.util.List;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public interface DomainObjectServices extends Injectable {

    // ///////////////////////////////////////////
    // Instantiate
    // ///////////////////////////////////////////

    /**
     * Provided by the <tt>PersistenceSession</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    ObjectAdapter createTransientInstance(ObjectSpecification spec);

    ObjectAdapter createViewModelInstance(ObjectSpecification spec, String memento);


    // ///////////////////////////////////////////
    // services
    // ///////////////////////////////////////////

    void injectServicesInto(Object domainObject);

    <T> T lookupService(Class<T> service);

    <T> Iterable<T> lookupServices(Class<T> service);

    // ///////////////////////////////////////////
    // retrieve
    // ///////////////////////////////////////////

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    void resolve(Object parent);

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    void resolve(Object parent, Object field);

    /**
     * Provided by <tt>PersistenceSession</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>BookmarkServicesDefault</tt>.
     * @return 
     */
    Object lookup(Bookmark bookmark);

    Bookmark bookmarkFor(Object domainObject);

    Bookmark bookmarkFor(Class<?> cls, String identifier);

    
    // ///////////////////////////////////////////
    // flush, commit
    // ///////////////////////////////////////////

    /**
     * Provided by <tt>TransactionManager</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    boolean flush();

    /**
     * Provided by <tt>TransactionManager</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    void commit();

    // //////////////////////////////////////////////////////////////////
    // info, warn, error messages
    // //////////////////////////////////////////////////////////////////

    /**
     * Provided by <tt>MessageBroker</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    void informUser(String message);

    /**
     * Provided by <tt>MessageBroker</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    void warnUser(String message);

    /**
     * Provided by <tt>MessageBroker</tt> when used by framework.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    void raiseError(String message);

    // //////////////////////////////////////////////////////////////////
    // properties
    // //////////////////////////////////////////////////////////////////

    /**
     * Provided by {@link RuntimeContextAbstract} itself, cloned properties from
     * {@link IsisConfiguration}.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    String getProperty(String name);

    /**
     * Provided by {@link RuntimeContextAbstract} itself, cloned properties from
     * {@link IsisConfiguration}.
     * 
     * <p>
     * Called by <tt>DomainObjectContainerDefault</tt>.
     */
    List<String> getPropertyNames();

}
