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

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.apache.isis.commons.ensure.Assert;


/**
 * InstancesCriteria based on a Hibernate Criteria
 */
public class HibernateCriteriaCriteria extends HibernateInstancesCriteria {
    private final CriteriaImpl criteria;

    public HibernateCriteriaCriteria(final Class<?> cls, final Criteria criteria, final int resultType) {
        super(cls, resultType);
        Criteria rootCriteria = criteria;
        while (!(rootCriteria instanceof CriteriaImpl)) {
            Assert.assertTrue(rootCriteria instanceof CriteriaImpl.Subcriteria);
            rootCriteria = ((CriteriaImpl.Subcriteria) rootCriteria).getParent();
            Assert.assertFalse(criteria == rootCriteria); // if circular reference could loop forever
        }
        this.criteria = (CriteriaImpl) rootCriteria;
    }

    @Override
    public void setSession(final Session session) {
        Assert.assertTrue(session instanceof SessionImplementor);
        criteria.setSession((SessionImplementor) session);
    }

    public Criteria getCriteria() {
        return criteria;
    }

    @Override
    public List<?> getResults() {
        switch (getResultType()) {
        case LIST: {
            return criteria.list();
        }
        case UNIQUE_RESULT: {
            final Object result = criteria.uniqueResult();
            return result == null ? Collections.EMPTY_LIST : Arrays.asList(new Object[] { result });
        }
        default:
            throw new RuntimeException("Result type out of range");
        }
    }
}
