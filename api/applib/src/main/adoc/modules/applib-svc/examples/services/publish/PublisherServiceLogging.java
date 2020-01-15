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
package org.apache.isis.applib.services.publish;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.util.schema.ChangesDtoUtils;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.schema.chg.v2.ChangesDto;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisApplib.PublisherServiceLogging")
@Order(OrderPrecedence.LOW)
@Primary
@Qualifier("Logging")
@Log4j2
public class PublisherServiceLogging implements PublisherService {

    @Override
    public void publish(final Interaction.Execution<?, ?> execution) {

        if(!log.isDebugEnabled()) {
            return;
        }

        final InteractionDto interactionDto =
                InteractionDtoUtils.newInteractionDto(execution, InteractionDtoUtils.Strategy.DEEP);

        log.debug(InteractionDtoUtils.toXml(interactionDto));

    }

    @Override
    public void publish(final PublishedObjects publishedObjects) {

        if(!log.isDebugEnabled()) {
            return;
        }

        final ChangesDto changesDto = publishedObjects.getDto();

        log.debug(ChangesDtoUtils.toXml(changesDto));
    }

}

