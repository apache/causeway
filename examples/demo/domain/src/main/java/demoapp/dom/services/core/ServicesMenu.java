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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.services.core.wrapperFactory.WrapperFactoryJdo;
import demoapp.dom.services.core.wrapperFactory.WrapperFactoryJdoEntities;
import demoapp.dom.services.core.xmlSnapshotService.XmlSnapshotParentVm;
import demoapp.dom.services.core.xmlSnapshotService.peer.XmlSnapshotPeerVm;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.ServicesMenu")
@Log4j2
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ServicesMenu {

    final WrapperFactoryJdoEntities wrapperFactoryJdoEntities;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-gift", describedAs = "Formal object interactions + async")
    public WrapperFactoryJdo wrapperFactory(){
        return wrapperFactoryJdoEntities.first();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-camera", describedAs = "Snapshot object graphs as XML")
    public XmlSnapshotParentVm xmlSnapshot(){

        val parentVm = new XmlSnapshotParentVm("parent object");

        parentVm.setPeer(new XmlSnapshotPeerVm("peer object"));

        parentVm.addChild("child 1");
        parentVm.addChild("child 2");
        parentVm.addChild("child 3");

        return parentVm;
    }

}
