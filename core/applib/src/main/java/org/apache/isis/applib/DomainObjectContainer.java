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
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.isis.applib.annotation.Aggregated;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.i18n.TranslatableString;

/**
 * A domain service that acts as a framework's container for managing the 
 * domain objects, and which provides functionality to those domain objects
 * in order that they might interact or have knowledge of with the "outside world".
 */
public interface DomainObjectContainer {

    //region > titleOf

    /**
     * Return the title of the object, as rendered in the UI by the 
     * Isis viewers.
     */
    @Programmatic
    String titleOf(Object domainObject);

    //endregion

    //region > resolve, objectChanged

    /**
     * Ensure that the specified object is completely loaded into memory.
     * 
     * <p>
     * This forces the lazy loading mechanism to load the object if it is not
     * already loaded.
     * 
     * <p>
     * This method has been deprecated because lazy loading is now typically performed
     * by the framework, rather than by application code.
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    void resolve(Object domainObject);

    /**
     * Ensure that the specified object is completely loaded into memory, though
     * only if the supplied field reference is <tt>null</tt>.
     * 
     * <p>
     * This forces the lazy loading mechanism to load the object if it is not
     * already loaded.
     * 
     * <p>
     * This method has been deprecated because lazy loading is now typically performed
     * by the framework, rather than by application code.
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    void resolve(Object domainObject, Object field);

    /**
     * Flags that the specified object's state has changed and its changes need
     * to be saved.
     * 
     * <p>
     * This method has been deprecated because object dirtying is now typically performed
     * by the framework, rather than by application code.
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    void objectChanged(Object domainObject);


    //endregion

    //region > flush, commit

    /**
     * Flush all changes to the object store.
     * 
     * <p>
     * Occasionally useful to ensure that newly persisted domain objects
     * are flushed to the database prior to a subsequent repository query. 
     * 
     * @return  - is never used, always returns <tt>false</tt>. 
     */
    @Programmatic
    boolean flush();

    /**
     * Commit all changes to the object store.
     * 
     * <p>
     * This has been deprecated because the demarcation of transaction
     * boundaries is a framework responsibility rather than being a
     * responsibility of the domain object model.
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    void commit();

    //endregion

    //region > new{Transient/Persistent}Instance

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
     * @see #newPersistentInstance(Class)
     * @see #newAggregatedInstance(Object, Class)
     */
    @Programmatic
    <T> T newTransientInstance(final Class<T> ofType);

    
    /**
     * Create a new instance of the specified view model class, initializing with the
     * specified memento.
     *
     * <p>
     *     Rather than use this constructor it is generally preferable to simply instantiate a
     *     class annotated with {@link org.apache.isis.applib.annotation.ViewModel annotation}.
     *     If services need injecting into it, use {@link #injectServicesInto(Object)}.
     * </p>
     */
    @Programmatic
    <T> T newViewModelInstance(final Class<T> ofType, final String memento);

    /**
     * Create a new instance that will be persisted as part of the specified
     * parent (ie will be a part of a larger aggregate).
     * 
     * <p>
     * <b>Note:</b> not every objectstore implementation supports the concept
     * of aggregated instances.
     */
    @Programmatic
    <T> T newAggregatedInstance(Object parent, Class<T> ofType);

    /**
     * Returns a new instance of the specified class that will have been
     * persisted.
     * 
     * <p>
     * This method has been deprecated because in almost all cases the
     * workflow is to {@link #newTransientInstance(Class)}, populate the object
     * (eg with the arguments to an action) and then to 
     * {@link #persist(Object) persist) the object.  It is exceptionally rare for
     * an object to be created, and with no further data required - be in a state
     * to be persisted immediately.
     *  
     * @deprecated
     */
    @Programmatic
    @Deprecated
    <T> T newPersistentInstance(final Class<T> ofType);

    /**
     * Returns a new instance of the specified class that has the same persisted
     * state (either transient or persisted) as the provided object.
     * 
     * <p>
     * This method has been deprecated because it is a rare use case, causing
     * unnecessary interface bloat for very little gain.
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    <T> T newInstance(final Class<T> ofType, final Object object);

    //endregion

    //region > injectServicesInto

    @Programmatic
    <T> T injectServicesInto(final T domainObject);

    //endregion

    //region > isValid, validate

    /**
     * Whether the object is in a valid state, that is that none of the
     * validation of properties, collections and object-level is vetoing.
     * 
     * @see #validate(Object)
     */
    @Programmatic
    boolean isValid(Object domainObject);

    /**
     * The reason, if any why the object is in a invalid state
     * 
     * <p>
     * Checks the validation of all of the properties, collections and
     * object-level.
     * 
     * @see #isValid(Object)
     */
    @Programmatic
    String validate(Object domainObject);

    //endregion

    //region > isViewModel

    @Programmatic
    boolean isViewModel(Object domainObject);

    //endregion

    //region > isPersistent, persist, remove

    /**
     * Determines if the specified object is persistent (that it is stored
     * permanently outside of the virtual machine).
     */
    @Programmatic
    boolean isPersistent(Object domainObject);

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
    @Programmatic
    void persist(Object domainObject);

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
    @Programmatic
    void persistIfNotAlready(Object domainObject);

    /**
     * Removes (deletes) the persisted object.
     * 
     * @param persistentDomainObject
     */
    @Programmatic
    void remove(Object persistentDomainObject);

    /**
     * Removes (deletes) the domain object but only if is persistent.
     * 
     * @param domainObject
     */
    @Programmatic
    void removeIfNotAlready(Object domainObject);

    //endregion

    //region > info, warn, error

    /**
     * Make the specified message available to the user. Note this will probably
     * be displayed in transitory fashion, so is only suitable for useful but
     * optional information.
     * 
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #warnUser(String)
     * @see #raiseError(String)
     */
    @Programmatic
    void informUser(String message);

    /**
     * Make the specified message available to the user, translated (if possible) to user's locale.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LocaleProvider} service.  This will most commonly be the
     *     locale of the current request (ie the current user's locale).
     * </p>
     *
     * @see #informUser(java.lang.String)
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    @Programmatic
    String informUser(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    /**
     * Warn the user about a situation with the specified message. The container
     * should guarantee to display this warning to the user, and will typically
     * require acknowledgement.
     * 
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(String)
     * @see #informUser(String)
     */
    @Programmatic
    void warnUser(String message);

    /**
     * Warn the user about a situation with the specified message, translated (if possible) to user's locale.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LocaleProvider} service.  This will most commonly be the
     *     locale of the current request (ie the current user's locale).
     * </p>
     *
     * @see #warnUser(String)
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #raiseError(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    @Programmatic
    String warnUser(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    /**
     * Notify the user of an application error with the specified message. Note
     * this will probably be displayed in an alarming fashion, so is only
     * suitable for errors. The user will typically be required to perform
     * additional steps after the error (eg to inform the helpdesk).
     *
     * @see #warnUser(String)
     * @see #informUser(String)
     */
    @Programmatic
    void raiseError(String message);

    /**
     * Notify the user of an application error with the specified message, , translated (if possible) to user's locale.
     *
     * <p>
     *     More precisely, the locale is as provided by the configured
     *     {@link org.apache.isis.applib.services.i18n.LocaleProvider} service.  This will most commonly be the
     *     locale of the current request (ie the current user's locale).
     * </p>
     *
     * @see #raiseError(String)
     * @see #informUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     * @see #warnUser(org.apache.isis.applib.services.i18n.TranslatableString, Class, String)
     */
    @Programmatic
    String raiseError(TranslatableString message, final Class<?> contextClass, final String contextMethod);

    //endregion

    //region > properties

    /**
     * Get the configuration property with the specified name.
     */
    @Programmatic
    String getProperty(String name);

    /**
     * Get the configuration property with the specified name and if it doesn't
     * exist then return the specified default value.
     */
    @Programmatic
    String getProperty(String name, String defaultValue);

    /**
     * Get the names of all the available properties.
     */
    @Programmatic
    List<String> getPropertyNames();

    //endregion

    //region > security

    /**
     * Get the details about the current user.
     */
    @Programmatic
    UserMemento getUser();

    //endregion

    //region > allInstances, allMatches, firstMatch, uniqueMatch

    /**
     * Returns all the instances of the specified type (including subtypes).
     * If the optional range parameters are used, the dataset returned starts 
     * from (0 based) index, and consists of only up to count items.  
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * 
     * <p>
     * This method should only be called where the number of instances is known
     * to be relatively low, unless the optional range parameters (2 longs) are
     * specified. The range parameters are "start" and "count".
     * 
     * @param range 2 longs, specifying 0-based start and count.
     */
    @Programmatic
    public <T> List<T> allInstances(Class<T> ofType, long... range);

    /**
     * Returns all the instances of the specified type (including subtypes) that
     * the predicate object accepts. If the optional range parameters are used, the 
     * dataset returned starts from (0 based) index, and consists of only up to 
     * count items. 
     * 
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #allMatches(Query)} for production code.
     * 
     * @param range 2 longs, specifying 0-based start and count.
     */
    @Programmatic
    <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate, long... range);
    
    /**
     * Returns all the instances of the specified type (including subtypes) that
     * the filter object accepts. If the optional range parameters are used, the 
     * dataset returned starts from (0 based) index, and consists of only up to 
     * count items. 
     * 
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #allMatches(Query)} for production code.
     * 
     * @param range 2 longs, specifying 0-based start and count.
     * 
     * @deprecated - use {@link #allMatches(Class, Predicate, long...)} instead.
     */
    @Programmatic
    @Deprecated
    <T> List<T> allMatches(final Class<T> ofType, final Filter<? super T> filter, long... range);

    /**
     * Returns all the instances of the specified type (including subtypes) that
     * have the given title.
     * If the optional range parameters are used, the dataset returned starts 
     * from (0 based) index, and consists of only up to count items.  
     * 
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #allMatches(Query)} for production code.
     * 
     * @param range 2 longs, specifying 0-based start and count.
     */
    @Programmatic
    public <T> List<T> allMatches(Class<T> ofType, String title, long... range);

    /**
     * Returns all the instances of the specified type (including subtypes) that
     * match the given object: where any property that is set will be tested and
     * properties that are not set will be ignored.
     * If the optional range parameters are used, the dataset returned starts 
     * from (0 based) index, and consists of only up to count items.  
     * 
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #allMatches(Query, long...)} for production code.
     * 
     * @param range 2 longs, specifying 0-based start and count.
     */
    @Programmatic
    <T> List<T> allMatches(Class<T> ofType, T pattern, long... range);

    /**
     * Returns all the instances that match the given {@link Query}.
     * 
     * <p>
     * If there are no instances the list will be empty. This method creates a
     * new {@link List} object each time it is called so the caller is free to
     * use or modify the returned {@link List}, but the changes will not be
     * reflected back to the repository.
     */
    @Programmatic
    <T> List<T> allMatches(Query<T> query);

    // //////////////////////////////////////

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied {@link Predicate}, or <tt>null</tt> if none.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #firstMatch(Query)} for production code.
     */
    @Programmatic
    <T> T firstMatch(final Class<T> ofType, final Predicate<T> predicate);
    
    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied {@link Filter}, or <tt>null</tt> if none.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #firstMatch(Query)} for production code.
     * 
     * @deprecated - use {@link #firstMatch(Class, Predicate)}
     */
    @Programmatic
    @Deprecated
    <T> T firstMatch(final Class<T> ofType, final Filter<T> filter);

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied title, or <tt>null</tt> if none.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #firstMatch(Query)} for production code.
     */
    @Programmatic
    <T> T firstMatch(Class<T> ofType, String title);

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied object as a pattern, or <tt>null</tt> if none.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #firstMatch(Query)} for production code.
     */
    @Programmatic
    <T> T firstMatch(Class<T> ofType, T pattern);

    /**
     * Returns the first instance that matches the supplied query, or
     * <tt>null</tt> if none.
     */
    @Programmatic
    <T> T firstMatch(Query<T> query);

    // //////////////////////////////////////

    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     * 
     * <p>
     * If no instance is found then <tt>null</tt> will be return, while if there
     * is more that one instances a run-time exception will be thrown.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #uniqueMatch(Query)} for production code.
     */
    @Programmatic
    <T> T uniqueMatch(final Class<T> ofType, final Predicate<T> predicate);

    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     * 
     * <p>
     * If no instance is found then <tt>null</tt> will be return, while if there
     * is more that one instances a run-time exception will be thrown.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #uniqueMatch(Query)} for production code.
     * 
     * @deprecated - use {@link #uniqueMatch(Class, Predicate)}
     */
    @Programmatic
    @Deprecated
    public <T> T uniqueMatch(final Class<T> ofType, final Filter<T> filter);
    
    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     * 
     * <p>
     * If no instance is found then <tt>null</tt> will be returned, while if
     * there is more that one instances a run-time exception will be thrown.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #uniqueMatch(Query)} for production code.
     */
    @Programmatic
    <T> T uniqueMatch(Class<T> ofType, String title);

    /**
     * Find the only instance of the patterned object type (including subtypes)
     * that matches the set fields in the pattern object: where any property
     * that is set will be tested and properties that are not set will be
     * ignored.
     * 
     * <p>
     * If no instance is found then null will be return, while if there is more
     * that one instances a run-time exception will be thrown.
     * 
     * <p>
     * This method is useful during exploration/prototyping, but you may want to
     * use {@link #uniqueMatch(Query)} for production code.
     */
    @Programmatic
    <T> T uniqueMatch(Class<T> ofType, T pattern);

    /**
     * Find the only instance that matches the provided query.
     *
     * <p>
     * If no instance is found then null will be return, while if there is more
     * that one instances a run-time exception will be thrown.
     */
    @Programmatic
    <T> T uniqueMatch(Query<T> query);


    //endregion


    class Functions {
        public static <T> Function<T, String> titleOfUsing(final DomainObjectContainer container) {
            return new Function<T, String>() {
                @Override
                public String apply(final T input) {
                    return container.titleOf(input);
                }
            };
        }
    }
}
