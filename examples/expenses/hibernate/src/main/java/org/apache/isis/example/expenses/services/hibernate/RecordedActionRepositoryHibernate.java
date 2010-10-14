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


package org.apache.isis.example.expenses.services.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.example.expenses.recordedAction.RecordedActionContext;
import org.apache.isis.example.expenses.recordedAction.impl.RecordedAction;
import org.apache.isis.example.expenses.recordedAction.impl.RecordedActionRepositoryAbstract;


public class RecordedActionRepositoryHibernate extends RecordedActionRepositoryAbstract {
    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, Factories or other Services) used by
     * this domain object. The references are injected by the application container.
     */

    // {{ Injected: HibernateHelper
    private HibernateHelper hibernateHelper;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected HibernateHelper getHibernateHelper() {
        return this.hibernateHelper;
    }

    /**
     * Injected by the application container.
     */
    public void setHibernateHelper(final HibernateHelper hibernateHelper) {
        this.hibernateHelper = hibernateHelper;
    }

    // }}

    @Hidden
    public List<RecordedAction> allRecordedActions(final RecordedActionContext context) {
        final Criteria criteria = hibernateHelper.createCriteria(RecordedAction.class);
        criteria.add(Restrictions.eq("context", context));
        return hibernateHelper.findByCriteria(criteria, RecordedAction.class);
    }
}
