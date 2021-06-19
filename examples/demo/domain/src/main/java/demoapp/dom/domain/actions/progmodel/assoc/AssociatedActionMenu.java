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
package demoapp.dom.domain.actions.progmodel.assoc;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.factory.FactoryService;

@DomainService(
        nature=NatureOfService.VIEW,
        logicalTypeName = "demo.AssociatedActionMenu"
)
@DomainObjectLayout(
        named="Associated Action"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class AssociatedActionMenu {

    @Inject private FactoryService factoryService;

    @Action
    @ActionLayout(cssClassFa="fa-bolt")
    public AssociatedActionDemo associatedActions(){
        return AssociatedActionDemo.createWithDemoData();
    }

//    @Action
//    @ActionLayout(cssClassFa="fa-bolt")
//    public AssociatedActionDemo associatedActions(){
//        val demo = factoryService.viewModel(AssociatedActionDemo.class);
//        demo.getItems().clear();
//        demo.getItems().add(DemoItem.of("first"));
//        demo.getItems().add(DemoItem.of("second"));
//        demo.getItems().add(DemoItem.of("third"));
//        demo.getItems().add(DemoItem.of("last"));
//        return demo;
//    }

}
