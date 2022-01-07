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
package demoapp.web.linebreaker;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.NatureOfService;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.core.interaction.session.IsisInteraction;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 *
 * REST endpoint to allow for remote application shutdown
 *
 */
@DomainService(
        nature = NatureOfService.REST,
        logicalTypeName = "demo.LineBreaker"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
@Log4j2
public class LineBreaker {

    final InteractionLayerTracker iInteractionLayerTracker;

    @Action(semantics = SemanticsOf.SAFE)
    public void shutdown() {
        log.info("about to shutdown the JVM");

        // allow for current interaction to complete gracefully
        iInteractionLayerTracker.currentInteraction()
        .map(IsisInteraction.class::cast)
        .ifPresent(interaction->{
            interaction.setOnClose(()->System.exit(0));
        });
    }


}
