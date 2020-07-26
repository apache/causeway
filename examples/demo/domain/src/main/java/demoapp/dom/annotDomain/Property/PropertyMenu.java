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

import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventJdo;
import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventJdoEntities;
import demoapp.dom.annotDomain.Property.domainEvent.PropertyDomainEventJdoSeedService;
import demoapp.dom.annotDomain.Property.editing.PropertyEditingVm;
import demoapp.dom.annotDomain.Property.fileAccept.PropertyFileAcceptVm;
import demoapp.dom.annotDomain.Property.hidden.PropertyHiddenVm;
import demoapp.dom.annotDomain.Property.hidden.child.PropertyHiddenChildVm;
import demoapp.dom.annotDomain.Property.maxLength.PropertyMaxLengthVm;
import demoapp.dom.annotDomain.Property.mustSatisfy.PropertyMustSatisfyVm;
import demoapp.dom.annotDomain.Property.optionality.PropertyOptionalityVm;
import demoapp.dom.annotDomain.Property.regexPattern.PropertyRegexPatternVm;
import demoapp.dom.types.Samples;

@DomainService(nature=NatureOfService.VIEW, objectType = "demo.PropertyMenu")
@Log4j2
public class PropertyMenu {

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-asterisk", describedAs = "Decouples interaction of properties")
    public PropertyDomainEventJdo domainEvent(){
        return propertyDomainEventJdoEntities.all().stream().findFirst().orElse(null);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-pencil-alt", describedAs = "Editable fields")
    public PropertyEditingVm editing(){
        val vm = new PropertyEditingVm();

        vm.setPropertyUsingAnnotation("this property is editable");
        vm.setPropertyUsingMetaAnnotation("this property is also editable");
        vm.setPropertyUsingMetaAnnotationButOverridden("this property is NOT editable");
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-file-upload", describedAs = "Length of text fields")
    public PropertyFileAcceptVm fileAccept(){
        val vm = new PropertyFileAcceptVm();

        setSampleBlob(".pdf", vm::setPdfPropertyUsingAnnotation);
        setSampleBlob(".pdf", vm::setPdfPropertyUsingMetaAnnotation);
        setSampleBlob(".docx", vm::setDocxPropertyUsingMetaAnnotationButOverridden);
        setSampleClob(".txt", vm::setTxtPropertyUsingAnnotation);

        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-glasses", describedAs = "Visibility of properties, eg in tables")
    public PropertyHiddenVm hidden() {
        val vm = new PropertyHiddenVm();
        vm.setPropertyHiddenAnywhere("hidden anywhere");
        vm.setPropertyHiddenEverywhere("hidden everywhere");
        vm.setPropertyHiddenNowhereUsingAnnotation("hidden nowhere using annotation");
        vm.setPropertyUsingMetaAnnotation("using meta-annotation");
        vm.setPropertyUsingMetaAnnotationButOverridden("using meta-annotation but overridden");

        vm.getChildren().add(new PropertyHiddenChildVm("child 1", vm));
        vm.getChildren().add(new PropertyHiddenChildVm("child 2", vm));
        vm.getChildren().add(new PropertyHiddenChildVm("child 3", vm));
        return vm;
    }


    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-ruler-horizontal", describedAs = "Length of text fields")
    public PropertyMaxLengthVm maxLength(){
        val vm = new PropertyMaxLengthVm();
        vm.setPropertyUsingAnnotation("abcdefghij");
        vm.setPropertyUsingMetaAnnotation("abcdefghij");
        vm.setPropertyUsingMetaAnnotationButOverridden("abc");
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-star-half-alt", describedAs = "Regular expressions, such as email")
    public PropertyMustSatisfyVm mustSatisfy(){
        val vm = new PropertyMustSatisfyVm();
        vm.setCustomerAgePropertyUsingAnnotation(18);
        vm.setCustomerAgePropertyUsingMetaAnnotation(65);
        vm.setCustomerAgePropertyUsingMetaAnnotationButOverridden(66);
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-infinity", describedAs = "Regular expressions, such as email")
    public PropertyOptionalityVm optionality(){
        val vm = new PropertyOptionalityVm();
        vm.setPropertyUsingAnnotation(null);
        vm.setMandatoryPropertyUsingAnnotation("mandatory");
        vm.setPropertyUsingMetaAnnotation(null);
        vm.setPropertyUsingMetaAnnotationButOverridden("mandatory");
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-equals", describedAs = "Regular expressions, such as email")
    public PropertyRegexPatternVm regexPattern(){
        val vm = new PropertyRegexPatternVm();
        vm.setEmailAddressPropertyUsingAnnotation("joe@bloggs.com");
        vm.setEmailAddressPropertyUsingMetaAnnotation("flo@bloggs.com");
        vm.setEmailAddressPropertyUsingMetaAnnotationButOverridden("mo@bloggs.org");
        return vm;
    }

    private void setSampleBlob(String suffix, Consumer<Blob> blobConsumer) {
        blobSamples.stream()
                .filter(x -> x.getName().endsWith(suffix))
                .findFirst()
                .ifPresent(blobConsumer);
    }

    private void setSampleClob(String suffix, Consumer<Clob> clobConsumer) {
        clobSamples.stream()
                .filter(x -> x.getName().endsWith(suffix))
                .findFirst()
                .ifPresent(clobConsumer);
    }

    @Inject
    Samples<Blob> blobSamples;
    @Inject
    Samples<Clob> clobSamples;
    @Inject
    PropertyDomainEventJdoEntities propertyDomainEventJdoEntities;

}
