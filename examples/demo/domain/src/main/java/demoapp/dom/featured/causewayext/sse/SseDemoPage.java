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

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.value.Markup;
import org.apache.causeway.extensions.sse.applib.annotations.ServerSentEvents;
import org.apache.causeway.extensions.sse.applib.service.SseService;
import org.apache.causeway.extensions.sse.applib.service.SseService.ExecutionBehavior;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Named("demo.AsyncAction")
@DomainObject(nature=Nature.VIEW_MODEL, editing=Editing.DISABLED)
@DomainObjectLayout(cssClassFa="fa-bolt")
@XmlRootElement(name = "root")
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor
public class SseDemoPage implements HasAsciiDocDescription {

    public String title() { return "Server-side events"; }

    @Inject @XmlTransient SseService sseService;

//tag::progressView[]
    @XmlElement @XmlJavaTypeAdapter(Markup.JaxbToStringAdapter.class)
    @Property
    @ServerSentEvents(observe=DemoTask.class)           // <.>
    @Getter @Setter Markup progressView;
//end::progressView[]

//tag::startSimpleTask[]
    @Action
    public SseDemoPage startSimpleTask() {
        final DemoTask demoTask = DemoTask.of(100);             // <.>
        sseService.submit(demoTask, ExecutionBehavior.SIMPLE);  // <.>
        return this;
    }
//end::startSimpleTask[]

//tag::startTaskWithItsOwnSession[]
    @Action
    public SseDemoPage startTaskWithItsOwnSession() {
        final DemoTask demoTask = DemoTask.of(10);  // setup to run in 10 steps
        sseService.submit(demoTask, ExecutionBehavior.REQUIRES_NEW_SESSION);
        return this;
    }
//end::startTaskWithItsOwnSession[]
}
