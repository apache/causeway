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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class AuditerServiceLogging implements AuditerService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditerServiceLogging.class);

    @PostConstruct
    public void init() {
    }

    @Override
    public boolean isEnabled() {
        return LOG.isDebugEnabled();
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
        LOG.debug(auditMessage);
    }

}
