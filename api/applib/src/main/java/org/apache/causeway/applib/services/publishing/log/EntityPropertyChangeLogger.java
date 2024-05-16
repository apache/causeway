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
package org.apache.causeway.applib.services.publishing.log;

import javax.annotation.Priority;
import javax.inject.Named;

import org.apache.causeway.commons.collections.Can;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChangeSubscriber;

import lombok.extern.log4j.Log4j2;

/**
 * Simple implementation of {@link EntityPropertyChangeSubscriber} that just logs out the {@link EntityPropertyChange}
 * to a debug log.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(EntityPropertyChangeLogger.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("logging")
@Log4j2
public class EntityPropertyChangeLogger implements EntityPropertyChangeSubscriber {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".EntityPropertyChangeLogger";

    @Override
    public boolean isEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void onChanging(final EntityPropertyChange entityPropertyChange) {
        log.debug(entityPropertyChange.toString());
    }

    @Override
    public void onBulkChanging(Can<EntityPropertyChange> entityPropertyChanges) {
        entityPropertyChanges.stream().map(EntityPropertyChange::toString).forEach(log::debug);
    }
}