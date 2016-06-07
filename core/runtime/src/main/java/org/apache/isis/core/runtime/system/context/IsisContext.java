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

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.commons.config.IsisConfiguration;
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
 * Simply a static field holding the {@link IsisSessionFactory} singleton, and conveneince methods to obtain the
 * current {@link IsisSession}, along with application-scoped components and also any transaction-scoped components.
 */
public final class IsisContext {

    private static final Logger LOG = LoggerFactory.getLogger(IsisContext.class);

    private IsisContext(){
        throw new IllegalStateException("Never instantiated");
    }

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

    //region > set (the sessionFactory)

    private static IsisSessionFactory sessionFactory;

    public static void set(final IsisSessionFactory sessionFactory) {
        if (IsisContext.sessionFactory != null) {
            throw new IsisException("SessionFactory already set up");
        }
        IsisContext.sessionFactory = sessionFactory;
    }


    /**
     * Resets
     */
    public static void testReset() {
        sessionFactory = null;
    }

    //endregion

    //region > shutdown

    public static void shutdown() {
        if (sessionFactory == null) {
            return;
        }
        sessionFactory.shutdown();
    }

    //endregion

    //region > Static Convenience methods (session management)


    /**
     * Convenience method to open a new {@link IsisSession}.
     */
    public static IsisSession openSession(final AuthenticationSession authenticationSession) {
        return sessionFactory.openSession(authenticationSession);
    }

    /**
     * Convenience method to close the current {@link IsisSession}.
     */
    public static void closeSession() {
        sessionFactory.closeSession();
    }

    //endregion

    //region > Static Convenience methods (application scoped)
    /**
     * Convenience method returning the {@link IsisSessionFactory} of the
     * current {@link #getCurrentSession() session}.
     */
    public static IsisSessionFactory getSessionFactory() {
        return sessionFactory;
    }


    /**
     * Convenience method.
     * 
     * @see IsisSessionFactory#getConfiguration()
     */
    public static IsisConfiguration getConfiguration() {
        return getSessionFactory().getConfiguration();
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
        return sessionFactory.inSession();
    }

    /**
     * Convenience method returning the current {@link IsisSession}.
     */
    public static IsisSession getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * @see IsisSession#getAuthenticationSession()
     */
    public static AuthenticationSession getAuthenticationSession() {
        return sessionFactory.getCurrentSession().getAuthenticationSession();
    }

    /**
     * Convenience method.
     * 
     * @see IsisSession#getPersistenceSession()
     */
    public static PersistenceSession getPersistenceSession() {
        return sessionFactory.getCurrentSession().getPersistenceSession();
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
        if (inSession()) {
            if (getCurrentTransaction() != null) {
                if (!getCurrentTransaction().getState().isComplete()) {
                    return true;
                }
            }
        }
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
        return getCurrentSession().getCurrentTransaction();
    }

    /**
     * Convenience method, returning the {@link org.apache.isis.core.commons.authentication.MessageBroker} of the
     * {@link #getCurrentTransaction() current transaction}.
     */
    public static MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
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
