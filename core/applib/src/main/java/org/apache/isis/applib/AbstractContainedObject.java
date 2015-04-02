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

package org.apache.isis.applib;

import java.util.List;
import com.google.common.base.Predicate;
import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.i18n.TranslatableString;

/**
 * Convenience super class for all classes that wish to interact with the
 * container.
 * 
 * @see org.apache.isis.applib.DomainObjectContainer
 */
public abstract class AbstractContainedObject {

    /**
     * Create a new instance of the specified class, but do not persist it.
     *
     * <p>
     * It is recommended that the object be initially instantiated using
     * this method, though the framework will also handle the case when 
     * the object is simply <i>new()</i>ed up.  The benefits of using
     * {@link #newTransientInstance(Class)} are:
     * <ul>
     * <li>any services will be injected into the object immediately
     *     (otherwise they will not be injected until the framework
     *     becomes aware of the object, typically when it is 
     *     {@link #persist(Object) persist}ed</li>
     * <li>the default value for any properties (usually as specified by 
     *     <tt>default<i>Xxx</i>()</tt> supporting methods) will not be
     *     used</li>
     * <li>the <tt>created()</tt> callback will not be called.
     * </ul>
     * 
     * <p>
     * The corollary is: if your code never uses <tt>default<i>Xxx</i>()</tt> 
     * supporting methods or the <tt>created()</tt> callback, then you can
     * alternatively just <i>new()</i> up the object rather than call this
     * method. 

     * <p>
     * If the type is annotated with {@link Aggregated}, then as per
     * {@link #newAggregatedInstance(Object, Class)}.  Otherwise will be an
     * aggregate root.
     * 
     * @see #newAggregatedInstance(Object, Class)
     */
    protected <T> T newTransientInstance(final Class<T> ofType) {
        return getContainer().newTransientInstance(ofType);
    }

    /**
     * Create a new {@link ViewModel} of specified type, identified by memento.
     * 
     * @param ofType
     * @param memento
     * @return
     */
    protected <T extends ViewModel> T newViewModelInstance(final Class<T> ofType, final String memento) {
        return getContainer().newViewModelInstance(ofType, memento);
    }

    /**
     * Create an instance that will be persisted as part of this domain object
     * (ie this domain object is its parent in the aggregate).
     * 
     * <p>
     * The type provided should be annotated with {@link Aggregated}.
     * 
     *  @see #newAggregatedInstance(Object, Class)
     */
    protected <T> T newAggregatedInstance(final Class<T> ofType) {
        return newAggregatedInstance(this, ofType);
    }

    /**
     * Create an instance that will be persisted as part of specified paremt domain object
     * (ie that domain object will be its parent in the aggregate).
     * 
     * <p>
     * The type provided should be annotated with {@link Aggregated}.
     * 
     *  @see #newAggregatedInstance(Class)
     */
    protected <T> T newAggregatedInstance(final Object parent, final Class<T> ofType) {
        return getContainer().newAggregatedInstance(parent, ofType);
    }
    
    // //////////////////////////////////////

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#allInstances(Class, long...)
     */
    protected <T> List<T> allInstances(final Class<T> ofType, long... range) {
        return getContainer().allInstances(ofType, range);
    }

    // //////////////////////////////////////

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#allMatches(Class, Predicate, long...)
     */
    protected <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate, long... range) {
        return getContainer().allMatches(ofType, predicate, range);
    }
    
    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#allMatches(Class, Filter, long...)
     * 
     * @deprecated - use {@link #allMatches(Class, Predicate, long...)}
     */
    @Deprecated
    protected <T> List<T> allMatches(final Class<T> ofType, final Filter<? super T> filter, long... range) {
        return getContainer().allMatches(ofType, filter, range);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#allMatches(Class, Object, long...)
     */
    protected <T> List<T> allMatches(final Class<T> ofType, final T pattern, long... range) {
        return getContainer().allMatches(ofType, pattern, range);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#allMatches(Class, String, long...)
     */
    protected <T> List<T> allMatches(final Class<T> ofType, final String title, long... range) {
        return getContainer().allMatches(ofType, title, range);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#allMatches(Query)
     */
    protected <T> List<T> allMatches(final Query<T> query) {
        return getContainer().allMatches(query);
    }

    // //////////////////////////////////////

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#firstMatch(Class, Predicate)
     * 
     */
    protected <T> T firstMatch(final Class<T> ofType, final Predicate<T> predicate) {
        return getContainer().firstMatch(ofType, predicate);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#firstMatch(Class, Filter)
     * 
     * @deprecated - use {@link #firstMatch(Class, Predicate)}
     */
    @Deprecated
    protected <T> T firstMatch(final Class<T> ofType, final Filter<T> filter) {
        return getContainer().firstMatch(ofType, filter);
    }
    
    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#firstMatch(Class, Object)
     */
    protected <T> T firstMatch(final Class<T> ofType, final T pattern) {
        return getContainer().firstMatch(ofType, pattern);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#firstMatch(Class, String)
     */
    protected <T> T firstMatch(final Class<T> ofType, final String title) {
        return getContainer().firstMatch(ofType, title);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#firstMatch(Query)
     */
    protected <T> T firstMatch(final Query<T> query) {
        return getContainer().firstMatch(query);
    }

    // //////////////////////////////////////


    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#uniqueMatch(Class, Predicate)
     */
    protected <T> T uniqueMatch(final Class<T> ofType, final Predicate<T> predicate) {
        return getContainer().uniqueMatch(ofType, predicate);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#uniqueMatch(Class, Filter)
     * 
     * @deprecated - use {@link #uniqueMatch(Class, Predicate)}
     */
    @Deprecated
    protected <T> T uniqueMatch(final Class<T> ofType, final Filter<T> filter) {
        return getContainer().uniqueMatch(ofType, filter);
    }
    
    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#uniqueMatch(Class, String)
     */
    protected <T> T uniqueMatch(final Class<T> ofType, final String title) {
        return getContainer().uniqueMatch(ofType, title);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#uniqueMatch(Class, Object)
     */
    protected <T> T uniqueMatch(final Class<T> ofType, final T pattern) {
        return getContainer().uniqueMatch(ofType, pattern);
    }

    /**
     * Convenience method that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#uniqueMatch(Query)
     */
    protected <T> T uniqueMatch(final Query<T> query) {
        return getContainer().uniqueMatch(query);
    }

    // //////////////////////////////////////

    /**
     * Convenience methods that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#isValid(Object)
     */
    protected boolean isValid(final Object domainObject) {
        return getContainer().isValid(domainObject);
    }

    /**
     * Convenience methods that delegates to {@link DomainObjectContainer}.
     * 
     * @see DomainObjectContainer#validate(Object)
     */
    protected String validate(final Object domainObject) {
        return getContainer().validate(domainObject);
    }

    // //////////////////////////////////////

    /**
     * Determines if the specified object is persistent (that it is stored
     * permanently outside of the virtual machine).
     */
    protected boolean isPersistent(final Object domainObject) {
        return getContainer().isPersistent(domainObject);
    }

    /**
     * Persist the specified transient object.
     * 
     * <p>
     * It is recommended that the object be initially instantiated using
     * {@link #newTransientInstance(Class)}.  However, the framework will also
     * handle the case when the object is simply <i>new()</i>ed up.  See
     * {@link #newTransientInstance(Class)} for more information.
     * 
     * <p>
     * Throws an exception if object is already persistent, or if the object
     * is not yet known to the framework.
     * 
     * @see #newTransientInstance(Class)
     * @see #isPersistent(Object)
     * @see #persistIfNotAlready(Object)
     */
    protected <T> T persist(final T transientDomainObject) {
        getContainer().persist(transientDomainObject);
        return transientDomainObject;
    }

    /**
     * Persist the specified object (or do nothing if already persistent).
     * 
     * <p>
     * It is recommended that the object be initially instantiated using
     * {@link #newTransientInstance(Class)}.  However, the framework will also
     * handle the case when the object is simply <i>new()</i>ed up.  See
     * {@link #newTransientInstance(Class)} for more information.
     *
     * @see #newTransientInstance(Class)
     * @see #isPersistent(Object)
     * @see #persist(Object)
     */
    protected <T> T persistIfNotAlready(final T domainObject) {
        getContainer().persistIfNotAlready(domainObject);
        return domainObject;
    }

    /**
     * Delete the provided object from the persistent object store.
     */
    protected <T> T remove(final T persistentDomainObject) {
        getContainer().remove(persistentDomainObject);
        return persistentDomainObject;
    }

    /**
     * Delete the provided object from the persistent object store.
     */
    protected <T> T removeIfNotAlready(final T persistentDomainObject) {
        getContainer().removeIfNotAlready(persistentDomainObject);
        return persistentDomainObject;
    }
    
    // //////////////////////////////////////

    /**
     * Display the specified message to the user, in a non-intrusive fashion.
     */
    protected void informUser(final String message) {
        getContainer().informUser(message);
    }

    /**
     * Display the specified message to the user, in a non-intrusive fashion.
     */
    protected void informUser(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        getContainer().informUser(message, contextClass, contextMethod);
    }

    /**
     * Display the specified message as a warning to the user, in a more visible
     * fashion, but without requiring explicit acknowledgement.
     */
    protected void warnUser(final String message) {
        getContainer().warnUser(message);
    }


    protected void warnUser(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        getContainer().warnUser(message, contextClass, contextMethod);
    }

    /**
     * Display the specified message as an error to the user, ensuring that it
     * is acknowledged.
     */
    protected void raiseError(final String message) {
        getContainer().raiseError(message);
    }

    protected String raiseError(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        return getContainer().raiseError(message, contextClass, contextMethod);
    }

    // //////////////////////////////////////

    protected UserMemento getUser() {
        return getContainer().getUser();
    }

    // //////////////////////////////////////

    private DomainObjectContainer container;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected DomainObjectContainer getContainer() {
        return this.container;
    }

    /**
     * Injected by the application container itself.
     */
    @Programmatic
    public void setContainer(final DomainObjectContainer container) {
        this.container = container;
    }

}
