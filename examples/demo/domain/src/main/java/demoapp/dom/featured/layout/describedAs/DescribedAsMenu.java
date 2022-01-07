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

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.DomainService;
import org.apache.isis.applib.annotations.NatureOfService;
import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.annotations.SemanticsOf;
import org.apache.isis.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;
import lombok.val;

import demoapp.dom.domain.actions.progmodel.assoc.DemoItem;

@DomainService(
        nature=NatureOfService.VIEW,
        logicalTypeName = "demo.DescribedAsMenu"
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
