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

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.query.QueryPlaceholder;
import org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.session.SessionPlaceHolder;
import org.apache.isis.runtime.persistence.services.RepositoryHelper;


public abstract class HibernateFactoryAndRepository extends AbstractFactoryAndRepository {

    private static final SessionPlaceHolder sessionPlaceHolder = new SessionPlaceHolder();

    /**
     * Create a Hibernate Criteria for the class persisted by this repository
     */
    protected Criteria createCriteria(final Class<?> cls) {
        return sessionPlaceHolder.createCriteria(cls);
    }

    /**
     * Create a Hibernate Query using a complete query string. This can be used to query any entity.
     */
    protected Query createQuery(final String query) {
        return sessionPlaceHolder.createQuery(query);
    }

    /**
     * Return a named Hibernate Query which is defined in hibernate.cfg.xml.
     * 
     * @param name
     *            the Query name
     */
    protected Query getNamedQuery(final String name) {
        // return getSession().getNamedQuery(name);
        return null;
    }

    /**
     * A shortcut for creating a count(*) Query on the class for this repository. The query generated is
     * 
     * <pre>
     * select count(*) from &lt;cls&gt; as o {where &lt;whereClause&gt;}
     * </pre>
     * 
     * The whereClause is optional.
     * <p>
     * To generate more complex queries, possibly using other classes, use the {@link #createQuery(String)}}
     * method
     * 
     * @param whereClause
     *            the where clause, not including "where", or null to count all instances.
     */
    protected Query createCountQuery(final String whereClause, final Class<?> cls) {
        String query;
        if (whereClause == null) {
            query = "select count(*) from " + cls.getName() + " as o";
        } else {
            query = "select count(*) from " + cls.getName() + " as o where " + whereClause;
        }
        return createQuery(query);
    }

    /**
     * A shortcut for creating a Query on the class for this repository. The query generated is
     * 
     * <pre>
     * from &lt;cls&gt; as o where &lt;whereClause&gt;
     * </pre>
     * 
     * The whereClause must be specified, to select all instances use {@link #allInstances()}.
     * <p>
     * To generate more complex queries, possibly using other classes, use {@link #createQuery(String)}}.
     * 
     * @param whereClause
     *            the where clause, not including "where"
     */
    protected Query createEntityQuery(final String whereClause, final Class<?> cls) {
        return createQuery("from " + cls.getName() + " as o where " + whereClause);
    }

    protected List<?> findByCriteria(final Criteria criteria, final Class<?> cls) {
        return findByCriteria(new HibernateCriteriaCriteria(cls, criteria, HibernateInstancesCriteria.LIST), cls);
    }

    protected Object findUniqueResultByCriteria(final Criteria criteria, final Class<?> cls) {
        final List<?> list = findByCriteria(new HibernateCriteriaCriteria(cls, criteria, HibernateInstancesCriteria.UNIQUE_RESULT),
                cls);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    protected Object findUniqueResultByQuery(final Query query, final Class<?> cls) {
        final List<?> list = findByCriteria(new HibernateQueryCriteria(cls, (QueryPlaceholder) query,
                HibernateInstancesCriteria.UNIQUE_RESULT), cls);
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    protected Object findFirstByCriteria(final Criteria criteria, final Class<?> cls) {
        criteria.setMaxResults(1);
        return getFirst(findByCriteria(criteria, cls));
    }

    protected List<?> findByQuery(final Query query, final Class<?> cls) {
        return findByCriteria(new HibernateQueryCriteria(cls, (QueryPlaceholder) query, HibernateInstancesCriteria.LIST), cls);
    }

    protected Object findFirstByQuery(final Query query, final Class<?> cls) {
        query.setMaxResults(1);
        return getFirst(findByQuery(query, cls));
    }

    protected List<?> findByQuery(final String query, final Class<?> cls) {
        return findByQuery(createQuery(query), cls);
    }

    protected Object findFirstByQuery(final String query, final Class<?> cls) {
        return findFirstByQuery(createQuery(query), cls);
    }

    /**
     * Return the first object in a list. If the array is null, or is empty, then return null.
     */
    protected Object getFirst(final List<?> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);

    }

    /**
     * Return true if the Query returns an integer > 0. The Query MUST return a single value, which must be an
     * integer, e.g. "select count(*) from myClass". This is a utility method to help with common repository
     * usage.
     */
    protected boolean countNotZero(final Query query) {
        final int count = ((Integer) query.uniqueResult()).intValue();
        return count > 0;

    }

    protected List<?> findByCriteria(final HibernateInstancesCriteria criteria, final Class<?> cls) {
        return RepositoryHelper.findByPersistenceQuery(criteria, cls);
    }
}
