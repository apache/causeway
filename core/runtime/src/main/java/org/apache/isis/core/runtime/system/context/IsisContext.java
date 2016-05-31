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

package org.apache.isis.core.runtime.system.context;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.components.TransactionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationException;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.services.l10n.LocalizationDefault;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.internal.InitialisationSession;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

/**
 * Provides singleton <i>access to</i> the current (session scoped)
 * {@link IsisSession}, along with convenience methods to obtain
 * application-scoped components and also any transaction-scoped components
 * {@link TransactionScopedComponent} s if a {@link IsisTransaction}
 * {@link IsisSession#getCurrentTransaction() is in progress}.
 */
public class IsisContext {

    private static final Logger LOG = LoggerFactory.getLogger(IsisContext.class);

    //region > singleton
    private static IsisContext singleton;

    /**
     * Returns the singleton providing access to the set of execution contexts.
     */
    public static IsisContext getInstance() {
        return singleton;
    }

    /**
     * Whether a singleton has been created using {@link #getInstance()}.
     */
    public static boolean exists() {
        return singleton != null;
    }

    /**
     * Resets the singleton, so another can created.
     */
    public static void testReset() {
        singleton = null;
    }

    //endregion


    //region > metaModelInvalidExceptionIfAny (static)
    /**
     * Populated only if the metamodel was found to be invalid
     */
    private static MetaModelInvalidException metamodelInvalidException;

    public static MetaModelInvalidException getMetaModelInvalidExceptionIfAny() {
        return IsisContext.metamodelInvalidException;
    }
    public static void setMetaModelInvalidException(final MetaModelInvalidException metaModelInvalid) {
        IsisContext.metamodelInvalidException = metaModelInvalid;
    }
    //endregion

    private static IsisConfiguration configuration;

    public static IsisContext createInstance(final IsisSessionFactory sessionFactory) {
        return new IsisContext(sessionFactory);
    }

    //region > constructor, fields

    protected enum SessionClosePolicy {
        /**
         * Sessions must be explicitly closed.
         *
         * @deprecated - in 1.9.0-SNAPSHOT it has been reported that on occasion the session is not explicitly closed.  This must mean that there's a leakage somewhere.  Using auto close instead will make the system overall more able to "repair itself" when this type of error (presumably a bug in our session management code) occurs
         */
        @Deprecated
        EXPLICIT_CLOSE,
        /**
         * Sessions will be automatically closed.
         */
        AUTO_CLOSE
    }



    private final IsisSessionFactory sessionFactory;
    private final SessionClosePolicy sessionClosePolicy;



    /**
     * Creates a new instance of the {@link IsisSession} holder.
     * 
     * <p>
     * Will throw an exception if an instance has already been created.
     */
    protected IsisContext(
            final SessionClosePolicy sessionClosePolicy,
            final IsisSessionFactory sessionFactory) {
        if (singleton != null) {
            throw new IsisException("Isis Context already set up");
        }
        singleton = this;
        this.sessionFactory = sessionFactory;
        this.sessionClosePolicy = sessionClosePolicy;
    }


    protected IsisContext(final IsisSessionFactory sessionFactory) {
        this(SessionClosePolicy.AUTO_CLOSE, sessionFactory);
    }

    /**
     * As injected in constructor.
     */
    public final IsisSessionFactory getSessionFactoryInstance() {
        return sessionFactory;
    }

    //endregion

    //region > shutdown

    protected void shutdownInstance() {
        this.sessionFactory.shutdown();
    }

    public static void shutdown() {
        if(exists()) {
            getInstance().shutdownInstance();
        }
    }


    //endregion

    //region > Policies

    /**
     * Whether any open session can be automatically
     * {@link #closeSessionInstance() close}d on
     * {@link #openSessionInstance(AuthenticationSession) open}.
     */
    public final boolean isSessionAutocloseable() {
        return sessionClosePolicy == SessionClosePolicy.AUTO_CLOSE;
    }

    /**
     * Helper method for subclasses' implementation of
     * {@link #openSessionInstance(AuthenticationSession)}.
     */
    protected void applySessionClosePolicy() {
        if (getSessionInstance() == null) {
            return;
        }
        if (!isSessionAutocloseable()) {
            throw new IllegalStateException("Session already open and context not configured for autoclose");
        }
        closeSessionInstance();
    }

    //endregion

    //region > openSessionInstance / closeSessionInstance, getSessionInstance

    private final ThreadLocal<IsisSession> currentSession = new ThreadLocal<>();

    /**
     * Creates a new {@link IsisSession} and binds into the current context.
     * 
     * Is only intended to be called through
     * {@link IsisContext#openSession(AuthenticationSession)}.
     *
     * <p>
     * Implementation note: an alternative design would have just been to bind
     * onto a thread local.
     *
     * @throws IllegalStateException
     *             if already opened.
     */
    public IsisSession openSessionInstance(final AuthenticationSession authenticationSession) {
        applySessionClosePolicy();
        final IsisSession session = getSessionFactoryInstance().openSession(authenticationSession);
        currentSession.set(session);
        session.open();
        return session;
    }

    /**
     * Closes the {@link IsisSession} for the current context.
     * 
     * <p>
     * Ignored if already closed.
     * 
     * <p>
     * This method is <i>not</i> marked <tt>final</tt> so it can be overridden
     * if necessarily. Generally speaking this shouldn't be necessary; one case
     * where it might though is if an implementation has multiple concurrent
     * uses of a session, in which case "closing" the session really means just
     * deregistering the usage of it by a particular thread; only when all
     * threads have finished with a session can it really be closed.
     */
    public void closeSessionInstance() {
        final IsisSession isisSession = getSessionInstance();
        if (isisSession != null) {
            isisSession.close();
            currentSession.set(null);
        }
    }

    /**
     * Locates the current {@link IsisSession} from the threadlocal.
     *
     * @see #openSessionInstance(AuthenticationSession)
     */
    public IsisSession getSessionInstance() {
        return currentSession.get();
    }

    //endregion

    //region > Static Convenience methods (session management)


    /**
     * Convenience method to open a new {@link IsisSession}.
     * 
     * @see #openSessionInstance(AuthenticationSession)
     */
    public static IsisSession openSession(final AuthenticationSession authenticationSession) {
        return getInstance().openSessionInstance(authenticationSession);
    }

    /**
     * Convenience method to close the current {@link IsisSession}.
     * 
     * @see #closeSessionInstance()
     */
    public static void closeSession() {
        getInstance().closeSessionInstance();
    }

    //endregion

    //region > Static Convenience methods (application scoped)
    /**
     * Convenience method returning the {@link IsisSessionFactory} of the
     * current {@link #getSession() session}.
     */
    public static IsisSessionFactory getSessionFactory() {
        return getInstance().getSessionFactoryInstance();
    }

    /**
     * Convenience method.
     * 
     * @see IsisSessionFactory#getConfiguration()
     */
    public static IsisConfiguration getConfiguration() {
        if (configuration == null) {
            throw new IsisConfigurationException("No configuration available");
        }
        return configuration;
    }

    public static void setConfiguration(final IsisConfiguration configuration) {
        IsisContext.configuration = configuration;
    }

    /**
     * Convenience method.
     * 
     * @see IsisSessionFactory#getDeploymentType()
     */
    public static DeploymentType getDeploymentType() {
        return getSessionFactory().getDeploymentType();
    }

    /**
     * Convenience method.
     * 
     * @see IsisSessionFactory#getSpecificationLoader()
     */
    public static SpecificationLoader getSpecificationLoader() {
        return getSessionFactory().getSpecificationLoader();
    }

    /**
     * Convenience method.
     * 
     * @see IsisSessionFactory#getAuthenticationManager()
     */
    public static AuthenticationManager getAuthenticationManager() {
        return getSessionFactory().getAuthenticationManager();
    }

    public static List<Object> getServices() {
        return getSessionFactory().getServices();
    }

    /**
     * Convenience method.
     * 
     * @see IsisSessionFactory#getOidMarshaller()
     */
    public static OidMarshaller getOidMarshaller() {
        return getSessionFactory().getOidMarshaller();
    }

    //endregion

    //region > Static Convenience methods (session scoped)

    public static boolean inSession() {
        final IsisSession session = getInstance().getSessionInstance();
        return session != null;
    }

    /**
     * Convenience method returning the current {@link IsisSession}.
     */
    public static IsisSession getSession() {
        final IsisSession session = getInstance().getSessionInstance();
        if (session == null) {
            throw new IllegalStateException("No Session opened for this thread");
        }
        return session;
    }

    /**
     * @see IsisSession#getAuthenticationSession()
     */
    public static AuthenticationSession getAuthenticationSession() {
        return getSession().getAuthenticationSession();
    }

    /**
     * Convenience method.
     * 
     * @see IsisSession#getPersistenceSession()
     */
    public static PersistenceSession getPersistenceSession() {
        return getSession().getPersistenceSession();
    }

    /**
     * Convenience method.
     */
    public static Localization getLocalization() {
        return new LocalizationDefault();
    }

    /**
     * Convenience methods
     * 
     * @see IsisSession#getPersistenceSession()
     * @see PersistenceSession#getTransactionManager()
     */
    public static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    //endregion

    //region > Static Convenience methods (transaction scoped)

    public static boolean inTransaction() {
        if (inSession())
            if (getCurrentTransaction() != null)
                if (!getCurrentTransaction().getState().isComplete())
                    return true;
        return false;
    }

    /**
     * Convenience method, returning the current {@link IsisTransaction
     * transaction} (if any).
     * 
     * <p>
     * Transactions are managed using the {@link IsisTransactionManager}
     * obtainable from the {@link IsisSession's} {@link PersistenceSession}.
     * 
     * @see IsisSession#getCurrentTransaction()
     * @see PersistenceSession#getTransactionManager()
     */
    public static IsisTransaction getCurrentTransaction() {
        return getSession().getCurrentTransaction();
    }

    /**
     * Convenience method, returning the {@link org.apache.isis.core.commons.authentication.MessageBroker} of the
     * {@link #getCurrentTransaction() current transaction}.
     */
    public static MessageBroker getMessageBroker() {
        return getCurrentTransaction().getMessageBroker();
    }


    /**
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param runnable The piece of code to run.
     */
    public static void doInSession(final Runnable runnable) {
        doInSession(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                runnable.run();
                return null;
            }
        });
    }

    /**
     * A template method that executes a piece of code in a session.
     * If there is an open session then it is reused, otherwise a temporary one
     * is created.
     *
     * @param callable The piece of code to run.
     * @return The result of the code execution.
     */
    public static <R> R doInSession(Callable<R> callable) {
        boolean noSession = !inSession();
        try {
            if (noSession) {
                openSession(new InitialisationSession());
            }

            return callable.call();
        } catch (Exception x) {
            throw new RuntimeException(
                String.format("An error occurred while executing code in %s session", noSession ? "a temporary" : "a"),
                x);
        } finally {
            if (noSession) {
                closeSession();
            }
        }
    }

    //endregion



}
