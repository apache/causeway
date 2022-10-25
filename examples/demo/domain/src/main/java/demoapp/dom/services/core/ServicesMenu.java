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

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.services.core.errorreportingservice.ErrorReportingServiceDemoVm;
import demoapp.dom.services.core.eventbusservice.EventBusServiceDemoVm;
import demoapp.dom.services.core.messageservice.MessageServiceDemoVm;
import demoapp.dom.services.core.wrapperFactory.WrapperFactoryEntity;
import demoapp.dom.services.core.xmlSnapshotService.XmlSnapshotParentVm;
import demoapp.dom.services.core.xmlSnapshotService.peer.XmlSnapshotPeerVm;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Named("demo.ServicesMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServicesMenu {

    final ValueHolderRepository<String, ? extends WrapperFactoryEntity> wrapperFactoryEntities;
    final FactoryService factoryService;


    @Action
    @ActionLayout(cssClassFa="fa-bolt")
    public EventBusServiceDemoVm eventBusService(){
        return factoryService.viewModel(new EventBusServiceDemoVm());
    }

    @Action
    @ActionLayout(cssClassFa="fa-sticky-note")
    public MessageServiceDemoVm messageService(){
        return factoryService.viewModel(new MessageServiceDemoVm());
    }

    @Action
    @ActionLayout(cssClassFa="fa-bomb")
    public ErrorReportingServiceDemoVm errorReportingService(){
        return factoryService.viewModel(new ErrorReportingServiceDemoVm());
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-gift", describedAs = "Formal object interactions + async")
    public WrapperFactoryEntity wrapperFactory(){
        return wrapperFactoryEntities.first().orElse(null);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-camera", describedAs = "Snapshot object graphs as XML")
    public XmlSnapshotParentVm xmlSnapshot(){

        val parentVm = new XmlSnapshotParentVm("parent object");

        parentVm.addChild("child 1");
        parentVm.addChild("child 2");
        parentVm.addChild("child 3");

        final XmlSnapshotPeerVm peerVm = new XmlSnapshotPeerVm("peer object");
        parentVm.setPeer(peerVm);

        peerVm.addChild("child 1");
        peerVm.addChild("child 2");
        peerVm.addChild("child 3");

        return parentVm;
    }

}
