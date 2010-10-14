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


package org.apache.isis.example.expenses.recordedAction;

/**
 * An implementation of this Service will be injected into any RecordedActionContext, thereby allowing that
 * context to record an action.
 * 
 * @author Richard
 * 
 */
public interface RecordActionService {

    /**
     * Creates and persists a RecordedAction object. Details field is optional.
     */
    public void recordMenuAction(RecordedActionContext context, String action, String details);

    public void recordFieldChange(RecordedActionContext context, String fieldName, Object previousContents, Object newContents);

}
