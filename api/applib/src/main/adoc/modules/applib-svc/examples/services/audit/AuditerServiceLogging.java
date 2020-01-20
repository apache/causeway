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

import java.sql.Timestamp;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisApplib.AuditerServiceLogging")
@Order(OrderPrecedence.LATE)
@Primary
@Qualifier("logging")
@Log4j2
public class AuditerServiceLogging implements AuditerService {

    @PostConstruct
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void audit(
            final UUID interactionId, int sequence,
            final String targetClassName, final Bookmark target,
            final String memberId, final String propertyName,
            final String preValue, final String postValue,
            final String user, final Timestamp timestamp) {

        String auditMessage =
                interactionId + "," + sequence + ": " +
                        target.toString() + " by " + user + ", " + propertyName + ": " + preValue + " -> " + postValue;
        log.debug(auditMessage);
    }

}
