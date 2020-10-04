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
package demoapp.dom.annotDomain._changes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.applib.services.publish.PublisherService;
import org.apache.isis.schema.chg.v2.ChangesDto;

import lombok.val;

//tag::class[]
@Service
public class PublisherServiceToCaptureChangesInMemory implements PublisherService {

    private final List<ChangesDto> publishedObjects = new ArrayList<>();

    @Override
    public void publish(
            PublishedObjects publishedObjects       // <.>
    ) {
        val dto = publishedObjects.getDto();
        this.publishedObjects.add(dto);
    }
    // ...
//end::class[]

//tag::demo[]
    public Stream<ChangesDto> streamPublishedObjects() {
        return publishedObjects.stream();
    }

    public void clear() {
        publishedObjects.clear();
    }
//end::demo[]

    @Override
    public void publish(Interaction.Execution<?, ?> execution) {
    }

//tag::class[]
}
//end::class[]
