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
package org.apache.causeway.persistence.commons.integration.deadlock;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.PriorityPrecedence;

import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;

import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Component;

@Component
@Priority(PriorityPrecedence.LATE)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class DeadlockRecognizerDefault implements DeadlockRecognizer {

    @Override
    public boolean isDeadlock(Throwable ex) {
        val whetherDeadlock = ex instanceof DeadlockLoserDataAccessException ||
                (ex.getMessage() != null && ex.getMessage().contains("chosen as the deadlock victim"));
        if (whetherDeadlock) {
            log.warn("Detected deadlock");
            log.debug("Detected deadlock details:", ex);
        }
        return whetherDeadlock;
    }

}
