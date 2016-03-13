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

package org.apache.isis.core.metamodel.services.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.google.common.base.Predicate;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.NonRecoverableException;
import org.apache.isis.applib.PersistFailedException;
import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.RepositoryException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.query.QueryFindAllInstances;
import org.apache.isis.applib.security.RoleMemento;
import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.sudo.SudoService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProviderAware;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationService;
import org.apache.isis.core.metamodel.runtimecontext.ConfigurationServiceAware;
import org.apache.isis.core.metamodel.runtimecontext.LocalizationProvider;
import org.apache.isis.core.metamodel.runtimecontext.LocalizationProviderAware;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerService;
import org.apache.isis.core.metamodel.runtimecontext.MessageBrokerServiceAware;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionService;
import org.apache.isis.core.metamodel.runtimecontext.PersistenceSessionServiceAware;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByPattern;
import org.apache.isis.core.metamodel.services.container.query.QueryFindByTitle;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

@DomainService(nature = NatureOfService.DOMAIN)
public class DomainObjectContainerDefaultORIG
        implements DomainObjectContainer, ConfigurationServiceAware,
        PersistenceSessionServiceAware, SpecificationLoaderAware, AuthenticationSessionProviderAware, AdapterManagerAware,
        ServicesInjectorAware, MessageBrokerServiceAware,
        LocalizationProviderAware, ExceptionRecognizer {
    private MessageBrokerService messageBrokerService;

    //region > titleOf

    @Programmatic
    @Override
    public String titleOf(final Object domainObject) {
        final ObjectAdapter objectAdapter = adapterManager.adapterFor(unwrapped(domainObject));
        final boolean destroyed = objectAdapter.isDestroyed();
        if(!destroyed) {
            return objectAdapter.getSpecification().getTitle(objectAdapter, localizationProvider.getLocalization());
        } else {
            return "[DELETED]";
        }
    }

    //endregion

    //region > iconNameOf

    @Programmatic
    @Override
    public String iconNameOf(final Object domainObject) {
        final ObjectAdapter objectAdapter = adapterManager.adapterFor(unwrapped(domainObject));
        return objectAdapter.getSpecification().getIconName(objectAdapter);
    }

    //endregion

    //region > newXxxInstance, remove

    @Programmatic
    @Override
    @SuppressWarnings("unchecked")
    public <T> T newTransientInstance(final Class<T> ofClass) {
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(ofClass);
        final ObjectAdapter adapter = doCreateTransientInstance(spec);
        return (T) adapter.getObject();
    }

    @Programmatic
    @SuppressWarnings("unchecked")
    @Override
    public <T> T newViewModelInstance(Class<T> ofClass, String memento) {
        final ObjectSpecification spec = getSpecificationLoader().loadSpecification(ofClass);
        if (!spec.containsFacet(ViewModelFacet.class)) {
            throw new IsisException("Type must be a ViewModel: " + ofClass);
        }
        final ObjectAdapter adapter = doCreateViewModelInstance(spec, memento);
        if(adapter.getOid().isViewModel()) {
            return (T)adapter.getObject();
        } else {
            throw new IsisException("Object instantiated but was not given a ViewModel Oid; please report as a possible defect in Isis: " + ofClass);
        }
    }

    /**
     * @deprecated - Aggregated objects are no longer supported
     */
    @Deprecated
    @Programmatic
    @Override
    @SuppressWarnings("unchecked")
    public <T> T newAggregatedInstance(final Object parent, final Class<T> ofClass) {
        throw new RuntimeException("Aggregated objects are no longer supported");
    }

    /**
     * Returns a new instance of the specified class that will have been
     * persisted.
     */
    @Deprecated
    @Programmatic
    @Override
    public <T> T newPersistentInstance(final Class<T> ofClass) {
        final T newInstance = newTransientInstance(ofClass);
        persist(newInstance);
        return newInstance;
    }

    /**
     * Returns a new instance of the specified class that has the same persisted
     * state as the specified object.
     */
    @Programmatic
    @Override
    @Deprecated
    public <T> T newInstance(final Class<T> ofClass, final Object object) {
        if (isPersistent(object)) {
            return newPersistentInstance(ofClass);
        } else {
            return newTransientInstance(ofClass);
        }
    }

    @Programmatic
    @Override
    public <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        final ObjectSpecification objectSpec = getSpecificationLoader().loadSpecification(mixinClass);
        final MixinFacet mixinFacet = objectSpec.getFacet(MixinFacet.class);
        if(mixinFacet == null) {
            throw new NonRecoverableException("Class '" + mixinClass.getName() + " is not a mixin");
        }
        if(!mixinFacet.isMixinFor(mixedIn.getClass())) {
            throw new NonRecoverableException("Mixin class '" + mixinClass.getName() + " is not a mixin for supplied object '" + mixedIn + "'");
        }
        final Constructor<?>[] constructors = mixinClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            if(constructor.getParameterTypes().length == 1 &&
               constructor.getParameterTypes()[0].isAssignableFrom(mixedIn.getClass())) {
                final Object mixin;
                try {
                    mixin = constructor.newInstance(mixedIn);
                    return (T)injectServicesInto(mixin);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new NonRecoverableException(e);
                }
            }
        }
        // should never get here because of previous guards
        throw new NonRecoverableException( String.format(
                "Failed to locate constructor in %s to instantiate using %s", mixinClass.getName(), mixedIn));
    }

    /**
     * Factored out as a potential hook method for subclasses.
     */
    protected ObjectAdapter doCreateTransientInstance(final ObjectSpecification spec) {
        return getPersistenceSessionService().createTransientInstance(spec);
    }

    protected ObjectAdapter doCreateViewModelInstance(final ObjectSpecification spec, final String memento) {
        return getPersistenceSessionService().createViewModelInstance(spec, memento);
    }

    private ObjectAdapter doCreateAggregatedInstance(final ObjectSpecification spec, final Object parent) {
        throw new RuntimeException("Aggregated instances are no longer supported");
    }

    @Programmatic
    @Override
    public void remove(final Object persistentObject) {
        if (persistentObject == null) {
            throw new IllegalArgumentException("Must specify a reference for disposing an object");
        }
        final ObjectAdapter adapter = getAdapterManager().adapterFor(unwrapped(persistentObject));
        if (!isPersistent(persistentObject)) {
            throw new RepositoryException("Object not persistent: " + adapter);
        }

        getPersistenceSessionService().remove(adapter);
    }

    @Programmatic
    @Override
    public void removeIfNotAlready(final Object object) {
        if (!isPersistent(object)) {
            return;
        }
        remove(object);
    }

    //endregion

    //region > injectServicesInto

    @Programmatic
    @Override
    public <T> T injectServicesInto(T domainObject) {
        servicesInjector.injectServicesInto(unwrapped(domainObject));
        return domainObject;
    }

    @Programmatic
    @Override
    public <T> T lookupService(final Class<T> service) {
        return servicesInjector.lookupService(service);
    }

    @Programmatic
    @Override
    public <T> Iterable<T> lookupServices(final Class<T> service) {
        return servicesInjector.lookupServices(service);
    }

    //endregion

    //region > resolve, objectChanged (deprecated)

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
    @Override
    public void resolve(final Object parent) {
        getPersistenceSessionService().resolve(unwrapped(parent));
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
    @Override
    public void resolve(final Object parent, final Object field) {
        getPersistenceSessionService().resolve(unwrapped(parent), field);
    }

    /**
     * @deprecated - no-op.
     */
    @Deprecated
    @Programmatic
    @Override
    public void objectChanged(final Object object) {
    }

    //endregion

    //region > flush, commit

    @Programmatic
    @Override
    public boolean flush() {
        return getPersistenceSessionService().flush();
    }

    @Programmatic
    @Override
    public void commit() {
        getPersistenceSessionService().commit();
    }

    //endregion

    //region > isValid, validate

    @Programmatic
    @Override
    public boolean isValid(final Object domainObject) {
        return validate(domainObject) == null;
    }

    @Programmatic
    @Override
    public String validate(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(unwrapped(domainObject));
        final InteractionResult validityResult =
                adapter.getSpecification().isValidResult(adapter, InteractionInitiatedBy.FRAMEWORK);
        return validityResult.getReason();
    }

    //endregion


    //region > isViewModel

    @Programmatic
    @Override
    public boolean isViewModel(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(unwrapped(domainObject));
        return adapter.getSpecification().isViewModel();
    }
    //endregion

    //region > persistence


    @Programmatic
    @Override
    public boolean isPersistent(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(unwrapped(domainObject));
        return adapter.representsPersistent();
    }

    /**
     * {@inheritDoc}
     */
    @Programmatic
    @Override
    public void persist(final Object domainObject) {
        final ObjectAdapter adapter = getAdapterManager().adapterFor(unwrapped(domainObject));

        if(adapter == null) {
            throw new PersistFailedException("Object not known to framework; instantiate using newTransientInstance(...) rather than simply new'ing up.");
        }
        if (adapter.isParentedCollection()) {
            // TODO check aggregation is supported
            return;
        }
        if (isPersistent(domainObject)) {
            throw new PersistFailedException("Object already persistent; OID=" + adapter.getOid());
        }
        getPersistenceSessionService().makePersistent(adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Programmatic
    @Override
    public void persistIfNotAlready(final Object object) {
        if (isPersistent(object)) {
            return;
        }
        persist(object);
    }

    @Override
    public void setMessageBrokerService(final MessageBrokerService messageBrokerService) {
        this.messageBrokerService = messageBrokerService;
    }

    //endregion

    //region > security

    static class UserAndRoleOverrides {
        final String user;
        final List<String> roles;

        UserAndRoleOverrides(final String user) {
            this(user, null);
        }

        UserAndRoleOverrides(final String user, final List<String> roles) {
            this.user = user;
            this.roles = roles;
        }
    }

    private final ThreadLocal<Stack<UserAndRoleOverrides>> overrides =
            new ThreadLocal<Stack<UserAndRoleOverrides>>() {
        @Override protected Stack<UserAndRoleOverrides> initialValue() {
            return new Stack<>();
        }
    };

    /**
     * Not API; for use by the implementation of {@link SudoService}.
     */
    @Programmatic
    public void overrideUser(final String user) {
        overrideUserAndRoles(user, null);
    }
    /**
     * Not API; for use by the implementation of {@link SudoService}.
     */
    @Programmatic
    public void overrideUserAndRoles(final String user, final List<String> roles) {
        this.overrides.get().push(new UserAndRoleOverrides(user, roles));
    }
    /**
     * Not API; for use by the implementation of {@link SudoService}.
     */
    @Programmatic
    public void resetOverrides() {
        this.overrides.get().pop();
    }

    @Programmatic
    @Override
    public UserMemento getUser() {
        final AuthenticationSession session = getAuthenticationSessionProvider().getAuthenticationSession();

        final UserAndRoleOverrides userAndRoleOverrides = currentOverridesIfAny();

        final String username = userAndRoleOverrides != null
                ? userAndRoleOverrides.user
                : session.getUserName();
        final List<String> roles = userAndRoleOverrides != null
                ? userAndRoleOverrides.roles != null
                    ? userAndRoleOverrides.roles
                    : session.getRoles()
                : session.getRoles();
        final List<RoleMemento> roleMementos = asRoleMementos(roles);

        final UserMemento user = new UserMemento(username, roleMementos);
        return user;
    }

    private UserAndRoleOverrides currentOverridesIfAny() {
        final Stack<UserAndRoleOverrides> userAndRoleOverrides = overrides.get();
        return !userAndRoleOverrides.empty()
                ? userAndRoleOverrides.peek()
                : null;
    }

    private static List<RoleMemento> asRoleMementos(final List<String> roles) {
        final List<RoleMemento> mementos = new ArrayList<RoleMemento>();
        if (roles != null) {
            for (final String role : roles) {
                mementos.add(new RoleMemento(role));
            }
        }
        return mementos;
    }

    //endregion

    //region > properties

    @Programmatic
    @Override
    public String getProperty(final String name) {
        return getConfigurationService().getProperty(name);
    }

    @Programmatic
    @Override
    public String getProperty(final String name, final String defaultValue) {
        final String value = getProperty(name);
        return value == null ? defaultValue : value;
    }

    @Programmatic
    @Override
    public List<String> getPropertyNames() {
        return getConfigurationService().getPropertyNames();
    }

    //endregion

    //region > info, warn, error messages

    @Programmatic
    @Override
    public void informUser(final String message) {
        messageBrokerService.informUser(message);
    }

    @Override
    public String informUser(final TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        return message.translate(translationService, context(contextClass, contextMethod));
    }

    @Programmatic
    @Override
    public void warnUser(final String message) {
        messageBrokerService.warnUser(message);
    }

    @Override
    public String warnUser(final TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        return message.translate(translationService, context(contextClass, contextMethod));
    }

    @Programmatic
    @Override
    public void raiseError(final String message) {
        messageBrokerService.raiseError(message);
    }

    @Override
    public String raiseError(final TranslatableString message, final Class<?> contextClass, final String contextMethod) {
        final String translatedMessage = message.translate(translationService, context(contextClass, contextMethod));
        messageBrokerService.raiseError(translatedMessage);
        return translatedMessage;
    }

    private static String context(final Class<?> contextClass, final String contextMethod) {
        return contextClass.getName()+"#"+contextMethod;
    }

    //endregion

    //region > allInstances, allMatches, uniqueMatch, firstMatch

    @Programmatic
    @Override
    public <T> List<T> allInstances(final Class<T> type, long... range) {
        return allMatches(new QueryFindAllInstances<T>(type, range));
    }

    // //////////////////////////////////////////////////////////////////

    @Programmatic
    @Override
    public <T> List<T> allMatches(final Class<T> cls, final Predicate<? super T> predicate, long... range) {
        final List<T> allInstances = allInstances(cls, range);
        final List<T> filtered = new ArrayList<T>();
        for (final T instance : allInstances) {
            if (predicate.apply(instance)) {
                filtered.add(instance);
            }
        }
        return filtered;
    }

    @Programmatic
    @Deprecated
    @Override
    public <T> List<T> allMatches(final Class<T> cls, final Filter<? super T> filter, long... range) {
        return allMatches(cls, Filters.asPredicate(filter), range);
    }

    @Programmatic
    @Override
    public <T> List<T> allMatches(final Class<T> type, final T pattern, long... range) {
        Assert.assertTrue("pattern not compatible with type", type.isAssignableFrom(pattern.getClass()));
        return allMatches(new QueryFindByPattern<T>(type, pattern, range));
    }

    @Programmatic
    @Override
    public <T> List<T> allMatches(final Class<T> type, final String title, long... range) {
        return allMatches(new QueryFindByTitle<T>(type, title, range));
    }

    @Programmatic
    @Override
    public <T> List<T> allMatches(final Query<T> query) {
        if(autoFlush) {
            flush(); // auto-flush any pending changes
        }
        return submitQuery(query);
    }

    <T> List<T> submitQuery(final Query<T> query) {
        final List<ObjectAdapter> allMatching = getPersistenceSessionService().allMatchingQuery(query);
        return ObjectAdapter.Util.unwrapT(allMatching);
    }

    // //////////////////////////////////////////////////////////////////

    @Programmatic
    @Override
    public <T> T firstMatch(final Class<T> cls, final Predicate<T> predicate) {
        final List<T> allInstances = allInstances(cls); // Have to fetch all, as matching is done in next loop
        for (final T instance : allInstances) {
            if (predicate.apply(instance)) {
                return instance;
            }
        }
        return null;
    }

    @Programmatic
    @Deprecated
    @Override
    public <T> T firstMatch(final Class<T> cls, final Filter<T> filter) {
        return firstMatch(cls, Filters.asPredicate(filter));
    }

    @Programmatic
    @Override
    public <T> T firstMatch(final Class<T> type, final T pattern) {
        final List<T> instances = allMatches(type, pattern, 0, 1); // No need to fetch more than 1
        return firstInstanceElseNull(instances);
    }

    @Programmatic
    @Override
    public <T> T firstMatch(final Class<T> type, final String title) {
        final List<T> instances = allMatches(type, title, 0, 1); // No need to fetch more than 1
        return firstInstanceElseNull(instances);
    }

    @Programmatic
    @Override
    @SuppressWarnings("unchecked")
    public <T> T firstMatch(final Query<T> query) {
        flush(); // auto-flush any pending changes
        final ObjectAdapter firstMatching = getPersistenceSessionService().firstMatchingQuery(query);
        return (T) ObjectAdapter.Util.unwrap(firstMatching);
    }

    // //////////////////////////////////////////////////////////////////

    @Programmatic
    @Override
    public <T> T uniqueMatch(final Class<T> type, final Predicate<T> predicate) {
        final List<T> instances = allMatches(type, predicate, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + predicate);
        }
        return firstInstanceElseNull(instances);
    }

    @Programmatic
    @Deprecated
    @Override
    public <T> T uniqueMatch(final Class<T> type, final Filter<T> filter) {
        final List<T> instances = allMatches(type, filter, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more than one instance of " + type + " matching filter " + filter);
        }
        return firstInstanceElseNull(instances);
    }

    @Programmatic
    @Override
    public <T> T uniqueMatch(final Class<T> type, final T pattern) {
        final List<T> instances = allMatches(type, pattern, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance of " + type + " matching pattern " + pattern);
        }
        return firstInstanceElseNull(instances);
    }

    @Programmatic
    @Override
    public <T> T uniqueMatch(final Class<T> type, final String title) {
        final List<T> instances = allMatches(type, title, 0, 2); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance of " + type + " with title " + title);
        }
        return firstInstanceElseNull(instances);
    }

    @Programmatic
    @Override
    public <T> T uniqueMatch(final Query<T> query) {
        final List<T> instances = allMatches(query); // No need to fetch more than 2.
        if (instances.size() > 1) {
            throw new RepositoryException("Found more that one instance for query:" + query.getDescription());
        }
        return firstInstanceElseNull(instances);
    }

    private static <T> T firstInstanceElseNull(final List<T> instances) {
        return instances.size() == 0 ? null : instances.get(0);
    }

    //endregion

    //region > ExceptionRecognizer

    static class ExceptionRecognizerForConcurrencyException
            extends ExceptionRecognizerForType {
        public ExceptionRecognizerForConcurrencyException() {
            super(Category.CONCURRENCY, ConcurrencyException.class, prefix("Another user has just changed this data"));
        }
    }
    static class ExceptionRecognizerForRecoverableException extends ExceptionRecognizerForType {
        public ExceptionRecognizerForRecoverableException() {
            super(Category.CLIENT_ERROR, RecoverableException.class);
        }
    }


    private final ExceptionRecognizer recognizer =
            new ExceptionRecognizerComposite(
                    new ExceptionRecognizerForConcurrencyException(),
                    new ExceptionRecognizerForRecoverableException()
                );

    /**
     * Framework-provided implementation of {@link ExceptionRecognizer},
     * which will automatically recognize any {@link RecoverableException}s or
     * any {@link ConcurrencyException}s.
     */
    @Programmatic
    @Override
    public String recognize(Throwable ex) {
        return recognizer.recognize(ex);
    }

    //endregion

    //region > init, shutdown

    /**
     * Normally any queries are automatically preceded by flushing pending executions.
     *
     * <p>
     * This key allows this behaviour to be disabled.
     *
     * <p>
     *     Originally introduced as part of ISIS-1134 (fixing memory leaks in the objectstore)
     *     where it was found that the autoflush behaviour was causing a (now unrepeatable)
     *     data integrity error (see <a href="https://issues.apache.org/jira/browse/ISIS-1134?focusedCommentId=14500638&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14500638">ISIS-1134 comment</a>, in the isis-module-security.
     *     However, that this could be circumvented by removing the call to flush().
     *     We don't want to break existing apps that might rely on this behaviour, on the
     *     other hand we want to fix the memory leak.  Adding this configuration property
     *     seems the most prudent way forward.
     * </p>
     */
    private static final String KEY_DISABLE_AUTOFLUSH = "isis.services.container.disableAutoFlush";

    private boolean autoFlush;

    @Programmatic
    @PostConstruct
    @Override
    public void init(Map<String, String> properties) {
        injectServicesInto(recognizer);
        recognizer.init(properties);

        final boolean disableAutoFlush = Boolean.parseBoolean(properties.get(KEY_DISABLE_AUTOFLUSH));
        this.autoFlush = !disableAutoFlush;
    }

    @Programmatic
    @PreDestroy
    @Override
    public void shutdown() {
        recognizer.shutdown();
    }

    //endregion

    //region > helpers

    private Object unwrapped(Object domainObject) {
        return wrapperFactory != null ? wrapperFactory.unwrap(domainObject) : domainObject;
    }
    //endregion

    //region > framework dependencies

    private PersistenceSessionService persistenceSessionService;
    private SpecificationLoader specificationLoader;
    private ConfigurationService configurationService;
    private AuthenticationSessionProvider authenticationSessionProvider;
    private AdapterManager adapterManager;
    private LocalizationProvider localizationProvider;
    private ServicesInjector servicesInjector;


    protected ConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Programmatic
    @Override
    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    protected SpecificationLoader getSpecificationLoader() {
        return specificationLoader;
    }

    @Programmatic
    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLoader) {
        this.specificationLoader = specificationLoader;
    }

    protected AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider;
    }

    @Programmatic
    @Override
    public void setAuthenticationSessionProvider(final AuthenticationSessionProvider authenticationSessionProvider) {
        this.authenticationSessionProvider = authenticationSessionProvider;
    }

    protected AdapterManager getAdapterManager() {
        return adapterManager;
    }

    @Programmatic
    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    protected PersistenceSessionService getPersistenceSessionService() {
        return persistenceSessionService;
    }

    @Programmatic
    @Override
    public void setPersistenceSessionService(final PersistenceSessionService persistenceSessionService) {
        this.persistenceSessionService = persistenceSessionService;
    }

    @Override
    public void setLocalizationProvider(final LocalizationProvider localizationProvider) {
        this.localizationProvider = localizationProvider;
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }


    //endregion

    //region > service dependencies

    @javax.inject.Inject
    WrapperFactory wrapperFactory;

    @javax.inject.Inject
    TranslationService translationService;


    //endregion

}
