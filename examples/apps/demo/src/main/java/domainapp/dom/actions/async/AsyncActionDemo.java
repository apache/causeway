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

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.util.JaxbAdapters;
import org.apache.isis.extensions.sse.api.SseService;
import org.apache.isis.extensions.sse.api.SseService.ExecutionBehavior;
import org.apache.isis.extensions.sse.api.ServerSentEvents;
import org.apache.isis.extensions.sse.markup.ListeningMarkup;

import domainapp.utils.DemoStub;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

@XmlRootElement(name = "Demo")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.AsyncAction", editing=Editing.ENABLED)
public class AsyncActionDemo extends DemoStub {

    @XmlTransient
    @Inject SseService eventStreamService;

    @XmlElement @XmlJavaTypeAdapter(JaxbAdapters.MarkupAdapter.class)
    @Property
    @ServerSentEvents(observe=DemoTask.class) // bind to a SSE channel
    @Getter @Setter ListeningMarkup progressView;

    @Action
    public AsyncActionDemo startSimpleTask() {

        val demoTask = DemoTask.of(10);  // setup to run in 10 steps
        eventStreamService.submit(demoTask, ExecutionBehavior.SIMPLE);

        return this;
    }

    @Action
    public AsyncActionDemo startTaskWithItsOwnSession() {

        val demoTask = DemoTask.of(10);  // setup to run in 10 steps
        eventStreamService.submit(demoTask, ExecutionBehavior.REQUIRES_NEW_SESSION);

        return this;
    }

    @Override
    public void initDefaults() {
        progressView = ListeningMarkup.valueOfHtml("Please start a task!");
    }

}
