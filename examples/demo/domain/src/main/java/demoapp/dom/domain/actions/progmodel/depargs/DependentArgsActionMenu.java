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
package demoapp.dom.domain.actions.progmodel.depargs;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Named("demo.DependentArgsActionMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@DomainObjectLayout(named="Associated Action")
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class DependentArgsActionMenu {

    final FactoryService factoryService;

    @Action
    @ActionLayout(cssClassFa="fa-bolt")
    public DependentArgsActionDemo dependentArgsActions(){
        val demo = factoryService.viewModel(new DependentArgsActionDemo());

        demo.getItems().clear();
        demo.getItems().add(DemoItem.of("first", Parity.ODD));
        demo.getItems().add(DemoItem.of("second", Parity.EVEN));
        demo.getItems().add(DemoItem.of("third", Parity.ODD));
        demo.getItems().add(DemoItem.of("last", Parity.EVEN));

        return demo;
    }

}

