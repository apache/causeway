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
package demoapp.dom.services.core;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;

import demoapp.dom.services.core.wrapperFactory.WrapperFactoryDemoPage;

import lombok.RequiredArgsConstructor;

import demoapp.dom.services.core.errorreportingservice.ErrorReportingServiceDemoPage;
import demoapp.dom.services.core.eventbusservice.EventBusServiceDemoPage;
import demoapp.dom.services.core.messageservice.MessageServiceDemoPage;

@Named("demo.ServicesMenu")
@DomainService(nature=NatureOfService.VIEW)
@Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServicesMenu {

    private final FactoryService factoryService;

    @Action
    @ActionLayout(cssClassFa="fa-bolt")
    public EventBusServiceDemoPage eventBusService(){
        return new EventBusServiceDemoPage();
    }

    @Action
    @ActionLayout(cssClassFa="fa-sticky-note")
    public MessageServiceDemoPage messageService(){
        return new MessageServiceDemoPage();
    }

    @Action
    @ActionLayout(cssClassFa="fa-bomb")
    public ErrorReportingServiceDemoPage errorReportingService(){
        return new ErrorReportingServiceDemoPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-gift",
            describedAs = "Formal object interactions + async"
    )
    public WrapperFactoryDemoPage wrapperFactory(){
        return factoryService.viewModel(WrapperFactoryDemoPage.class);
    }

}
