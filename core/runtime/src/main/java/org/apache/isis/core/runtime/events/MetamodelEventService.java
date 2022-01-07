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
package org.apache.isis.core.runtime.events;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.services.confview.ConfigurationViewService;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.core.metamodel.events.MetamodelEvent;

/**
 *
 * @since 2.0
 * @implNote Listeners to runtime events can only reliably receive these after the
 * post-construct phase has finished and before the pre-destroy phase has begun.
 */
@Service
@Named("isis.runtime.MetamodelEventService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class MetamodelEventService {

    @Inject
    private EventBusService eventBusService;

    @Autowired(required = false)
    private ConfigurationViewService configurationService;

    public void fireBeforeMetamodelLoading() {

        if(configurationService!=null) {
            _Xray.addConfiguration(configurationService);
        }

        eventBusService.post(MetamodelEvent.BEFORE_METAMODEL_LOADING);
    }

    public void fireAfterMetamodelLoaded() {
        eventBusService.post(MetamodelEvent.AFTER_METAMODEL_LOADED);
    }

}
