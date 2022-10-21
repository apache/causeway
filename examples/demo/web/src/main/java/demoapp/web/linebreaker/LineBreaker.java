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
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.causeway.core.interaction.session.CausewayInteraction;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 *
 * REST endpoint to allow for remote application shutdown
 *
 */
@Named("demo.LineBreaker")
@DomainService(
        nature = NatureOfService.REST
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
        .map(CausewayInteraction.class::cast)
        .ifPresent(interaction->{
            interaction.setOnClose(()->System.exit(0));
        });
    }


}
