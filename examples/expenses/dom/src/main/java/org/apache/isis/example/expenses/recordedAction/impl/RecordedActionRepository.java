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

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.example.expenses.recordedAction.RecordedActionContext;

import java.util.List;


/**
 * Defines methods retrieving the actions associated with a given context.
 * 
 */
public interface RecordedActionRepository {

    final static int MAX_RECORDED_ACTIONS = 10;

    /**
     * Returns the RecordedActions for a given context. Note: This is a rather naive implementation. In a real
     * application, objects might eventually accumulate too many recorded actions to be retrieved in one go,
     * so it would be more appropriate to specify a method that retrieved only the most recent 10, say, plus a
     * separate method for retrieving RecordedActions that match specified parameters and/or date range.
     * 
     * @return
     */
    @Hidden
    List<RecordedAction> allRecordedActions(RecordedActionContext context);

}
