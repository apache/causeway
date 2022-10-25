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
package demoapp.dom.domain.properties.Property;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.properties.Property.commandPublishing.PropertyCommandPublishingEntity;
import demoapp.dom.domain.properties.Property.domainEvent.PropertyDomainEventVm;
import demoapp.dom.domain.properties.Property.editing.PropertyEditingVm;
import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingEntity;
import demoapp.dom.domain.properties.Property.fileAccept.PropertyFileAcceptVm;
import demoapp.dom.domain.properties.Property.hidden.PropertyHiddenVm;
import demoapp.dom.domain.properties.Property.hidden.child.PropertyHiddenChildVm;
import demoapp.dom.domain.properties.Property.maxLength.PropertyMaxLengthVm;
import demoapp.dom.domain.properties.Property.mustSatisfy.PropertyMustSatisfyVm;
import demoapp.dom.domain.properties.Property.optionality.PropertyOptionalityVm;
import demoapp.dom.domain.properties.Property.projecting.PropertyProjectingVm;
import demoapp.dom.domain.properties.Property.projecting.child.PropertyProjectingChildVm;
import demoapp.dom.domain.properties.Property.projecting.persistence.PropertyProjectingChildEntity;
import demoapp.dom.domain.properties.Property.regexPattern.PropertyRegexPatternVm;
import demoapp.dom.domain.properties.Property.snapshot.PropertySnapshotVm;
import demoapp.dom.types.Samples;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Named("demo.PropertyMenu")
@DomainService(
        nature=NatureOfService.VIEW
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PropertyMenu {

    final ValueHolderRepository<String, ? extends PropertyCommandPublishingEntity> propertyCommandPublishingEntities;
    final ValueHolderRepository<String, ? extends PropertyExecutionPublishingEntity> propertyExecutionPublishingEntities;
    final ValueHolderRepository<String, ? extends PropertyProjectingChildEntity> propertyProjectingChildEntities;
    final Samples<Blob> blobSamples;
    final Samples<Clob> clobSamples;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-terminal", describedAs = "Action invocation intentions as XML")
    public PropertyCommandPublishingEntity commandPublishing(){
        return propertyCommandPublishingEntities.first().orElse(null);
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-asterisk", describedAs = "Class of the domain event emitted when interacting with the property")
    public PropertyDomainEventVm domainEvent(){
        return new PropertyDomainEventVm("change me");
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
    @ActionLayout(cssClassFa="fa-book", describedAs = "Property changed events as XML")
    public PropertyExecutionPublishingEntity executionPublishing(){
        return propertyExecutionPublishingEntities.first().orElse(null);
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
    @ActionLayout(cssClassFa="fa-external-link-square-alt", describedAs = "Regular expressions, such as email")
    public PropertyProjectingVm projecting(){
        val vm = new PropertyProjectingVm();

        propertyProjectingChildEntities.all().forEach(childEntity -> {
            val childVm = new PropertyProjectingChildVm(childEntity);
            vm.getChildren().add(childVm);
        });

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

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-camera", describedAs = "Snapshot inclusion/exclusion")
    public PropertySnapshotVm snapshot(){
        return new PropertySnapshotVm("value");
    }

    private void setSampleBlob(final String suffix, final Consumer<Blob> blobConsumer) {
        blobSamples.stream()
                .filter(x -> x.getName().endsWith(suffix))
                .findFirst()
                .ifPresent(blobConsumer);
    }

    private void setSampleClob(final String suffix, final Consumer<Clob> clobConsumer) {
        clobSamples.stream()
                .filter(x -> x.getName().endsWith(suffix))
                .findFirst()
                .ifPresent(clobConsumer);
    }


}
