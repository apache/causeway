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
package demoapp.dom.featured.causewayext.sse;

import java.util.concurrent.atomic.LongAdder;

import javax.inject.Named;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.commons.internal.concurrent._ThreadSleep;
import org.apache.causeway.extensions.sse.applib.annotations.SseSource;
import org.apache.causeway.extensions.sse.applib.service.SseChannel;

import lombok.RequiredArgsConstructor;

//tag::class[]
@Named("demo.DemoTask")
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.DISABLED)
@RequiredArgsConstructor(staticName="of")
public class DemoTask implements SseSource {
    // ...
//end::class[]

    @ObjectSupport public String title() {
        return String.format("DemoTask '%s'", Integer.toHexString(hashCode()));
    }

    private final int totalSteps;
    private TaskProgress taskProgress;

//tag::class[]
    @Override
    public void run(final SseChannel eventStream) {                     // <.>
        taskProgress = TaskProgress.of(new LongAdder(), totalSteps);
        for(int i=0; i<totalSteps; ++i) {
            _ThreadSleep.millis(1000);
            taskProgress.getStepsProgressed().increment();
            eventStream.fire(this);                                     // <.>
        }
    }

    @Override
    public String getPayload() {                                        // <.>
        return taskProgress.toHtmlProgressBar();
    }
}
//end::class[]
