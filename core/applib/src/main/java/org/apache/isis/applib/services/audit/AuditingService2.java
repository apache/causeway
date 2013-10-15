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
package org.apache.isis.applib.services.audit;

import org.apache.isis.applib.annotation.Hidden;

/**
 * Will be called whenever an object has changed its state.
 */
public interface AuditingService2 extends AuditingService {
    
    @Hidden
    public void audit(String user, long currentTimestampEpoch, String objectType, String identifier, String propertyId, String preValue, String postValue);
    
    
    public static class Stderr extends AuditingService.Stderr implements AuditingService2 {

        @Hidden
        public void audit(String user, long currentTimestampEpoch, String objectType, String identifier, String propertyId, String preValue, String postValue) {
            String auditMessage = objectType + ":" + identifier + " by " + user + ", " + propertyId +": " + preValue + " -> " + postValue;
            System.err.println(auditMessage);
        }
    }

}
