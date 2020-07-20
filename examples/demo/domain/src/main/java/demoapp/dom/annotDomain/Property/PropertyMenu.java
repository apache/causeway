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
package demoapp.dom.annotDomain.Property;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.value.Blob;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.annotDomain.Property.maxLength.PropertyMaxLengthVm;
import demoapp.dom.annotLayout.PropertyLayout.typicalLength.PropertyLayoutTypicalLengthVm;
import demoapp.dom.types.Samples;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.PropertyMenu")
@Log4j2
public class PropertyMenu {


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-ruler-horizontal", describedAs = "Length of text fields")
    public PropertyMaxLengthVm maxLength(){
        val vm = new PropertyMaxLengthVm();
        vm.setPropertyUsingAnnotation("abcdefghij");
        vm.setPropertyUsingLayout("abcdefghij");
        vm.setPropertyUsingMetaAnnotation("abcdefghij");
        vm.setPropertyUsingMetaAnnotationButOverridden("abc");
        return vm;
    }


}
