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


package org.apache.isis.applib.generics;

import org.apache.isis.applib.security.UserMemento;


/**
 * Represents a container that the domain objects work within. It provides access to the persistence mechanism
 * and user interface.
 */
public interface DomainGenericObjectContainer {

    //////////////////////// resolve, objectChanged //////////////////////////
    
    /**
     * Ensure that the specified object is completely loaded into memory.
     * 
     * <p>
     * This forces the lazy loading mechanism to load the object if it is not already loaded.
     */
    void resolve(Object object);

    /**
     * Ensure that the specified object is completely loaded into memory, though only if the supplied field
     * reference is <tt>null</tt>.
     * 
     * <p>
     * This forces the lazy loading mechanism to load the object if it is not already loaded.
     */
    void resolve(Object object, Object field);

    /**
     * Flags that the specified object's state has changed and its changes need to be saved.
     */
    void objectChanged(Object object);

    
    //////////////////////// new{Transient/Persistent}Instance //////////////////////////

    /**
     * Create a new instance of the specified class, but do not persist it.
     * 
     * @see #newPersistentInstance(Class)
     */
    <T> T newTransientInstance(Class<T> ofClass);

    /**
     * Create a new instance of the specified class and persist it.
     * 
     * @see #newTransientInstance(Class)
     */
    <T> T newPersistentInstance(Class<T> ofClass);

    /**
     * Create a new instance, of the specified class, in the same persistent state as the specified object.
     * 
     * @see #newTransientInstance()
     * @see #newPersistentInstance()
     */
    <T> T newInstance(Class<T> ofClass, Object sameStateAs);

    
    //////////////////////// isPersistent, makePersistent, disposeInstance //////////////////////////

    /**
     * Determines if the specified object is persistent; that is is stored permanently outside of the virtual
     * machine.
     */
    boolean isPersistent(Object object);

    /**
     * Make the specified transient object persistent. Throws an exception if object is already persistent.
     */
    void makePersistent(Object transientObject);

    void disposeInstance(Object persistentObject);



    //////////////////////// info, warn, error //////////////////////////
    
    /**
     * Make the specified message available to the user. Note this will probably be displayed in transitory
     * fashion, so is only suitable for useful but optional information.
     * 
     * @see #warnUser(String)
     * @see #raiseError(String)
     */
    void informUser(String message);

    /**
     * Warn the user about a situation with the specified message. The container should guarantee to display
     * this warning to the user.
     * 
     * @see #raiseError(String)
     * @see #informUser(String)
     */
    void warnUser(String message);

    /**
     * Notify the user of an application error with the specified message. Note this will probably be
     * displayed in an alarming fashion, so is only suitable for errors
     * 
     * @see #warnUser(String)
     * @see #informUser(String)
     */
    void raiseError(String message);

    
    //////////////////////// security //////////////////////////

    /**
     * Get the details about the current user.
     */
    UserMemento getUser();



    //////////// allInstances, findInstances, findInstance, firstInstance ///////////////////

    /**
     * Use of this method is discouraged; instead use a repository.
     */
    <T> T[] allInstances(Class<T> cls, boolean includeSubclasses);

    /**
     * Use of this method is discouraged; instead use a repository.
     */
    <T> T[] findInstances(Class<T> cls, String title, boolean includeSubclasses);

    /**
     * Use of this method is discouraged; instead use a repository.
     */
    <T> T[] findInstances(Class<T> cls, Object pattern, boolean includeSubclasses);

    /**
     * Use of this method is discouraged; instead use a repository.
     */
    <T> T findInstance(Class<T> cls, String title, boolean includeSubclasses);

    /**
     * Use of this method is discouraged; instead use a repository.
     */
    <T> T findInstance(Class<T> cls, Object pattern, boolean includeSubclasses);

    /**
     * Use of this method is discouraged; instead use a repository.
     */
    <T> T firstInstance(Class<T> cls, String title, boolean includeSubclasses);

    /**
     * Use of this method is discouraged; instead use a repository.
     */
    <T> T firstInstance(Class<T> cls, Object pattern, boolean includeSubclasses);

    
}
