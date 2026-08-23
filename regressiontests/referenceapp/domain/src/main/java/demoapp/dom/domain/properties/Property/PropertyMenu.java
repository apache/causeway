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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;

import lombok.RequiredArgsConstructor;

import demoapp.dom._infra.values.ValueHolderRepository;
import demoapp.dom.domain.properties.Property.commandPublishing.PropertyCommandPublishingPage;
import demoapp.dom.domain.properties.Property.domainEvent.PropertyDomainEventPage;
import demoapp.dom.domain.properties.Property.editing.PropertyEditingPage;
import demoapp.dom.domain.properties.Property.executionPublishing.PropertyExecutionPublishingPage;
import demoapp.dom.domain.properties.Property.fileAccept.PropertyFileAcceptPage;
import demoapp.dom.domain.properties.Property.maxLength.PropertyMaxLengthPage;
import demoapp.dom.domain.properties.Property.mustSatisfy.PropertyMustSatisfyPage;
import demoapp.dom.domain.properties.Property.optionality.PropertyOptionalityPage;
import demoapp.dom.domain.properties.Property.projecting.PropertyProjectingChildEntity;
import demoapp.dom.domain.properties.Property.projecting.PropertyProjectingChildVm;
import demoapp.dom.domain.properties.Property.projecting.PropertyProjectingPage;
import demoapp.dom.domain.properties.Property.regexPattern.PropertyRegexPatternPage;
import demoapp.dom.domain.properties.Property.snapshot.PropertySnapshotPage;
import demoapp.dom.types.Samples;

@Named("demo.PropertyMenu")
@DomainService
@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class PropertyMenu {

    final ValueHolderRepository<String, ? extends PropertyProjectingChildEntity> propertyProjectingChildEntities;
    final Samples<Blob> blobSamples;
    final Samples<Clob> clobSamples;
    final Samples<String> stringSamples;

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-terminal",
            describedAs = "Action invocation intentions as XML"
    )
    public PropertyCommandPublishingPage commandPublishing(){
        return new PropertyCommandPublishingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-asterisk",
            describedAs = "Class of the domain event emitted when interacting with the property"
    )
    public PropertyDomainEventPage domainEvent(){
        return new PropertyDomainEventPage("change me");
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-pencil-alt",
            describedAs = "Editable fields"
    )
    public PropertyEditingPage editing(){
        return new PropertyEditingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-book",
            describedAs = "Property changed events as XML"
    )
    public PropertyExecutionPublishingPage executionPublishing(){
        return new PropertyExecutionPublishingPage();
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-file-upload",
            describedAs = "Length of text fields"
    )
    public PropertyFileAcceptPage fileAccept(){
        var page = new PropertyFileAcceptPage();
        setSampleBlob(".pdf", page::setPdfProperty);
        setSampleClob(".txt", page::setTxtProperty);
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-ruler-horizontal",
            describedAs = "Length of text fields"
    )
    public PropertyMaxLengthPage maxLength(){
        var vm = new PropertyMaxLengthPage();
        vm.setName(stringSamples.single());
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-star-half-alt",
            describedAs = "Regular expressions, such as email"
    )
    public PropertyMustSatisfyPage mustSatisfy(){
        var vm = new PropertyMustSatisfyPage();
        vm.setCustomerAge(18);
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(cssClassFa="fa-infinity", describedAs = "Regular expressions, such as email")
    public PropertyOptionalityPage optionality(){
        var vm = new PropertyOptionalityPage();
        vm.setOptionalProperty(null);
        vm.setMandatoryProperty("mandatory");
        vm.setNullableProperty(null);
        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-external-link-square-alt",
            describedAs = "Regular expressions, such as email"
    )
    public PropertyProjectingPage projecting(){
        var vm = new PropertyProjectingPage();

        propertyProjectingChildEntities.all().forEach(childEntity -> {
            var childVm = new PropertyProjectingChildVm(childEntity);
            vm.getChildren().add(childVm);
        });

        return vm;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-equals",
            describedAs = "Regular expressions, such as email"
    )
    public PropertyRegexPatternPage regexPattern(){
        var page = new PropertyRegexPatternPage();
        page.setEmailAddress("joe@bloggs.com");
        return page;
    }

    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClassFa="fa-camera",
            describedAs = "Snapshot inclusion/exclusion"
    )
    public PropertySnapshotPage snapshot(){
        return new PropertySnapshotPage("Fred", "Bloggs", "K", "These are some notes");
    }

    private void setSampleBlob(final String suffix, final Consumer<Blob> blobConsumer) {
        blobSamples.stream()
                .filter(x -> x.name().endsWith(suffix))
                .findFirst()
                .ifPresent(blobConsumer);
    }

    private void setSampleClob(final String suffix, final Consumer<Clob> clobConsumer) {
        clobSamples.stream()
                .filter(x -> x.name().endsWith(suffix))
                .findFirst()
                .ifPresent(clobConsumer);
    }

}
