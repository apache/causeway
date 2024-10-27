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
import javax.annotation.Priority;
import javax.inject.Inject;

import org.apache.causeway.applib.annotation.PriorityPrecedence;

import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;

import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.stereotype.Component;

/**
 * Default implementation that supports Spring Boot's {@link DeadlockLoserDataAccessException} and also the standard
 * message thrown by SQL Server.
 *
 * @since 2.1
 */
@Component
@Priority(PriorityPrecedence.LATE)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Log4j2
public class DeadlockRecognizerDefault implements DeadlockRecognizer {

    static final String SQL_SERVER_DEADLOCK_MESSAGE = "chosen as the deadlock victim";

    @Override
    public boolean isDeadlock(Throwable ex) {
        var whetherDeadlock = ex instanceof DeadlockLoserDataAccessException || isMessage(ex, SQL_SERVER_DEADLOCK_MESSAGE);
        if (whetherDeadlock) {
            log.warn("Detected deadlock");
            log.debug("Detected deadlock details:", ex);
        }
        return whetherDeadlock;
    }

    private static boolean isMessage(Throwable ex, String message) {
        return isMessage(ex.getMessage(), message);
    }

    private static boolean isMessage(String exMessage, String message) {
        return exMessage != null && exMessage.contains(message);
    }

}
