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
package domainapp.dom.actions.async;

import java.util.concurrent.atomic.LongAdder;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.events.sse.EventStream;
import org.apache.isis.applib.events.sse.EventStreamSource;
import org.apache.isis.applib.value.Markup;

import lombok.RequiredArgsConstructor;

@DomainObject(nature=Nature.INMEMORY_ENTITY, editing=Editing.DISABLED) 
@RequiredArgsConstructor(staticName="of")
public class DemoTask implements EventStreamSource {
	
    public String title() {
        return String.format("DemoTask '%s'", Integer.toHexString(hashCode()));
    }
    
	private final int totalSteps;
    private TaskProgress taskProgress;


    @Override
    public void run(EventStream eventStream) {
        
        taskProgress = TaskProgress.of(new LongAdder(), totalSteps);
        
        for(int i=0;i<totalSteps;++i) {
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
                        
            taskProgress.getStepsProgressed().increment();
            
            eventStream.fire(this);
            
        }
    }

    @Override
    public Markup getPayload() {
        return new Markup("" + taskProgress + "<br/>" + taskProgress.toHtmlProgressBar());
    }



}
