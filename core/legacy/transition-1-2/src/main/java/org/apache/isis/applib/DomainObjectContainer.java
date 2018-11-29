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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.common.base.Predicate;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.jdosupport.IsisJdoSupport;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;

import static org.apache.isis.commons.internal.collections._Lists.lastElementIfAny;

@DomainService(nature=NatureOfService.DOMAIN)
@Deprecated
public class DomainObjectContainer {
	
	@Inject private RepositoryService repositoryService;
	@Inject private MetaModelService metaModelService;
	@Inject private IsisJdoSupport isisJdoSupport;
	@Inject private FactoryService factoryService;
	@Inject private TitleService titleService;
	@Inject private TransactionService transactionService;
	@Inject private ServiceRegistry serviceRegistry;
	@Inject private MessageService messageService;
	@Inject private UserService userService;
	
    /**
     * Return the title of the object, as rendered in the UI by the 
     * Isis viewers.
     *
     * @deprecated - use {@link TitleService#titleOf(Object)} instead.
     */
    @Deprecated
    @Programmatic
    public String titleOf(Object domainObject) {
    	return titleService.titleOf(domainObject);
    }

    /**
     * Return the icon name of the object, as rendered in the UI by the
     * Isis viewers.
     *
     * @deprecated - use {@link TitleService#iconNameOf(Object)} instead.
     */
    @Deprecated
    @Programmatic
    public String iconNameOf(Object domainObject) {
    	return titleService.iconNameOf(domainObject);
    }
    
    /**
     * Re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    public void resolve(Object domainObject) {
    	isisJdoSupport.refresh(domainObject);
    }

    /**
     * Provided that the <tt>field</tt> parameter is <tt>null</tt>, re-initialises the fields of an object, using the
     * JDO {@link javax.jdo.PersistenceManager#refresh(Object) refresh} API.
     *
     * <p>
     *     Previously this method was provided for manual control of lazy loading; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - equivalent to {@link org.apache.isis.applib.services.jdosupport.IsisJdoSupport#refresh(Object)}.
     */
    @Programmatic
    @Deprecated
    public void resolve(Object domainObject, Object field) {
    	resolve(domainObject);
    }

    /**
     * This method does nothing (is a no-op).
     *
     * <p>
     *     Previous this method was provided for manual control of object dirtyng; with the JDO/DataNucleus objectstore
     *     that original functionality is performed automatically by the framework.
     * </p>
     *
     * @deprecated - is a no-op
     */
    @Programmatic
    @Deprecated
    public void objectChanged(Object domainObject) {
    	// do nothing
    }
    

    /**
     * Flush all changes to the object store.
     * 
     * <p>
     * Occasionally useful to ensure that newly persisted domain objects
     * are flushed to the database prior to a subsequent repository query. 
     * 
     * @return  - is never used, always returns <tt>false</tt>.
     *
     * @deprecated - use {@link TransactionService#flushTransaction()}.
     */
    @Programmatic
    @Deprecated
    public boolean flush() {
    	transactionService.flushTransaction();
    	return false;
    }

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
    public void commit() {
    	// do nothing
    }

    /**
    * @deprecated - use {@link org.apache.isis.applib.services.factory.FactoryService#instantiate(Class)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T newTransientInstance(final Class<T> ofType) {
    	return factoryService.instantiate(ofType);
    }

    /**
     * Create a new {@link ViewModel} instance of the specified type, initializing with the specified memento.
     *
     * <p>
     *     Rather than use this constructor it is generally preferable to simply instantiate a
     *     class annotated with {@link org.apache.isis.applib.annotation.ViewModel annotation}.
     *     If services need injecting into it, use {@link #injectServicesInto(Object)}.
     * </p>
     */
    @Programmatic
    public <T> T newViewModelInstance(final Class<T> ofType, final String memento) {
    	if(memento!=null) {
    		throw new IllegalArgumentException("parameter 'memento' is no longer supported");
    	}
    	T obj = factoryService.instantiate(ofType);
    	serviceRegistry.injectServicesInto(obj);
    	return obj;
    }

    /**
     * @deprecated - not supported, will throw a RuntimeException
     */
    @Deprecated
    @Programmatic
    public <T> T newAggregatedInstance(Object parent, Class<T> ofType) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * (Deprecated) returns a new instance of the specified class that will have been
     * persisted.
     *
     * @deprecated - in almost all cases the workflow is to {@link #newTransientInstance(Class)}, populate the object
     * (eg with the arguments to an action) and then to
     * {@link #persist(Object) persist) the object.  It is exceptionally rare for
     * an object to be created, and with no further data required - be in a state
     * to be persisted immediately.
     */
    @Programmatic
    @Deprecated
    public <T> T newPersistentInstance(final Class<T> ofType) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * Returns a new instance of the specified class that has the same persisted
     * state (either transient or persisted) as the provided object.
     * 
     * <p>
     * This method has been deprecated because it is a rare use case, causing
     * unnecessary interface bloat for very little gain.
     * <p></p>
     * 
     * @deprecated
     */
    @Programmatic
    @Deprecated
    public <T> T newInstance(final Class<T> ofType, final Object object) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.factory.FactoryService#mixin(Class, Object)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T mixin( Class<T> mixinClass, Object mixedIn) {
    	return factoryService.mixin(mixinClass, mixedIn);
    }


    /**
     * @deprecated - use {@link org.apache.isis.applib.services.registry.ServiceRegistry#injectServicesInto(Object)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T injectServicesInto(final T domainObject) {
    	return serviceRegistry.injectServicesInto(domainObject);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.registry.ServiceRegistry#lookupService(Class)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T lookupService(Class<T> service) {
    	return serviceRegistry.lookupService(service).orElse(null);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.registry.ServiceRegistry#streamServices(Class)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> Iterable<T> lookupServices(Class<T> service){
    	return serviceRegistry.streamServices(service)
    	        .collect(Collectors.toList());	
    }

    /**
     * Whether the object is in a valid state, that is that none of the
     * validation of properties, collections and object-level is vetoing.
     * 
     * @see #validate(Object)
     */
    @Programmatic
    public boolean isValid(Object domainObject) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * The reason, if any why the object is in a invalid state
     * 
     * <p>
     * Checks the validation of all of the properties, collections and
     * object-level.
     * </p>
     *
     * @see #isValid(Object)
     */
    public @Programmatic
    String validate(Object domainObject) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.metamodel.MetaModelService#sortOf(Class, MetaModelService.Mode)} instead.
     */
    @Deprecated
    @Programmatic
    public boolean isViewModel(Object domainObject) {
    	return metaModelService.sortOf(domainObject.getClass(), MetaModelService.Mode.RELAXED).isViewModel();
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#isPersistent(Object)} instead.
     */
    @Deprecated
    @Programmatic
    public boolean isPersistent(Object domainObject) {
    	return repositoryService.isPersistent(domainObject);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} instead. Please note that {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} will not throw an exception if the Domain Object is already persistent, so the implementation will be the same as that of {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)}instead.
     */
    @Deprecated
    @Programmatic
    public void persist(Object domainObject) {
    	repositoryService.persist(domainObject);
    }
    
    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#persist(Object)} instead.
     */
    @Deprecated
    @Programmatic
    public void persistIfNotAlready(Object domainObject) {
    	repositoryService.persist(domainObject);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#remove(Object)} instead.
     */
    @Deprecated
    @Programmatic
    public void remove(Object persistentDomainObject) {
    	repositoryService.remove(persistentDomainObject);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#remove(Object)} instead.
     */
    @Deprecated
    @Programmatic
    public void removeIfNotAlready(Object domainObject) {
    	repositoryService.remove(domainObject);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#informUser(String)} instead.
     */
    @Deprecated
    @Programmatic
    public void informUser(String message) {
    	messageService.informUser(message);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#informUser(TranslatableString, Class, String)} instead.
     */
    @Deprecated
    @Programmatic
    public String informUser(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
    	return messageService.informUser(message, contextClass, contextMethod);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#warnUser(String)} instead.
     */
    @Deprecated
    @Programmatic
    public void warnUser(String message) {
    	messageService.warnUser(message);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#warnUser(TranslatableString, Class, String)} instead.
     */
    @Deprecated
    @Programmatic
    public String warnUser(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
    	return messageService.warnUser(message, contextClass, contextMethod);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#raiseError(String)} instead.
     */
    @Deprecated
    @Programmatic
    public void raiseError(String message) {
    	messageService.raiseError(message);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.message.MessageService#raiseError(TranslatableString, Class, String)} instead.
     */
    @Deprecated
    @Programmatic
    public String raiseError(TranslatableString message, final Class<?> contextClass, final String contextMethod) {
    	return messageService.raiseError(message, contextClass, contextMethod);
    }


    /**
     * @deprecated - use {@link IsisConfiguration#getProperty(String)} instead.
     */
    @Deprecated
    @Programmatic
    public String getProperty(String name) {
    	return _Config.getConfiguration().getString(name);
    }

    /**
     * @deprecated - use {@link IsisConfiguration#getProperty(String, String)} instead.
     */
    @Deprecated
    @Programmatic
    public String getProperty(String name, String defaultValue) {
    	return _Config.getConfiguration().getString(name, defaultValue);
    }

    /**
     * @deprecated - use {@link IsisConfiguration#asMap()} instead.
     */
    @Deprecated
    @Programmatic
    public List<String> getPropertyNames() {
        return new ArrayList<>(_Config.getConfiguration().asMap().keySet());
    }

    /**
     * @deprecated - use {@link UserService#getUser()} instead
     */
    @Deprecated
    @Programmatic
    public UserMemento getUser() {
    	return userService.getUser();
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allInstances(Class, long...)} instead
     */
    @Deprecated
    @Programmatic
    public <T> List<T> allInstances(Class<T> ofType, long... range){
    	return repositoryService.allInstances(ofType, range);
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Class, Predicate, long...)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> List<T> allMatches(final Class<T> ofType, final Predicate<? super T> predicate, long... range){
    	return repositoryService.allMatches(ofType, predicate::apply, range);
    }
    
    /**
     * @deprecated - use {@link #allMatches(Class, Predicate, long...)} or (better) {@link #allMatches(Query)} instead
     */
    @Deprecated
    @Programmatic
    public <T> List<T> allMatches(final Class<T> ofType, final Filter<? super T> filter, long... range) {
    	return repositoryService.allMatches(ofType, filter::accept, range);
    }

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
     * </p>
     *
     * <p>
     * This method is useful during prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #allMatches(Query)} for production code.
     * </p>
     *
     * @param range 2 longs, specifying 0-based start and count.
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Class, Predicate, long...)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> List<T> allMatches(Class<T> ofType, String title, long... range) {
    	return repositoryService.allMatches(ofType, obj->title.equals(titleService.titleOf(obj)), range);
    }

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
     * </p>
     *
     * <p>
     * This method is useful during prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #allMatches(Query)} for production code.
     * </p>
     *
     * @param range 2 longs, specifying 0-based start and count.
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Class, Predicate, long...)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> List<T> allMatches(Class<T> ofType, T pattern, long... range) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#allMatches(Query)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> List<T> allMatches(Query<T> query) {
    	return repositoryService.allMatches(query);
    }

    // //////////////////////////////////////

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T firstMatch(final Class<T> ofType, final Predicate<T> predicate) {
    	return lastElementIfAny(repositoryService.allMatches(ofType, predicate::apply, 0L, 1L));
    }
    
    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T firstMatch(final Class<T> ofType, final Filter<T> filter) {
    	return lastElementIfAny(repositoryService.allMatches(ofType, filter::accept, 0L, 1L));
    }

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied title, or <tt>null</tt> if none.
     *
     * <p>
     * This method is useful during prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #firstMatch(Query)} for production code.
     * </p>
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T firstMatch(Class<T> ofType, String title) {
    	return lastElementIfAny(repositoryService.allMatches(ofType, obj->title.equals(titleService.titleOf(obj)), 0L, 1L));
    }

    /**
     * Returns the first instance of the specified type (including subtypes)
     * that matches the supplied object as a pattern, or <tt>null</tt> if none.
     *
     * <p>
     * This method is useful during prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #firstMatch(Query)} for production code.
     * </p>
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Class, Predicate)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T firstMatch(Class<T> ofType, T pattern) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#firstMatch(Query)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T firstMatch(Query<T> query) {
    	Iterator<T> it = repositoryService.allMatches(query).iterator();
    	return it.hasNext() ? it.next() : null;
    }

    // //////////////////////////////////////

    /**
     * @deprecated  - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T uniqueMatch(final Class<T> ofType, final Predicate<T> predicate) {
    	return repositoryService.uniqueMatch(ofType, predicate::apply);
    }

    /**
     * @deprecated  - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)} instead.
     */
    @Programmatic
    @Deprecated
    public <T> T uniqueMatch(final Class<T> ofType, final Filter<T> filter) {
    	return repositoryService.uniqueMatch(ofType, filter::accept);
    }
    
    /**
     * Find the only instance of the specified type (including subtypes) that
     * has the specified title.
     * 
     * <p>
     * If no instance is found then <tt>null</tt> will be returned, while if
     * there is more that one instances a run-time exception will be thrown.
     * 
     * <p>
     * This method is useful during prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #uniqueMatch(Query)} for production code.
     * </p>
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T uniqueMatch(Class<T> ofType, String title) {
    	return repositoryService.uniqueMatch(ofType, obj->title.equals(titleService.titleOf(obj)));
    }

    /**
     * Find the only instance of the patterned object type (including subtypes)
     * that matches the set fields in the pattern object: where any property
     * that is set will be tested and properties that are not set will be
     * ignored.
     * 
     * <p>
     * If no instance is found then null will be return, while if there is more
     * that one instances a run-time exception will be thrown.
     * </p>
     *
     * <p>
     * This method is useful during prototyping, but - because the filtering is performed client-side -
     * this method is only really suitable for initial development/prototyping, or for classes with very few
     * instances.  Use {@link #uniqueMatch(Query)} for production code.
     * </p>
     *
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Class, Predicate)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T uniqueMatch(Class<T> ofType, T pattern) {
    	throw new RuntimeException("method no longer supported");
    }

    /**
     * @deprecated - use {@link org.apache.isis.applib.services.repository.RepositoryService#uniqueMatch(Query)} instead.
     */
    @Deprecated
    @Programmatic
    public <T> T uniqueMatch(Query<T> query) {
    	return repositoryService.uniqueMatch(query);
    }

	
}
