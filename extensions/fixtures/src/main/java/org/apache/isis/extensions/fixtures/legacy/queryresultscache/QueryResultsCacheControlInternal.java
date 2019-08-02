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
package org.apache.isis.extensions.fixtures.legacy.queryresultscache;

import javax.inject.Singleton;

import org.springframework.context.event.EventListener;

import org.apache.isis.applib.services.queryresultscache.QueryResultCacheControl;
import org.apache.isis.applib.services.queryresultscache.QueryResultsCache;
import org.apache.isis.extensions.fixtures.events.FixturesInstalledEvent;
import org.apache.isis.extensions.fixtures.events.FixturesInstallingEvent;


/**
 * In separate class because {@link QueryResultsCache} itself is request-scoped
 */
@Singleton
public class QueryResultsCacheControlInternal implements QueryResultCacheControl {

    @EventListener(FixturesInstallingEvent.class)
    public void onFixturesInstallingEvent(FixturesInstallingEvent ev) {
        fixturesInstalling = true;
    }
    
    @EventListener(FixturesInstalledEvent.class)
    public void onFixturesInstallingEvent(FixturesInstalledEvent ev) {
        fixturesInstalling = false;
    }

    private boolean fixturesInstalling;

    @Override
    public boolean isIgnoreCache() {
        return fixturesInstalling;
    }
}
