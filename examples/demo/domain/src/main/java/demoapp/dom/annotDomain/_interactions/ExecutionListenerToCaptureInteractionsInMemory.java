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
package demoapp.dom.annotDomain._interactions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber;
import org.apache.isis.applib.util.schema.InteractionDtoUtils;
import org.apache.isis.schema.ixn.v2.InteractionDto;

import lombok.val;

//tag::class[]
@Service
public class ExecutionListenerToCaptureInteractionsInMemory implements ExecutionSubscriber {

    private final List<InteractionDto> executions = new ArrayList<>();

    @Override
    public void onExecution(Interaction.Execution<?, ?> execution) {
        val dto = InteractionDtoUtils.newInteractionDto(            // <.>
                    execution, InteractionDtoUtils.Strategy.DEEP);
        executions.add(dto);
    }
    // ...
//end::class[]

//tag::demo[]
    public Stream<InteractionDto> streamInteractionDtos() {
        return executions.stream();
    }

    public void clear() {
        executions.clear();
    }
//end::demo[]

//tag::class[]
}
//end::class[]
