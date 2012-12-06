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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.Filter;
import org.apache.isis.applib.RepositoryException;


public abstract class AbstractGenericFactoryAndRepository extends AbstractService {

	private DomainGenericObjectContainer genericContainer;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected DomainGenericObjectContainer getGenericContainer() {
        return this.genericContainer;
    }

    /**
     * Injected by the application container itself.
     */
    public final void setGenericContainer(final DomainGenericObjectContainer container) {
        this.genericContainer = container;
    }
	
	
	
    /**
     * Returns all the instances of the specified type, including instances of any subclasses if the
     * includeSubclasses flag set. If there are no instances the list will be empty. This method creates a new
     * list object each time it is called so the caller is free to use or modify the returned List, but the
     * changes will not be reflected back to the repository.
     */
    protected <T> List<T> allInstances(final Class<T> cls, final boolean includeSubclasses) {
        T[] instances = getGenericContainer().allInstances(cls, includeSubclasses);
        return asList(instances);
    }

    /**
     * Returns all the instances of the specified type that the filter object accepts. This method will also
     * search through the instances of any subclasses if the includeSubclasses flag is set. If there are no
     * instances the list will be empty. This method creates a new list object each time it is called so the
     * caller is free to use or modify the returned List, but the changes will not be reflected back to the
     * repository.
     */
    protected <T> List<T> allMatches(final Class<T> cls, final Filter filter, final boolean includeSubclasses) {
        T[] allInstances = getGenericContainer().allInstances(cls, includeSubclasses);
        List<T> filtered = new ArrayList<T>();
        for (int i = 0; i < allInstances.length; i++) {
            T instance = allInstances[i];
            if (filter.accept(instance)) {
                filtered.add(instance);
            }
        }
        return filtered;
    }

    /**
     * Returns all the instances of the specified type that match the given object: where any property that is
     * set will be tested and properties that are not set will be ignored. This method will also search though
     * the instances of any subclasses if the includeSubclasses flag is set. If there are no instances the
     * list will be empty. This method creates a new list object each time it is called so the caller is free
     * to use or modify the returned List, but the changes will not be reflected back to the repository.
     */
    protected <T> List<T> allMatches(final Class<T> cls, final Object pattern, final boolean includeSubclasses) {
        T[] instances = getGenericContainer().findInstances(cls, pattern, includeSubclasses);
        return asList(instances);
    }

    /**
     * Returns all the instances of the specified type that have the given title. This method will also search
     * through the instances of any subclasses if the includeSubclasses flag is set. If there are no instances
     * the list will be empty. This method creates a new list object each time it is called so the caller is
     * free to use or modify the returned List, but the changes will not be reflected back to the repository.
     */
    protected <T> List<T> allMatches(final Class<T> cls, final String title, final boolean includeSubclasses) {
        T[] instances = getGenericContainer().findInstances(cls, title, includeSubclasses);
        return asList(instances);
    }

    private <T> List<T> asList(T[] instances) {
        List<T> list = Arrays.asList(instances);
        return list;
    }

    /**
     * Returns the first instance that matches the supplied filter, or null if none. This method will also
     * search though the instances of any subclasses if the includeSubclasses flag is set.
     */
    protected <T> T firstMatch(final Class<T> cls, final Filter filter, final boolean includeSubclasses) {
        T[] allInstances = getGenericContainer().allInstances(cls, includeSubclasses);
        for (int i = 0; i < allInstances.length; i++) {
            T instance = allInstances[i];
            if (filter.accept(instance)) {
                return instance;
            }
        }
        return null;
    }

    /**
     * Returns the first instance that matches the supplied filter, or null if none. This method will also
     * search though the instances of any subclasses if the includeSubclasses flag is set.
     */
    protected <T> T firstMatch(final Class<T> cls, final Object pattern, final boolean includeSubclasses) {
        return (T) getGenericContainer().firstInstance(cls, pattern, includeSubclasses);
    }

    /**
     * Returns the first instance that matches the supplied filter, or null if none. This method will also
     * search though the instances of any subclasses if the includeSubclasses flag is set.
     */
    protected <T> T firstMatch(final Class<T> cls, final String title, final boolean includeSubclasses) {
        return (T) getGenericContainer().firstInstance(cls, title, includeSubclasses);
    }

    /**
     * Returns a new instance of the specified class with a persisted state the same as the other object.
     * Therefore when the object is transient the new instance will not be persisted once it is created,
     * whereas if the object is persistent, then the new instance will be persisted once created.
     */
    protected <T> T newInstance(final Class<T> ofClass, final Object sameStateAs) {
        return getGenericContainer().newInstance(ofClass, sameStateAs);
    }

    /**
     * Returns a new instance of the specified class that will be persisted before returned.
     */
    protected <T> T newPersistentInstance(final Class<T> ofClass) {
        return getGenericContainer().newPersistentInstance(ofClass);
    }

    /**
     * Returns a new instance of the specified class that will not have been persisted.
     */
    protected <T> T newTransientInstance(final Class<T> ofClass) {
        return getGenericContainer().newTransientInstance(ofClass);
    }

    /**
     * Find the only instance of the specified type that has the specified title. If the includeSubclasses
     * flag is set then the search will also be include instances of all subclasses. If no instance is found
     * then null will be return, while if there is more that one instances a run-time exception will be
     * thrown.
     */
    protected <T> T uniqueMatch(final Class<T> cls, final Filter filter, final boolean includeSubclasses) {
        List<T> instances = allMatches(cls, filter, includeSubclasses);
        if (instances.size() == 1) {
            return instances.get(0);
        } else if (instances.size() == 0) {
            return null;
        } else {
            throw new RepositoryException("Found more that one instance matching filter " + filter);
        }
    }

    /**
     * Find the only instance of the specified type that has the specified title. If the includeSubclasses
     * flag is set then the search will also be include instances of all subclasses. If no instance is found
     * then null will be return, while if there is more that one instances a run-time exception will be
     * thrown.
     */
    protected <T> T uniqueMatch(final Class<T> cls, final String title, final boolean includeSubclasses) {
        return getGenericContainer().findInstance(cls, title, includeSubclasses);
    }

    /**
     * Find the only instance of the patterned object type that matches the set fields in the pattern object:
     * where any property that is set will be tested and properties that are not set will be ignored. If the
     * includeSubclasses flag is set then the search will also be include instances of all subclasses. If no
     * instance is found then null will be return, while if there is more that one instances a run-time
     * exception will be thrown.
     */
    protected <T> T uniqueMatch(final Class<T> cls, final T pattern, final boolean includeSubclasses) {
        return getGenericContainer().findInstance(cls, pattern, includeSubclasses);
    }
}

