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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.session;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.apache.isis.metamodel.commons.exceptions.NotYetImplementedException;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.query.QueryPlaceholder;


/**
 * An implementation of {@link Session} and {@link SessionImplementor} that
 * is mostly throws {@link NotYetImplementedException} except for those methods
 * pertaining to creating {@link Criteria} and {@link Query}.
 */
public class SessionPlaceHolder extends SessionPlaceHolderNotImplemented {

    private static final long serialVersionUID = 1L;


    @Override
    @SuppressWarnings("unchecked")
    public Criteria createCriteria(final Class persistentClass, final String alias) {
        return new CriteriaImpl(persistentClass.getName(), alias, this);
    }

    @Override
    public Criteria createCriteria(final String entityName, final String alias) {
        return new CriteriaImpl(entityName, alias, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Criteria createCriteria(final Class persistentClass) {
        return new CriteriaImpl(persistentClass.getName(), this);
    }

    @Override
    public Criteria createCriteria(final String entityName) {
        return new CriteriaImpl(entityName, this);
    }

    @Override
    public Query createQuery(final String queryString) throws HibernateException {
        final Query query = new QueryPlaceholder(queryString);
        query.setComment(queryString);
        return query;
    }

    
    
    
}
