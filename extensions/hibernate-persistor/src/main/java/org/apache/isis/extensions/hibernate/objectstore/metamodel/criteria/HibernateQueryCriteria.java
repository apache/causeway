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


package org.apache.isis.extensions.hibernate.objectstore.metamodel.criteria;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.apache.isis.commons.ensure.Assert;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.query.QueryPlaceholder;


/**
 * Criteria based on a Hibernate Query
 */
public class HibernateQueryCriteria extends HibernateInstancesCriteria {
    private final QueryPlaceholder query;
    private transient Session session;

    public HibernateQueryCriteria(final Class<?> cls, final QueryPlaceholder query, final int resultType) {
        super(cls, resultType);
        this.query = query;
    }

    @Override
    public void setSession(final Session session) {
        this.session = session;
    }

    @Override
    public List<?> getResults() {
        Assert.assertNotNull(session);
        query.setSession(session);
        switch (getResultType()) {
        case LIST: {
            return query.list();
        }
        case UNIQUE_RESULT: {
            final Object result = query.uniqueResult();
            return result == null ? Collections.EMPTY_LIST : Arrays.asList(new Object[] { result });
        }
        default:
            throw new RuntimeException("Result type out of range");
        }
    }

    public QueryPlaceholder getQuery() {
        return query;
    }

}
