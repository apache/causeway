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
package demoapp.dom.domain.objects.customvaluetypes;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.CustomValueTypeMenu")
@RequiredArgsConstructor
public class CustomValueTypeMenu {

    private final FactoryService factoryService;
    private final NumberConstantJdoRepository numberConstantRepo;

    @Action
    @ActionLayout(cssClassFa="fa-bolt", describedAs = "Experimental support for custom value types")
    public CustomValueTypeVm customValueTypes(){

        if(numberConstantRepo.listAll().size() == 0) {
            numberConstantRepo.add("Pi", ComplexNumber.of(Math.PI, 0.));
            numberConstantRepo.add("Euler's Constant", ComplexNumber.of(Math.E, 0.));
            numberConstantRepo.add("Imaginary Unit", ComplexNumber.parse("0 + 1i"));
        }

        return factoryService.viewModel(new CustomValueTypeVm());
    }



}
