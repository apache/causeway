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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.schema.chg.v1.ChangesDto;
import org.apache.isis.schema.ixn.v1.InteractionDto;
import org.apache.isis.schema.utils.ChangesDtoUtils;
import org.apache.isis.schema.utils.InteractionDtoUtils;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class PublisherServiceLogging implements PublisherService {

    private static final Logger LOG = LoggerFactory.getLogger(PublisherServiceLogging.class);


    @PostConstruct
    public void init() {
    }

    @Override
    public void publish(final Interaction.Execution<?, ?> execution) {

        if(!LOG.isDebugEnabled()) {
            return;
        }

        final InteractionDto interactionDto =
                InteractionDtoUtils.newInteractionDto(execution, InteractionDtoUtils.Strategy.DEEP);

        LOG.debug(InteractionDtoUtils.toXml(interactionDto));

    }

    @Override
    public void publish(final PublishedObjects publishedObjects) {

        if(!LOG.isDebugEnabled()) {
            return;
        }

        final ChangesDto changesDto = publishedObjects.getDto();

        LOG.debug(ChangesDtoUtils.toXml(changesDto));
    }

    //TODO [ahuber] not used, remove?
    //    @javax.inject.Inject
    //    private CommandContext commandContext;
    //
    //    @javax.inject.Inject
    //    private UserService userService;

}

