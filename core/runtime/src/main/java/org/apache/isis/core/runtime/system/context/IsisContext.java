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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

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

    //region > sessionFactory (static)

    private static IsisSessionFactory sessionFactory;

    public static IsisSessionFactory getSessionFactory() {
        return sessionFactory;
    }


    /**
     * Intended to be called only by {@link IsisSessionFactoryBuilder}.
     */
    public static void setSessionFactory(final IsisSessionFactory sessionFactory) {
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


}
