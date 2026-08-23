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
package demoapp.dom.domain._changes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.services.publishing.spi.EntityChanges;
import org.apache.causeway.applib.services.publishing.spi.EntityChangesSubscriber;
import org.apache.causeway.schema.chg.v2.ChangesDto;

//tag::class[]
@Service
public class EntityChangesSubscriberToCaptureChangesInMemory implements EntityChangesSubscriber {

    private final List<ChangesDto> changedEntities = new ArrayList<>();

    @Override
    public void onChanging(
            final EntityChanges changingEntities       // <.>
    ) {
        var dto = changingEntities.getDto();
        this.changedEntities.add(dto);
    }
    // ...
//end::class[]

//tag::demo[]
    public Stream<ChangesDto> streamChangedEntities() {
        return changedEntities.stream();
    }

    public void clear() {
        changedEntities.clear();
    }
//end::demo[]

//tag::class[]
}
//end::class[]
