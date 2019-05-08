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
package org.apache.isis.core.runtime.services.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.RequestScoped;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.jdo.listener.LoadLifecycleListener;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.WithTransactionScope;
import org.apache.isis.applib.services.metrics.MetricsService;
import org.apache.isis.core.runtime.system.transaction.ChangedObjectsServiceInternal;

@RequestScoped
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class MetricsServiceDefault implements MetricsService, InstanceLifecycleListener, LoadLifecycleListener, WithTransactionScope {

    private AtomicInteger numberLoaded = new AtomicInteger(0);

    @Override
    public int numberObjectsLoaded() {
        return numberLoaded.get();
    }

    @Override
    public int numberObjectsDirtied() {
        return changedObjectsServiceInternal.numberObjectsDirtied();
    }

    @Programmatic
    @Override
    public void postLoad(final InstanceLifecycleEvent event) {
        numberLoaded.incrementAndGet();
    }

    /**
     * Intended to be called at the end of a transaction.  (This service really ought to be considered
     * a transaction-scoped service; since that isn't yet supported by the framework, we have to manually reset).
     */
    @Programmatic
    @Override
    public void resetForNextTransaction() {
        numberLoaded.set(0);
    }

    @javax.inject.Inject
    ChangedObjectsServiceInternal changedObjectsServiceInternal;

}
