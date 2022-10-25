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
package demoapp.dom.featured.layout.describedAs;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.factory.FactoryService;

import demoapp.dom.domain.actions.progmodel.assoc.DemoItem;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Named("demo.DescribedAsMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class DescribedAsMenu {

    final FactoryService factoryService;


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-comment", describedAs="Opens the Tooltip-Demo page.")
    public DescribedAsVm describedAs(){
        val demo = factoryService.viewModel(new DescribedAsVm());

        demo.getCollection().add(DemoItem.of("first"));
        demo.getCollection().add(DemoItem.of("second"));
        demo.getCollection().add(DemoItem.of("third"));

        return demo;
    }

}
