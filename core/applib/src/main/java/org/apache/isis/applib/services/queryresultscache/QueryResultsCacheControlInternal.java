/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.queryresultscache;

import javax.annotation.PostConstruct;

import org.apache.isis.applib.AbstractSubscriber;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.fixturescripts.events.FixturesInstalledEvent;
import org.apache.isis.applib.fixturescripts.events.FixturesInstallingEvent;


/**
 * In separate class because {@link QueryResultsCache} itself is request-scoped
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class QueryResultsCacheControlInternal extends AbstractSubscriber implements QueryResultCacheControl {

    @PostConstruct
    @Override
    public void postConstruct() {

        super.postConstruct();

        eventBusService.addEventListener(FixturesInstallingEvent.class, ev->{
            fixturesInstalling = true;
        });

        eventBusService.addEventListener(FixturesInstalledEvent.class, ev->{
            fixturesInstalling = false;
        });

    }

    private boolean fixturesInstalling;

    @Override
    public boolean isFixturesInstalling() {
        return fixturesInstalling;
    }
}
