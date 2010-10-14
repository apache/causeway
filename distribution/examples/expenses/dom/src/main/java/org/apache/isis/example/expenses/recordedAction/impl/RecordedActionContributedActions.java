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


package org.apache.isis.example.expenses.recordedAction.impl;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.example.expenses.recordedAction.RecordedActionContext;

import java.util.List;


/**
 * Defines user actions made available on objects implementing RecordedActionContext
 * 
 */
@Named("Recorded Actions")
public class RecordedActionContributedActions extends AbstractService {

    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, Factories or other Services) used by
     * this domain object. The references are injected by the application container.
     */

    // {{ Injected: RecordedActionRepository
    private RecordedActionRepository recordedActionRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected RecordedActionRepository getRecordedActionRepository() {
        return this.recordedActionRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setRecordedActionRepository(final RecordedActionRepository recordedActionRepository) {
        this.recordedActionRepository = recordedActionRepository;
    }

    // }}

    // }}

    /**
     * Returns the most recently-recorded actions on a context, up to a system-defined maximum of, say 10.
     */
    public List<RecordedAction> allRecordedActions(final RecordedActionContext context) {
        return recordedActionRepository.allRecordedActions(context);
    }

}
