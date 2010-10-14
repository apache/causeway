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

import org.hibernate.Session;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.query.PersistenceQuery;


/**
 * Superclass of all InstancesCriteria which use Hibernate to access the database.
 */
public abstract class HibernateInstancesCriteria implements PersistenceQuery {
    public static final int LIST = 0;
    public static final int UNIQUE_RESULT = 1;

    private final Class<?> type;
    private final int resultType;

    public HibernateInstancesCriteria(final Class<?> type, final int resultType) {
        this.resultType = resultType;
        this.type = type;
    }

    /**
     * provided for serialization purposes only.
     */
    protected HibernateInstancesCriteria() {
        type = null;
        resultType = LIST;
    }

    public ObjectSpecification getSpecification() {
        return IsisContext.getSpecificationLoader().loadSpecification(type);
    }

    /**
     * Not required as this will be in the Hibernate query/criteria
     */
    public boolean includeSubclasses() {
        return false;
    }

    /**
     * Not required as this will be decided by the Hibernate query/criteria
     */
    public boolean matches(final ObjectAdapter object) {
        return false;
    }

    /**
     * Return the results of executing the Hibernate query.
     * 
     * @return a List of persistent Objects
     */
    public abstract List<?> getResults();

    public abstract void setSession(final Session session);

    public int getResultType() {
        return resultType;
    }

    protected Class<?> getCls() {
        return type;
    }

}
